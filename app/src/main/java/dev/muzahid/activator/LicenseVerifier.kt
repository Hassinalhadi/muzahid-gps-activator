package dev.muzahid.activator

import android.content.Context
import android.os.Build
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDate
import java.util.Base64

class LicenseVerifier(private val context: Context) {
    data class Result(val valid: Boolean, val message: String)

    fun validate(license: String, today: LocalDate = LocalDate.now()): Result {
        return try {
            val parts = license.trim().split('.')
            if (parts.size != 3 || parts[0] != "MZL1") return Result(false, "صيغة المفتاح غير صحيحة")
            val payload = String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
            val signature = Base64.getUrlDecoder().decode(parts[2])
            val fields = payload.split('|')
            if (fields.size != 4 || fields[0] != "MZL1") return Result(false, "بيانات الترخيص غير صحيحة")
            val expiry = LocalDate.parse(fields[2])
            if (today.isAfter(expiry)) return Result(false, "انتهت صلاحية الترخيص في ${fields[2]}")
            val deviceId = fields[3]
            if (deviceId != "*" && deviceId != deviceFingerprint()) return Result(false, "الترخيص غير مخصص لهذا الجهاز")

            val publicKeyDer = Base64.getMimeDecoder().decode(
                context.getString(R.string.license_public_key)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
            )
            val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKeyDer))
            val verifier = Signature.getInstance("SHA256withRSA")
            verifier.initVerify(publicKey)
            verifier.update(payload.toByteArray(StandardCharsets.UTF_8))
            if (!verifier.verify(signature)) return Result(false, "توقيع الترخيص غير صالح")
            Result(true, "مرخّص حتى ${fields[2]} — ${fields[1]}")
        } catch (_: Exception) {
            Result(false, "تعذر التحقق من الترخيص")
        }
    }

    private fun deviceFingerprint(): String {
        val androidId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        return androidId.lowercase()
    }
}

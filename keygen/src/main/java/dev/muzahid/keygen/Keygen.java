package dev.muzahid.keygen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;

/** Administrative-only license issuer. Never ship the private key with the APK. */
public final class Keygen {
    private static final String PREFIX = "MZL1";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            return;
        }
        switch (args[0]) {
            case "generate" -> generate(Path.of(args.length > 1 ? args[1] : "keys"));
            case "issue" -> {
                if (args.length < 4) {
                    throw new IllegalArgumentException("issue <private-key.pem> <customer> <expiry:YYYY-MM-DD> [device-id]");
                }
                issue(Path.of(args[1]), args[2], args[3], args.length > 4 ? args[4] : "*");
            }
            default -> usage();
        }
    }

    private static void generate(Path dir) throws Exception {
        Files.createDirectories(dir);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(3072);
        KeyPair pair = generator.generateKeyPair();
        Files.writeString(dir.resolve("license-private.pem"), pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
        Files.writeString(dir.resolve("license-public.pem"), pem("PUBLIC KEY", pair.getPublic().getEncoded()));
        System.out.println("Generated license keys in " + dir.toAbsolutePath());
        System.out.println("Copy only license-public.pem into the Android app; keep license-private.pem offline.");
    }

    private static void issue(Path privateKeyFile, String customer, String expiry, String deviceId) throws Exception {
        LocalDate.parse(expiry);
        PrivateKey key = readPrivateKey(privateKeyFile);
        String payload = String.join("|", PREFIX, clean(customer), expiry, clean(deviceId));
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(key);
        signer.update(payload.getBytes(StandardCharsets.UTF_8));
        String license = PREFIX + "." + b64(payload.getBytes(StandardCharsets.UTF_8)) + "." + b64(signer.sign());
        System.out.println(license);
    }

    private static PrivateKey readPrivateKey(Path file) throws Exception {
        byte[] der = decodePem(Files.readString(file), "PRIVATE KEY");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static String clean(String value) {
        if (value == null || value.isBlank() || value.contains("|")) {
            throw new IllegalArgumentException("Values must be non-empty and must not contain '|'.");
        }
        return value.trim();
    }

    private static String pem(String label, byte[] der) {
        return "-----BEGIN " + label + "-----\n" + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(der)
                + "\n-----END " + label + "-----\n";
    }

    private static byte[] decodePem(String pem, String label) {
        return Base64.getMimeDecoder().decode(pem.replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", ""));
    }

    private static String b64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("  gradle :keygen:run --args=\"generate [directory]\"");
        System.out.println("  gradle :keygen:run --args=\"issue <private-key.pem> <customer> <YYYY-MM-DD> [device-id]\"");
    }
}

package dev.muzahid.activator

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0F172A"))
            isFillViewport = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        scroll.addView(layout)
        setContentView(scroll)

        // Header Title
        val title = TextView(this).apply {
            text = "⚡ MUZAHID GPS VIP Activator"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        layout.addView(title)

        val subtitle = TextView(this).apply {
            text = "Modern LibXposed Module (API 102)\nUnlock VIP & Bypass Key Dialog"
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        layout.addView(subtitle)

        // Status Card
        val card = CardView(this).apply {
            radius = 24f
            setCardBackgroundColor(Color.parseColor("#1E293B"))
            cardElevation = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 36) }
        }

        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 36, 36, 36)
        }

        fun createStatusRow(label: String, value: String, valueColor: String): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 12, 0, 12)
                val tLabel = TextView(this@MainActivity).apply {
                    text = label
                    textSize = 15f
                    setTextColor(Color.parseColor("#CBD5E1"))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val tValue = TextView(this@MainActivity).apply {
                    text = value
                    textSize = 15f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor(valueColor))
                }
                addView(tLabel)
                addView(tValue)
            }
        }

        cardLayout.addView(createStatusRow("Architecture:", "Modern LibXposed (API 102)", "#38BDF8"))
        cardLayout.addView(createStatusRow("Target Package:", "com.muzahid.gps", "#FACC15"))
        cardLayout.addView(createStatusRow("License Bypass:", "Active (Permanent VIP)", "#4ADE80"))
        cardLayout.addView(createStatusRow("Xposed Warning Dialog:", "Eliminated (Auto-Dismiss)", "#4ADE80"))
        cardLayout.addView(createStatusRow("Key / License Popup:", "Bypassed (Zero Dialog)", "#4ADE80"))

        card.addView(cardLayout)
        layout.addView(card)

        // Open MUZAHID GPS Button
        val btnLaunch = Button(this).apply {
            text = "🚀 Open MUZAHID GPS"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2563EB"))
            setPadding(0, 24, 0, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            setOnClickListener {
                val launchIntent = packageManager.getLaunchIntentForPackage("com.muzahid.gps")
                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(this@MainActivity, "com.muzahid.gps is not installed!", Toast.LENGTH_LONG).show()
                }
            }
        }
        layout.addView(btnLaunch)

        // Instructions Footer
        val footer = TextView(this).apply {
            text = "📌 How to use:\n1. Enable this module in LSPosed Manager.\n2. Set scope to 'MUZAHID GPS (com.muzahid.gps)'.\n3. Open MUZAHID GPS — it will open directly to the map without any dialogs!"
            textSize = 13f
            setTextColor(Color.parseColor("#64748B"))
            setPadding(8, 16, 8, 0)
        }
        layout.addView(footer)
    }
}

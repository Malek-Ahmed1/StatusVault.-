package com.kaboas.statusvault

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.kaboas.statusvault.utils.LocaleHelper

class ContactActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "You won't receive reply notifications", Toast.LENGTH_LONG).show()
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val contactType = intent.getStringExtra("contact_type") ?: "General"

        findViewById<TextView>(R.id.txtContactType).text = contactType
        findViewById<ImageButton>(R.id.btnCloseContact).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btnTranslateContact).setOnClickListener {
            startActivity(android.content.Intent(this, TranslateActivity::class.java))
        }

        val btnTheme = findViewById<ImageButton>(R.id.btnThemeToggleContact)
        btnTheme.setImageResource(
            if (isNightMode()) R.drawable.light else R.drawable.dark
        )
        btnTheme.setOnClickListener {
            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            val currentMode = prefs.getBoolean("night_mode", false)
            val newMode = !currentMode
            prefs.edit().putBoolean("night_mode", newMode).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (newMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
        }

        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<MaterialButton>(R.id.btnSendContact)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDeleteContact)

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_write), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            btnSend.isEnabled = false
            btnSend.text = getString(R.string.sending)

            val data = hashMapOf(
                "type" to contactType,
                "message" to message,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "status" to "pending",
                "reply" to ""
            )

            db.collection("messages")
                .add(data)
                .addOnCompleteListener {
                    Toast.makeText(this, getString(R.string.message_sent), Toast.LENGTH_SHORT).show()
                    etMessage.setText("")
                    btnSend.isEnabled = true
                    btnSend.text = getString(R.string.send)
                }
        }

        btnDelete.setOnClickListener {
            etMessage.setText("")
            Toast.makeText(this, getString(R.string.cleared), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNightMode(): Boolean {
        return getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("night_mode", false)
    }
}

package com.kaboas.statusvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.kaboas.statusvault.data.MessageRepository
import com.kaboas.statusvault.utils.BadgeHelper
import com.kaboas.statusvault.utils.LocaleHelper
import kotlinx.coroutines.launch

class ContactTypeActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_type)

        findViewById<ImageButton>(R.id.btnCloseContactType).setOnClickListener { finish() }

        val btnTheme = findViewById<ImageButton>(R.id.btnThemeToggleContactType)
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

        findViewById<LinearLayout>(R.id.btnSuggestion).setOnClickListener {
            openContactScreen("Suggestion")
        }
        findViewById<LinearLayout>(R.id.btnComplaint).setOnClickListener {
            openContactScreen("Complaint")
        }
        findViewById<LinearLayout>(R.id.btnQuestionsRequests).setOnClickListener {
            openContactScreen("Questions & Requests")
        }

        loadBadges()
    }

    override fun onResume() {
        super.onResume()
        loadBadges()
    }

    private fun loadBadges() {
        val badgeSuggestion = findViewById<TextView>(R.id.badgeSuggestion)
        val badgeComplaint = findViewById<TextView>(R.id.badgeComplaint)
        val badgeQuestions = findViewById<TextView>(R.id.badgeQuestions)

        lifecycleScope.launch {
            val lastSeen = BadgeHelper.getLastSeen(this@ContactTypeActivity)
            val sc = MessageRepository.getReplyCountByType("Suggestion", lastSeen)
            val cc = MessageRepository.getReplyCountByType("Complaint", lastSeen)
            val qc = MessageRepository.getReplyCountByType("Questions & Requests", lastSeen)
            setBadge(badgeSuggestion, sc)
            setBadge(badgeComplaint, cc)
            setBadge(badgeQuestions, qc)
        }
    }

    private fun setBadge(view: TextView, count: Int) {
        if (count > 0) {
            view.text = count.toString()
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun isNightMode(): Boolean {
        return getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("night_mode", false)
    }

    private fun openContactScreen(type: String) {
        val intent = Intent(this, ContactActivity::class.java)
        intent.putExtra("contact_type", type)
        startActivity(intent)
    }
}

package com.kaboas.statusvault

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaboas.statusvault.adapter.MessageAdapter
import com.kaboas.statusvault.data.MessageRepository
import com.kaboas.statusvault.utils.BadgeHelper
import com.kaboas.statusvault.utils.LocaleHelper
import kotlinx.coroutines.launch

class MyMessagesActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_messages)

        findViewById<ImageButton>(R.id.btnCloseMyMessages).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btnTranslateMyMessages).setOnClickListener {
            startActivity(android.content.Intent(this, TranslateActivity::class.java))
        }

        val btnTheme = findViewById<ImageButton>(R.id.btnThemeToggleMyMessages)
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

        val recycler = findViewById<RecyclerView>(R.id.recyclerMessages)
        val txtEmpty = findViewById<TextView>(R.id.txtNoMessages)

        recycler.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val messages = MessageRepository.getMessagesWithReplies()
            if (messages.isEmpty()) {
                recycler.visibility = View.GONE
                txtEmpty.visibility = View.VISIBLE
            } else {
                recycler.visibility = View.VISIBLE
                txtEmpty.visibility = View.GONE
                recycler.adapter = MessageAdapter(messages)
            }
            BadgeHelper.setLastSeen(this@MyMessagesActivity, System.currentTimeMillis())
        }
    }

    private fun isNightMode(): Boolean {
        return getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("night_mode", false)
    }
}

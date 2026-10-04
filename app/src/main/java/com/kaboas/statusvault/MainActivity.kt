package com.kaboas.statusvault

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.kaboas.statusvault.data.MessageRepository
import com.kaboas.statusvault.utils.BadgeHelper
import com.kaboas.statusvault.utils.LocaleHelper
import com.kaboas.statusvault.utils.NotificationHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Permission needed. Closing app...", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createChannel(this)

        drawerLayout = findViewById(R.id.drawerLayout)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // زر الترجمة
        findViewById<ImageButton>(R.id.btnTranslate).setOnClickListener {
            startActivity(Intent(this, TranslateActivity::class.java))
        }

        // زر الوضع الليلي
        val btnTheme = findViewById<ImageButton>(R.id.btnThemeToggle)
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

        // زر المفضلة
        findViewById<ImageButton>(R.id.btnFavoriteToolbar).setOnClickListener {
            val intent = Intent(this, ReelsActivity::class.java)
            intent.putExtra("favorites", true)
            startActivity(intent)
        }

        // القائمة الجانبية
        val navView = findViewById<NavigationView>(R.id.navigationView)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_home -> { }
                R.id.drawer_favorites -> {
                    val intent = Intent(this, ReelsActivity::class.java)
                    intent.putExtra("favorites", true)
                    startActivity(intent)
                }
                R.id.drawer_apps -> {
                    startActivity(Intent(this, OurAppsActivity::class.java))
                }
                R.id.drawer_contact -> {
                    startActivity(Intent(this, ContactTypeActivity::class.java))
                }
                R.id.drawer_about -> {
                    Toast.makeText(this, "About Us", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        checkStoragePermission()

        findViewById<MaterialButton>(R.id.btnOpenWhatsApp).setOnClickListener {
            openWhatsApp()
        }

        findViewById<MaterialButton>(R.id.btnWatchReels).setOnClickListener {
            startActivity(Intent(this, ReelsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { }
    }

    override fun onResume() {
        super.onResume()
        checkForNewReplies()
    }

    private fun checkForNewReplies() {
        lifecycleScope.launch {
            val lastSeen = BadgeHelper.getLastSeen(this@MainActivity)
            val totalReplies = MessageRepository.getTotalReplyCount(lastSeen)
            if (totalReplies > 0) {
                NotificationHelper.showReplyNotification(this@MainActivity, totalReplies)
            }
        }
    }

    private fun isNightMode(): Boolean {
        return getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("night_mode", false)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openWhatsApp() {
        var intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (intent == null) {
            intent = packageManager.getLaunchIntentForPackage("com.whatsapp.w4b")
        }
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, R.string.open_whatsapp_error, Toast.LENGTH_SHORT).show()
        }
    }
}

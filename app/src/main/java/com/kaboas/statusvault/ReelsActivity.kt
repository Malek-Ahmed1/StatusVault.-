package com.kaboas.statusvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.kaboas.statusvault.data.MediaType
import com.kaboas.statusvault.fragments.ReelsFragment
import com.kaboas.statusvault.utils.LocaleHelper

class ReelsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var isFavoritesMode = false

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reels)

        drawerLayout = findViewById(R.id.drawerLayoutReels)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarReels)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        isFavoritesMode = intent.getBooleanExtra("favorites", false)

        // زر الترجمة
        findViewById<ImageButton>(R.id.btnTranslateReels).setOnClickListener {
            startActivity(Intent(this, TranslateActivity::class.java))
        }

        // زر الوضع الليلي
        val btnTheme = findViewById<ImageButton>(R.id.btnThemeToggleReels)
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

        // القائمة الجانبية
        val navView = findViewById<NavigationView>(R.id.navigationViewReels)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
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
                    android.widget.Toast.makeText(this, "About Us", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val btnFavToolbar = findViewById<ImageButton>(R.id.btnFavoriteToolbar)
        val bottomNav = findViewById<LinearLayout>(R.id.bottomNavReels)
        val txtSubtitle = findViewById<TextView>(R.id.txtReelsSubtitle)

        if (isFavoritesMode) {
            txtSubtitle.text = getString(R.string.favorites)
            bottomNav.visibility = View.GONE
            btnFavToolbar.visibility = View.GONE
            if (savedInstanceState == null) {
                switchFragment(ReelsFragment.newInstanceFavorites())
            }
        } else {
            txtSubtitle.text = getString(R.string.watch_reels)
            bottomNav.visibility = View.VISIBLE
            btnFavToolbar.visibility = View.VISIBLE
            btnFavToolbar.setImageResource(R.drawable.love_menu)
            btnFavToolbar.setOnClickListener {
                val intent = Intent(this, ReelsActivity::class.java)
                intent.putExtra("favorites", true)
                startActivity(intent)
            }

            findViewById<LinearLayout>(R.id.navReelsVideos).setOnClickListener {
                switchFragment(ReelsFragment.newInstance(MediaType.VIDEO))
            }
            findViewById<LinearLayout>(R.id.navReelsPhotos).setOnClickListener {
                switchFragment(ReelsFragment.newInstance(MediaType.IMAGE))
            }

            if (savedInstanceState == null) {
                switchFragment(ReelsFragment.newInstance(MediaType.VIDEO))
            }
        }
    }

    private fun isNightMode(): Boolean {
        return getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("night_mode", false)
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.reelsFragmentContainer, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

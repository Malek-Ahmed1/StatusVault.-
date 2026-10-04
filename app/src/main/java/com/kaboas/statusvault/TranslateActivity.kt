package com.kaboas.statusvault

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.kaboas.statusvault.utils.LocaleHelper

class TranslateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        val toolbar = findViewById<Toolbar>(R.id.toolbarTranslate)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<LinearLayout>(R.id.btnLangEn).setOnClickListener { setLang("en") }
        findViewById<LinearLayout>(R.id.btnLangAr).setOnClickListener { setLang("ar") }
        findViewById<LinearLayout>(R.id.btnLangFr).setOnClickListener { setLang("fr") }
        findViewById<LinearLayout>(R.id.btnLangZh).setOnClickListener { setLang("zh") }
        findViewById<LinearLayout>(R.id.btnLangJa).setOnClickListener { setLang("ja") }
        findViewById<LinearLayout>(R.id.btnLangEs).setOnClickListener { setLang("es") }
        findViewById<LinearLayout>(R.id.btnLangDe).setOnClickListener { setLang("de") }
    }

    private fun setLang(lang: String) {
        LocaleHelper.setLanguage(this, lang)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

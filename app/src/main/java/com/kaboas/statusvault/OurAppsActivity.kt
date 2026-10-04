package com.kaboas.statusvault

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.kaboas.statusvault.adapter.AppsAdapter
import com.kaboas.statusvault.utils.LocaleHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AppItem(
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val link: String = ""
)

class OurAppsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_our_apps)

        val toolbar = findViewById<Toolbar>(R.id.toolbarOurApps)
        toolbar.setNavigationOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerApps)
        recycler.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val snapshot = db.collection("apps").get().await()
                val apps = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    AppItem(
                        name = name,
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        link = doc.getString("link") ?: ""
                    )
                }
                recycler.adapter = AppsAdapter(apps)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

package com.kaboas.statusvault.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kaboas.statusvault.OurAppsActivity
import com.kaboas.statusvault.R

class AppsAdapter(private val items: List<OurAppsActivity.AppItem>) :
    RecyclerView.Adapter<AppsAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgAppLogo)
        val txtName: TextView = v.findViewById(R.id.txtAppName)
        val txtDesc: TextView = v.findViewById(R.id.txtAppDesc)
        val container: View = v.findViewById(R.id.appContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        holder.txtName.text = app.name
        holder.txtDesc.text = app.description

        if (app.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView).load(app.imageUrl).into(holder.img)
        }

        holder.container.setOnClickListener {
            if (app.link.isNotEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.link))
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getItemCount() = items.size
}

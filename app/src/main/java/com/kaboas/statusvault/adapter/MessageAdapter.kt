package com.kaboas.statusvault.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaboas.statusvault.R
import com.kaboas.statusvault.data.UserMessage

class MessageAdapter(private var items: List<UserMessage>) : RecyclerView.Adapter<MessageAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val txtType: TextView = v.findViewById(R.id.txtMessageType)
        val txtMessage: TextView = v.findViewById(R.id.txtYourMessage)
        val txtReply: TextView = v.findViewById(R.id.txtReply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = items[position]
        holder.txtType.text = msg.type
        holder.txtMessage.text = msg.message
        holder.txtReply.text = msg.reply
    }

    override fun getItemCount() = items.size
}

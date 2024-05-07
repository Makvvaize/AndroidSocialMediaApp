package com.example.socialmedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewSender: TextView = itemView.findViewById(R.id.textViewSender)
        private val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)

        fun bind(message: Message) {
            textViewSender.text = message.senderDisplayName
            textViewMessage.text = message.content
            textViewTimestamp.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                Date(message.timestamp)
            )
        }
    }
}



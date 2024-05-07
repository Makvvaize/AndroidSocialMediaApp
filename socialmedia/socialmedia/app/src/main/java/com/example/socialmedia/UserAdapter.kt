package com.example.socialmedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val userList: List<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>(), Filterable {

    private var filteredUserList: List<User> = userList
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(user: User)

    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = filteredUserList[position]
        holder.displayName.text = currentUser.displayName
        holder.email.text = currentUser.email
        holder.itemView.setOnClickListener {
            listener?.onItemClick(filteredUserList[position])
        }
    }

    override fun getItemCount(): Int {
        return filteredUserList.size
    }

    inner class UserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val displayName: TextView = itemView.findViewById(R.id.textViewDisplayName)
        val email: TextView = itemView.findViewById(R.id.emailTextView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener?.onItemClick(filteredUserList[position])
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<User>()

                if (!constraint.isNullOrBlank()) {
                    val filterPattern = constraint.toString().toLowerCase().trim()
                    for (user in userList) {
                        if (user.displayName?.toLowerCase()?.contains(filterPattern) == true ||
                            user.email?.toLowerCase()?.contains(filterPattern) == true
                        ) {
                            filteredList.add(user)
                        }
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredUserList = results?.values as List<User>
                notifyDataSetChanged()
            }
        }
    }
}

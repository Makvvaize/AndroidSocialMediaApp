package com.example.socialmedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class PostAdapter(private var posts: List<Post>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener : AdapterView.OnItemClickListener {
        fun onItemClick(post: Post)
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        Glide.with(holder.itemView)
            .load(post.mediaUrl)
            .into(holder.postimageView)
        holder.bind(post)

    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun setPosts(posts: List<Post>) {
        // Reverse the order of posts
        this.posts = posts.reversed()
        notifyDataSetChanged()
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewPostTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.textViewPostContent)
        private val authorTextView: TextView = itemView.findViewById(R.id.textViewPostAuthor)
        val postimageView: ImageView = itemView.findViewById(R.id.imageViewPost)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(post: Post) {
            authorTextView.text = post.authorDisplayName
            titleTextView.text = post.title
            contentTextView.text = post.content
        }


        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val post = posts[position]


                listener.onItemClick(post)
            }
        }
    }

}

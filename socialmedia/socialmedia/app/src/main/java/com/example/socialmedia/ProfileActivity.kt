package com.example.socialmedia

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity(), PostAdapter.OnItemClickListener {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postsDatabase: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var textViewDisplayName: TextView
    private lateinit var textViewEmail: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //Retrieve user details from Intent extras
        val userId = intent.getStringExtra("userId")
        val email = intent.getStringExtra("email")
        val displayName = intent.getStringExtra("displayName")

        // Create a User object from the retrieved details
        val user = User(userId, email, displayName)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val authorID = currentUser?.uid

        textViewDisplayName = findViewById(R.id.textViewDisplayName)
        textViewEmail = findViewById(R.id.textViewEmail)
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts)
        recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(emptyList(), this)
        recyclerViewPosts.adapter = postAdapter

        // Set email
        if (user != null) {
            textViewEmail.text = user.email
        }

        // Database reference to fetch user data
        val usersDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

        // Fetch user data
        usersDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userFetch = dataSnapshot.getValue(User::class.java)
                    userFetch?.let {
                        textViewDisplayName.text = it.displayName
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })

        // Database reference to fetch user posts
        postsDatabase = FirebaseDatabase.getInstance().getReference("posts")

        // Read posts from Firebase Realtime Database
        postsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val postsList = mutableListOf<Post>()
                for (postSnapshot in dataSnapshot.children) {
                    if (postSnapshot.getValue(Post::class.java)!!.authorId ==userId) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let { postsList.add(it) }
                    }
                }
                postAdapter.setPosts(postsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })



    }

    override fun onItemClick(post: Post) {
        val detailedPostIntent = Intent(this, DetailedPostActivity::class.java)
        detailedPostIntent.putExtra("post", post)
        startActivity(detailedPostIntent)
    }



}

package com.example.socialmedia

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), PostAdapter.OnItemClickListener {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postsDatabase: DatabaseReference
    private lateinit var usersDatabase: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var buttonCreatePost: Button
    private lateinit var buttonProfile: Button
    private lateinit var buttonMessage: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonCreatePost = findViewById(R.id.buttonCreatePost)
        buttonProfile = findViewById(R.id.buttonProfile)
        buttonMessage = findViewById(R.id.buttonMessage)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUserid = auth.currentUser!!.uid
        val postsList: List<Post> = mutableListOf<Post>()


        recyclerViewPosts = findViewById(R.id.recyclerViewPosts)
        recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postsList,this)
        recyclerViewPosts.adapter = postAdapter

        postsDatabase = FirebaseDatabase.getInstance().getReference("posts")
        usersDatabase = FirebaseDatabase.getInstance().getReference("users")

        buttonCreatePost.setOnClickListener {
           val CreatePostIntent = Intent(this, CreatePostActivity::class.java)
           startActivity(CreatePostIntent)
           finish()
        }

        buttonProfile = findViewById(R.id.buttonProfile)
        buttonProfile.setOnClickListener {
            fetchUserDetails(currentUserid) { user ->
                user?.let {
                    startProfileActivity(it)
                    Log.e(ContentValues.TAG, "${it.userId} ${it.email} ${it.displayName}")
                }
            }


        }

        buttonMessage = findViewById(R.id.buttonMessage)
        buttonMessage.setOnClickListener {
            val messageIntent = Intent(this, MessageBoxActivity::class.java)
            startActivity(messageIntent)
        }



        // Read posts from Firebase Realtime Database
        postsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val postsList = mutableListOf<Post>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let { postsList.add(it) }
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

    private fun startProfileActivity(user: User) {
        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        intent.putExtra("userId", user.userId)
        intent.putExtra("email", user.email)
        intent.putExtra("displayName", user.displayName)
        startActivity(intent)
    }

    private fun fetchUserDetails(userID: String, onComplete: (User?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(userID)

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                onComplete(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                onComplete(null)
            }
        })
    }

}

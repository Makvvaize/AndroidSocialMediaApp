package com.example.socialmedia

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailedPostActivity : AppCompatActivity() {

    private lateinit var post: Post
    private lateinit var commentsAdapter: CommentAdapter
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var editTextComment: EditText
    private lateinit var buttonAddComment: Button
    private lateinit var textViewLikeCount: TextView
    private lateinit var buttonLike: Button
    private lateinit var buttonVisitProfile: Button

    private lateinit var postLikesRef: DatabaseReference
    private lateinit var postRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_post)

        // Retrieve post data from Intent extras
        post = intent.getParcelableExtra("post")!!

        // Initialize views
        val titleTextView: TextView = findViewById(R.id.textViewPostTitle)
        val contentTextView: TextView = findViewById(R.id.textViewPostContent)
        val authorTextView: TextView = findViewById(R.id.textViewPostAuthor)
        val imageViewPost: ImageView = findViewById(R.id.imageViewPost)
        editTextComment = findViewById(R.id.editTextComment)
        buttonAddComment = findViewById(R.id.buttonAddComment)
        commentsRecyclerView = findViewById(R.id.recyclerViewComments)
        textViewLikeCount = findViewById(R.id.textViewLikeCount)
        buttonLike = findViewById(R.id.buttonLike)
        buttonVisitProfile = findViewById(R.id.buttonVisitProfile)

        val maxHeightInPixels = resources.getDimensionPixelSize(R.dimen.max_recyclerview_height)
        commentsRecyclerView.layoutParams.height = maxHeightInPixels

        // Populate views with post data
        titleTextView.text = post.title
        contentTextView.text = post.content
        authorTextView.text = post.authorDisplayName
        Glide.with(this)
            .load(post.mediaUrl)
            .into(imageViewPost)

        // Initialize comments RecyclerView and adapter
        commentsAdapter = CommentAdapter()
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentsAdapter

        // Load comments for the post
        loadComments()

        // Set up Firebase references
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        postLikesRef = FirebaseDatabase.getInstance().getReference("posts/${post.postId}/likes")
        postRef = FirebaseDatabase.getInstance().getReference("posts/${post.postId}")

        buttonAddComment.setOnClickListener {
            val commentText = editTextComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
                editTextComment.text.clear()
            } else {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }

        postLikesRef.child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    buttonLike.setOnClickListener {
                        unlikePost(userId)
                    }
                }
                else{
                    buttonLike.setOnClickListener {
                        likePost(userId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        buttonVisitProfile.setOnClickListener {
            fetchUserDetails(post.authorId!!) { user ->
                user?.let {
                    startProfileActivity(it)
                    Log.e(ContentValues.TAG, "${it.userId} ${it.email} ${it.displayName}")
                }
            }


        }


//        // Set up like button click listener
//        buttonLike.setOnClickListener {
//            if (userId != null ) {
//                likePost(userId)
//            } else {
//                // User is not authenticated, handle accordingly
//                Toast.makeText(this, "Please sign in to like the post", Toast.LENGTH_SHORT).show()
//            }
//        }

        // Load total like count for the post
        loadLikeCount()
    }

    private fun addComment(commentText: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val authorId = currentUser?.uid
        val authorDisplayName = currentUser?.displayName

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        val usersRef: DatabaseReference = databaseReference.child("users").child(authorId!!)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userDisplayName =
                        dataSnapshot.child("displayName").getValue(String::class.java)

                    val comment = Comment(commentText, authorId, userDisplayName)

                    val postCommentsRef =
                        post.postId?.let { FirebaseDatabase.getInstance().getReference("comments").child(it) }
                    val commentId = postCommentsRef?.push()?.key
                    if (commentId != null) {
                        postCommentsRef.child(commentId).setValue(comment)
                            .addOnSuccessListener {
                                // Comment added successfully
                            }
                            .addOnFailureListener { exception ->
                                // Handle failure to add comment
                            }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })




    }

    private fun loadComments() {
        val postCommentsRef = FirebaseDatabase.getInstance().getReference("comments/${post.postId}")
        postCommentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentsList = mutableListOf<Comment>()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    comment?.let { commentsList.add(it) }
                }
                commentsAdapter.setComments(commentsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun loadLikeCount() {
        postLikesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalLikes = snapshot.childrenCount.toInt()
                textViewLikeCount.text = totalLikes.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun likePost(userId: String) {
        postLikesRef.child(userId).setValue(true)
            .addOnSuccessListener {
                // Like added successfully
                Toast.makeText(this, "Post liked", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Handle failure to like the post
                Toast.makeText(this, "Failed to like the post: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unlikePost(userId: String) {
        postLikesRef.child(userId).removeValue()
            .addOnSuccessListener {
                // Like removed successfully
                Toast.makeText(this, "Post unliked", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Handle failure to unlike the post
                Toast.makeText(this, "Failed to unlike the post: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startProfileActivity(user: User) {
        val intent = Intent(this@DetailedPostActivity, ProfileActivity::class.java)
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

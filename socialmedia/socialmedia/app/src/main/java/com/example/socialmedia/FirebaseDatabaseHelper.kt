package com.example.socialmedia

import com.google.firebase.database.*

class FirebaseDatabaseHelper {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Function to add a new post to the database
    fun addPost(title: String, content: String, author: String) {
        val postID = database.child("posts").push().key // Generate unique ID for the post
        val timestamp = System.currentTimeMillis()

        val postMap = hashMapOf(
            "title" to title,
            "content" to content,
            "author" to author,
            "timestamp" to timestamp
        )

        val childUpdates = hashMapOf<String, Any>(
            "/posts/$postID" to postMap
        )

        database.updateChildren(childUpdates)
    }

    // Function to add a new comment to a post in the database
    fun addComment(postID: String, content: String, author: String) {
        val commentID = database.child("posts").child(postID).child("comments").push().key // Generate unique ID for the comment
        val timestamp = System.currentTimeMillis()

        val commentMap = hashMapOf(
            "content" to content,
            "author" to author,
            "timestamp" to timestamp
        )

        val childUpdates = hashMapOf<String, Any>(
            "/posts/$postID/comments/$commentID" to commentMap
        )

        database.updateChildren(childUpdates)
    }

    // Function to retrieve all posts from the database
    fun getAllPosts(callback: (List<Post>) -> Unit) {
        val postsList = mutableListOf<Post>()

        database.child("posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let { postsList.add(it) }
                }
                callback(postsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    // Function to retrieve all comments for a specific post from the database
    fun getCommentsForPost(postID: String, callback: (List<Comment>) -> Unit) {
        val commentsList = mutableListOf<Comment>()

        database.child("posts").child(postID).child("comments").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (commentSnapshot in dataSnapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    comment?.let { commentsList.add(it) }
                }
                callback(commentsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}

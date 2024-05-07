package com.example.socialmedia

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageBoxActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var currentUser: User
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_box)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        currentUser = getCurrentUser()
        loadUsersChattedWith()
        loadUsersFromDatabase()





    }

    private fun getCurrentUser(): User {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return User(
            userId = firebaseUser!!.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: ""
        )
    }



    private fun loadUsersChattedWith() {
        val currentUserID = getCurrentUserID()
        val messagesRef = FirebaseDatabase.getInstance().getReference("messages")

        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usersChattedWith = mutableSetOf<String>()

                for (receiverSnapshot in dataSnapshot.children) {
                    for (senderSnapshot in receiverSnapshot.children) {
                        for (conversationSnapshot in senderSnapshot.children) {
                            val receiverId = conversationSnapshot.child("receiverId")
                                .getValue(String::class.java)
                            val senderId = conversationSnapshot.child("senderId")
                                .getValue(String::class.java)

                            // Determine the ID of the other user in the conversation
                            val otherUserId =
                                if (receiverId == currentUserID) senderId else receiverId

                            // Exclude current user from the list
                            if (otherUserId != currentUserID) {
                                usersChattedWith.add(otherUserId!!)
                            }
                        }
                    }
                }

                // Fetch user details for each unique user ID
                val usersChattedWithList = mutableListOf<User>()
                usersChattedWith.forEach { userId ->
                    fetchUserDetails(userId) { user ->
                        user?.let {
                            usersChattedWithList.add(it)
                            Log.e(TAG, "${it.userId} ${it.email} ${it.displayName}")
                        }
                    }
                }

                // Initialize and set adapter
                adapter = UserAdapter(usersChattedWithList)
                recyclerView.adapter = adapter

                // Set item click listener
                adapter.setOnItemClickListener(object : UserAdapter.OnItemClickListener {
                    override fun onItemClick(user: User) {
                        startMessageActivity(user)
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
            // Handle database error
            }
        })
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

    private fun startMessageActivity(user: User) {
        val intent = Intent(this@MessageBoxActivity, MessageActivity::class.java)
        intent.putExtra("userId", user.userId)
        intent.putExtra("email", user.email)
        intent.putExtra("displayName", user.displayName)
        startActivity(intent)
    }

    private fun getCurrentUserID(): String {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser!!.uid
    }

    private fun loadUsersFromDatabase() {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList = mutableListOf<User>()

                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key
                    val email = userSnapshot.child("email").getValue(String::class.java)
                    val displayName = userSnapshot.child("displayName").getValue(String::class.java)

                    // Create User object
                    val user = User(userId ?: "", email ?: "", displayName ?: "")
                    userList.add(user)
                }

                // Update the adapter with the fetched user list
                // Set up SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        adapter = UserAdapter(userList)
                        recyclerView.adapter = adapter
                        adapter.filter.filter(newText)

                        adapter.setOnItemClickListener(object : UserAdapter.OnItemClickListener {
                            override fun onItemClick(user: User) {
                                startMessageActivity(user)
                            }
                        })

                        return true
                    }
                })


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }




}


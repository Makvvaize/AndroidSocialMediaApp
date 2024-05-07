package com.example.socialmedia

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageActivity : AppCompatActivity() {
    private lateinit var user: User
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var currentUser: User
    private var messages: MutableList<Message> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // Retrieve user details from Intent extras
        val userId = intent.getStringExtra("userId")
        val email = intent.getStringExtra("email")
        val displayName = intent.getStringExtra("displayName")

        // Create a User object from the retrieved details
        val user = User(userId, email, displayName)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        messageAdapter = MessageAdapter(messages)
        recyclerView.adapter = messageAdapter



        currentUser = getCurrentUser()
        loadMessages()

        buttonSend.setOnClickListener {
            val messageContent = editTextMessage.text.toString()
            if (messageContent.isNotEmpty()) {
                sendMessage(messageContent)
                editTextMessage.text.clear()
            }
        }
    }

    private fun getCurrentUser(): User {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return User(
            userId = firebaseUser!!.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: ""
        )
    }


    private fun loadMessages() {
        // Get the user IDs of the current user and the selected user
        val currentUserID = getCurrentUserID()
        val selectedUserID = intent.getStringExtra("userId")

        // Construct the path to the messages node for this conversation
        val messagesPath = if (currentUserID < selectedUserID.toString()) {
            "/messages/$currentUserID/$selectedUserID"
        } else {
            "/messages/$selectedUserID/$currentUserID"
        }

        val messagesRef = FirebaseDatabase.getInstance().getReference(messagesPath)

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()

                for (messageSnapshot in dataSnapshot.children) {
                    val senderName= messageSnapshot.child("senderDisplayName").getValue(String::class.java)
                    val content = messageSnapshot.child("content").getValue(String::class.java)
                    val timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java)

                    content?.let { msgContent ->
                        timestamp?.let { msgTimestamp ->
                            val message = Message(
                                currentUserID,
                                selectedUserID!!,
                                senderName,
                                msgContent,
                                msgTimestamp
                            )
                            messages.add(message)
                        }
                    }
                }

                updateMessages(messages)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun updateMessages(messages: List<Message>) {
        this.messages.clear()
        this.messages.addAll(messages)
        messageAdapter.notifyDataSetChanged()
    }

    private fun sendMessage(content: String) {
        val authorId = getCurrentUserID()
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        val usersRef: DatabaseReference = databaseReference.child("users").child(authorId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userDisplayName = dataSnapshot.child("displayName").getValue(String::class.java)


                    val currentUserID = getCurrentUserID()
                    val selectedUserID = intent.getStringExtra("userId")
                    val messagesPath = if (currentUserID < selectedUserID.toString()) {
                        "/messages/$currentUserID/$selectedUserID"
                    } else {
                        "/messages/$selectedUserID/$currentUserID"
                    }

                    val messagesRef = FirebaseDatabase.getInstance().getReference(messagesPath)
                    val timestamp = System.currentTimeMillis()
                    val message = Message(currentUserID, selectedUserID!!,userDisplayName, content, timestamp)
                    val newMessageRef = messagesRef.push()
                    newMessageRef.setValue(message)
                        .addOnSuccessListener {
                            // Message sent successfully
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Failed to send message: $exception")
                        }



                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })



    }

    private fun getCurrentUserID(): String {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser!!.uid
    }
}


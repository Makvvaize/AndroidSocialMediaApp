package com.example.socialmedia

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import javax.annotation.Nonnull
import android.net.Uri
import android.widget.EditText
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

//import kotlinx.android.synthetic.main.activity_create_post.*

class CreatePostActivity<Uri : Any> : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonPublish: Button
    private lateinit var imageViewSelectedImage: ImageView
    private lateinit var editTextPostTitle: EditText
    private lateinit var editTextPostContent: EditText


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        editTextPostTitle = findViewById(R.id.editTextPostTitle)
        editTextPostContent = findViewById(R.id.editTextPostContent)
        buttonPublish = findViewById(R.id.buttonPublish)
        imageViewSelectedImage = findViewById(R.id.imageViewSelectedImage)



        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSelectImage.setOnClickListener {
            // Open image picker or camera when the button is clicked
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> dispatchTakePictureIntent()
                1 -> dispatchPickImageIntent()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun dispatchPickImageIntent() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    val imageUri = getImageUriFromBitmap(this, imageBitmap)

                    imageViewSelectedImage.setImageBitmap(imageBitmap)

                    buttonPublish.setOnClickListener{
                        uploadMediaToStorage(imageUri)
                        val PublishIntent = Intent(this, MainActivity::class.java)
                        startActivity(PublishIntent)
                        finish()

                    }


                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    // Call the function to upload the selected/captured image to Firebase Storage
                    if (imageUri != null) {

                        Glide.with(this)
                            .load(imageUri)
                            .into(imageViewSelectedImage)

                        buttonPublish.setOnClickListener{
                            uploadMediaToStorage(imageUri)
                            val PublishIntent = Intent(this, MainActivity::class.java)
                            startActivity(PublishIntent)
                            finish()

                        }

                    }
                }
            }
        }


    }




    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): android.net.Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return android.net.Uri.parse(path)
    }

    private fun uploadMediaToStorage(mediaUri: android.net.Uri) {
        val storageReference = FirebaseStorage.getInstance().getReference("media")
        val fileReference = storageReference.child("media_${System.currentTimeMillis()}")

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        val postId = databaseReference.child("posts").push().key

        fileReference.putFile(mediaUri)
            .addOnSuccessListener { taskSnapshot ->
                // Media upload successful, get the download URL
                fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Proceed to create the post object with media URL
                    if (postId != null) {
                        createPostWithMedia(downloadUri.toString(),editTextPostTitle.text.toString(), editTextPostContent.text.toString() , postId)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to upload media
                Log.e(TAG, "Failed to upload media: $exception")
            }
    }

    private fun createPostWithMedia(mediaUrl: String ,title: String, content: String , postID:String) {
        val title = editTextPostTitle.text.toString()
        val content = editTextPostContent.text.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val authorId = currentUser?.uid
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        val usersRef: DatabaseReference = databaseReference.child("users").child(authorId!!)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
             override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userDisplayName =
                        dataSnapshot.child("displayName").getValue(String::class.java)

                    val likesMap: MutableMap<String, Boolean> = mutableMapOf()

                    val post = Post( postID,title, content, authorId , userDisplayName, mediaUrl,likesMap,System.currentTimeMillis())


                    storePostInDatabase(post ,postID)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        buttonPublish.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }

    }

    private fun storePostInDatabase(post: Post ,postID:String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("posts")
        //val postId = databaseReference.child("posts").push().key

        postID.let { id ->
            databaseReference.child(id).setValue(post)
                .addOnSuccessListener {
                    // Post data stored successfully
                }
                .addOnFailureListener { exception ->
                    // Handle failure to store post data
                    Log.e(TAG, "Failed to store post data: $exception")
                }
        }
    }



}

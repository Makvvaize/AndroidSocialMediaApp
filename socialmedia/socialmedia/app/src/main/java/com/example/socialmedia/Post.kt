package com.example.socialmedia

import android.os.Parcel
import android.os.Parcelable

data class Post(
    val postId: String? = "",
    val title: String? = "",
    val content: String? = "",
    val authorId: String? = "",
    val authorDisplayName: String? = "",
    val mediaUrl: String? = "",
    var likes: MutableMap<String, Boolean> = mutableMapOf(),
    val timestamp: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        mutableMapOf<String, Boolean>().apply {
            parcel.readMap(this, Boolean::class.java.classLoader)
        },
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeString(authorId)
        parcel.writeString(authorDisplayName)
        parcel.writeString(mediaUrl)
        parcel.writeMap(likes) // Write the likes map to the parcel
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}

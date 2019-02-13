package edu.rosehulman.crawfoaj.clubcalender

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

data class User(var username: String = "", var interestedClubs: ArrayList<String> = arrayListOf(), var managedClubs: ArrayList<String> = arrayListOf()) {
    @get:Exclude
    var id = ""

    companion object {
        val KEY = "GetUsers"

        fun fromSnapshot(snapshot: DocumentSnapshot): User{
            val user = snapshot.toObject(User::class.java)!!
            user.id = snapshot.id
            return user
        }

    }



}
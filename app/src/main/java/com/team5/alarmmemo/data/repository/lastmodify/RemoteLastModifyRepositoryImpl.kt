package com.team5.alarmmemo.data.repository.lastmodify

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject

class RemoteLastModifyRepositoryImpl @Inject constructor(private val db:FirebaseFirestore):LastModifyRepository {
    private var userId = "default"

    fun setUserId(userId:String){
        this.userId = userId
    }

    override fun getLastModifyTime(uniqueId: String, callback: (Long) -> Unit) {
        db.collection("LastModify").document(userId).get().addOnSuccessListener {
            val time = it.get(uniqueId) as Long? ?:0L
            callback(time)
        }
    }

    override fun saveLastModifyTime(time: Long, uniqueId: String) {
        val data = mapOf(uniqueId to time)
        db.collection("LastModify").document(userId).set(data, SetOptions.merge())
    }
}
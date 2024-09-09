package com.team5.alarmmemo.data.repository.memo

import android.text.SpannableStringBuilder
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.presentation.memo.CheckItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteMemoDataRepositoryImpl @Inject constructor(private val db:FirebaseFirestore,private val gson:Gson):MemoDataRepository {

    private var userId = "default"

    fun setUserid(userId: String){
        this.userId = userId
        Log.d("아이디",userId)
    }


    override fun getList(callback: (ArrayList<Triple<String, String, SpannableStringBuilder>>) -> Unit) {
        db.collection("title").document(userId).get().addOnSuccessListener {
            if(it!=null&&it.exists()){
                val idTitle= it.data as HashMap<String, String>
                db.collection("Memo").document(userId).get().addOnSuccessListener {
                    if(it!=null&&it.exists()){
                        val idMemo = it.data as HashMap<String,String>
                        val result =ArrayList<Triple<String, String, SpannableStringBuilder>>()
                        for((k,v) in idTitle){
                            val json = idMemo.getOrDefault(k,null)
                            val span = gson.fromJson(json,SpannableStringBuilder::class.java)?:SpannableStringBuilder("")
                            result+=Triple(k,v,span)
                        }
                        callback(result)
                    }
                }
            }

        }
    }


    override fun saveAlarmSetting(alarmSetting: AlarmSetting, uniqueId: String) {
        if(userId=="default") return

        val data = mapOf(uniqueId to gson.toJson(alarmSetting,AlarmSetting::class.java))

        db.collection("AlarmSetting").document(userId).set(data, SetOptions.merge())
    }

    override fun getAlarmSetting(uniqueId: String, callback: (AlarmSetting) -> Unit) {
        if(userId=="default"){
            callback(AlarmSetting())
            return
        }
        db.collection("AlarmSetting").document(userId).get().addOnSuccessListener {
            if(it!=null&&it.exists()){
                val settings = gson.fromJson(it.getString(uniqueId),AlarmSetting::class.java)
                callback(settings)
            }else{
                callback(AlarmSetting())
            }
        }.addOnFailureListener {
            callback(AlarmSetting())
        }
    }

    override fun saveMemo(str: SpannableStringBuilder?, uniqueId: String) {
        if(userId=="default") return

        val data = mapOf(uniqueId to gson.toJson(str,SpannableStringBuilder::class.java))

        db.collection("Memo").document(userId).set(data, SetOptions.merge())
    }

    override fun getMemo(uniqueId: String, callback: (SpannableStringBuilder) -> Unit) {
        if(userId=="default"){
            callback(SpannableStringBuilder(""))
            return
        }
        db.collection("Memo").document(userId).get().addOnSuccessListener {
            if(it!=null&&it.exists()){
                val memo = gson.fromJson(it.getString(uniqueId),SpannableStringBuilder::class.java)
                callback(memo)
            }else{
                callback(SpannableStringBuilder(""))
            }
        }.addOnFailureListener {
            callback(SpannableStringBuilder(""))
        }
    }

    override fun saveDraw(drawList: List<CheckItem>, uniqueId: String) {
        if(userId=="default") return

        val data = mapOf(uniqueId to gson.toJson(drawList))

        db.collection("draw").document(userId).set(data, SetOptions.merge())
    }

    override fun getDraw(uniqueId: String, callback: (List<CheckItem>) -> Unit) {
        if(userId=="default") return

        db.collection("draw").document(userId).get().addOnSuccessListener {
            if(it!=null&&it.exists()){
                val draw = gson.fromJson(it.getString(uniqueId), Array<CheckItem>::class.java).toList()
                callback(draw)
            }else{
                callback(listOf())
            }
        }.addOnFailureListener {
            callback(listOf())
        }
    }

    override fun saveTitle(title: String, uniqueId: String) {
        if(userId=="default") return

        val data = mapOf(uniqueId to title)

        db.collection("title").document(userId).set(data, SetOptions.merge())
    }

    override fun getTitle(uniqueId: String, callback: (String) -> Unit) {
        if(userId=="default"){
            callback("")
            return
        }

        db.collection("title").document(userId).get().addOnSuccessListener {
            if(it!=null&&it.exists()){
                callback(it.getString(uniqueId) as String)
            }else{
                callback("")
            }
        }.addOnFailureListener {
            callback("")
        }
    }

    override fun getAllAlarms(callback: (List<AlarmSetting>,List<String>) -> Unit) {
        if(userId=="default"){
            callback(listOf(), listOf())
            return
        }
        db.collection("AlarmSetting").document(userId).get().addOnSuccessListener {
            if(it!=null&&it.exists()){
                val data = it.data?:HashMap<String,AlarmSetting>()
                val result1 = mutableListOf<AlarmSetting>()
                val result2 = mutableListOf<String>()
                for((k,v) in data){
                    val alarmSetting = gson.fromJson(v as String,AlarmSetting::class.java)
                    result1+=alarmSetting
                    result2+=k as String
                }
                callback(result1 as List<AlarmSetting>,result2 as List<String>)
            }else{
                callback(listOf(), listOf())
            }
        }.addOnFailureListener {
            callback(listOf(), listOf())
        }
    }

    override fun removeAlarmSetting(uniqueId: String) {
        db.collection("AlarmSetting").document(userId).update(uniqueId, FieldValue.delete())
    }

    override fun removeMemo(uniqueId: String) {
        db.collection("Memo").document(userId).update(uniqueId,FieldValue.delete())
    }

    override fun removeDraw(uniqueId: String) {
        db.collection("draw").document(userId).update(uniqueId,FieldValue.delete())
    }

    override fun removeTitle(uniqueId: String) {
        db.collection("title").document(userId).update(uniqueId,FieldValue.delete())
    }

    override fun removeIdContent(callback:(Boolean) -> Unit) = CoroutineScope(Dispatchers.IO).launch {

        val list = listOf(
            async { deleteDocument("AlarmSetting") },
            async { deleteDocument("Memo") },
            async { deleteDocument("draw") },
            async { deleteDocument("title") },
            async { deleteDocument("LastModify") }
        )

        val result = list.awaitAll()

        if(result.all{it}){
            callback(true)
        }else{
            callback(false)
        }

    }

    suspend fun deleteDocument(collection: String): Boolean {
        return try {
            db.collection(collection).document(userId).delete().await()
            true // 성공 시 true 반환
        } catch (e: Exception) {
            e.printStackTrace()
            false // 실패 시 false 반환
        }
    }

}
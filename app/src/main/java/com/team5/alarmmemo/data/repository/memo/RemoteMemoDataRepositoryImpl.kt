package com.team5.alarmmemo.data.repository.memo

import android.text.SpannableStringBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.data.model.MemoList
import com.team5.alarmmemo.data.model.MemoUnitData
import com.team5.alarmmemo.presentation.memo.CheckItem
import javax.inject.Inject

class RemoteMemoDataRepositoryImpl @Inject constructor(private val db:FirebaseFirestore,private val gson:Gson):MemoDataRepository {

    private var userId = "default"

    fun setUserid(userId: String){
        this.userId = userId
    }

    //한번에 가져오게 하기 위해서 각 컬렉션 외에도 메모와 타이틀은 uniq와 함께 관리한다.
    //json형태로 관리
    override fun getList(callback: (ArrayList<Triple<String, String, SpannableStringBuilder>>) -> Unit) {
        if(userId=="default"){
            callback(ArrayList())
            return
        }
        db.collection("UniqueIdList").document(userId).get().addOnSuccessListener {
            if(it!=null&&it.exists()){
                val data = it.toObject(MemoList::class.java)
                val result = ArrayList<Triple<String, String, SpannableStringBuilder>>()
                data?.memoLists?.forEach { list ->
                    val memo = gson.fromJson(list.memo, SpannableStringBuilder::class.java)
                    result+=Triple(list.uniqId,list.title,memo)
                }
                callback(result)
            }else{
                callback(ArrayList())
            }
        }.addOnFailureListener {
            callback(ArrayList())
        }
    }

    //0: 성공, 1:문서가 없음 => createList로가기, 2: 실패
    fun addList(list:MemoUnitData, callback:(Int)->Unit){
        if(userId=="default"){
            callback(0)
            return
        }
        db.collection("UniqueIdList").document(userId).update("memoLists", FieldValue.arrayUnion(list)).addOnFailureListener { e ->
            if(e is FirebaseFirestoreException&&e.code == FirebaseFirestoreException.Code.NOT_FOUND){
                callback(1)
            }else{
                callback(2)
            }
        }.addOnSuccessListener {
            callback(0)
        }
    }

    fun createList(callback:(Boolean)->Unit){
        db.collection("UniqueIdList").document(userId).set(MemoList()).addOnSuccessListener {
            callback(true)
        }.addOnFailureListener {
            callback(false)
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

}
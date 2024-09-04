package com.team5.alarmmemo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.team5.alarmmemo.data.model.User
import javax.inject.Inject

class RemoteUserDataRepositoryImpl @Inject constructor(private val db:FirebaseFirestore):RemoteUserDataRepository {
    override fun getByUserEmail(email: String, callback: (Boolean, String?) -> Unit) {
        if(email.isEmpty()){
            callback(false,"Empty email")
            return
        }
        db.collection("User").document(email).get().addOnSuccessListener {
            if(!it.exists()){
                callback(true,null)
            }else{
                callback(false,"already exist")
            }
        }.addOnFailureListener {
            callback(false,it.message)
        }
    }

    override fun addOrModifyUserData(user: User, callback: (Boolean, String?) -> Unit) {
        db.collection("User").document(user.email).set(user).addOnSuccessListener {
            callback(true,null)
        }.addOnFailureListener {
            callback(false,it.message)
        }
    }

    override fun deleteUserData(email: String, callback: (Boolean, String?) -> Unit) {
        db.collection("User").document(email).delete().addOnSuccessListener {
            callback(true,null)
        }.addOnFailureListener {
            callback(false,it.message)
        }
    }

    //소셜 로그인인 경우 회원가입이 필요없으므로 처음 소셜로그인이면 자동으로 아이디 생성해서 로그인하도록 함 - 소셜 로그인시 사용자 정보로 이름가져오기
    override fun Login(
        emailOrToken: String,
        password: String?,
        callback: (User?, String?) -> Unit
    ) {
        db.collection("User").document(emailOrToken).get().addOnSuccessListener {
            if(it.exists()){
                val user = it.toObject(User::class.java)
                if(password==null||password==user!!.password){
                    callback(user,null)
                }else{
                    callback(null,"wrong password")
                }
            }else{
                if(password==null){
                    callback(null, "Socail Login Init") //이 값을 받으면 바로 addOrModifyUserData로  계정 생성하고 로그인 재시도
                }else{
                    callback(null,"wrong id")
                }
            }
        }.addOnFailureListener {
            callback(null,it.message)
        }
    }

}
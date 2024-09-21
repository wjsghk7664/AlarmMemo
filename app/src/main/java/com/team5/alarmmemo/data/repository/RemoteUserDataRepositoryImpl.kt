package com.team5.alarmmemo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.Constants.USER
import com.team5.alarmmemo.util.AccountUtil.hashPassword
import javax.inject.Inject

class RemoteUserDataRepositoryImpl @Inject constructor(private val store: FirebaseFirestore) :
    RemoteUserDataRepository {

    // 이메일 중복확인
    override fun findUser(email: String, callback: (Boolean, String?, String?) -> Unit) {
        val query = store.collection(USER)
            .whereEqualTo("email", email).limit(1).get()

        query.addOnSuccessListener {
            if (!it.isEmpty) {
                val password = it.documents[0].getString("password")
                callback(true, password, null)
            } else {
                callback(false, null, null)
            }
        }.addOnFailureListener {
            callback(false, null, it.message)
        }
    }

    // 파이어스토어에 유저 정보 추가
    override fun resetPassword(email: String, newPassword: String, callback: (String?) -> Unit) {
        store.collection(USER).document(email).update("password", newPassword)
            .addOnSuccessListener {
                callback(null)
            }.addOnFailureListener {
                callback(it.message)
            }
    }

    // 유저 데이터 추가 및 수정
    override fun addOrModifyUserData(user: User, callback: (String?) -> Unit) {
        store.collection(USER).document(user.email).set(user).addOnSuccessListener {
            callback(null)
        }.addOnFailureListener {
            callback(it.message)
        }
    }


    // 유저 데이터 삭제
    override fun deleteUserData(email: String, callback: (String?) -> Unit) {
        store.collection(USER).document(email).delete().addOnSuccessListener {
            callback(null)
        }.addOnFailureListener {
            callback(it.message)
        }
    }

    //소셜 로그인인 경우 회원가입이 필요없으므로 처음 소셜로그인이면 자동으로 아이디 생성해서 로그인하도록 함 - 소셜 로그인시 사용자 정보로 이름 가져오기
    override fun login(
        emailOrToken: String,
        password: String?,
        callback: (User?, String?) -> Unit
    ) {
        store.collection(USER).document(emailOrToken).get().addOnSuccessListener {
            if (it.exists()) {
                val user = it.toObject(User::class.java)
                if (password == null || password == user!!.password) {
                    callback(user, null)
                } else {
                    callback(null, "wrong password")
                }
            } else {
                if (password == null) {
                    callback(
                        null,
                        "Social Login Init"
                    )
                } else {
                    callback(null, "wrong id")
                }
            }
        }.addOnFailureListener {
            callback(null, it.message)
        }
    }

}
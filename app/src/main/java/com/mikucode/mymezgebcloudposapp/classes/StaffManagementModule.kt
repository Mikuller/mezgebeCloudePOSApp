package com.mikucode.mymezgebcloudposapp.classes

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.collections.ArrayList

class StaffManagementModule(sharedPreferences: SharedPreferences, val context: Context) {
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val businessID = sharedPreferences.getString("businessID","")
    private val pathString = "${currentUser?.uid}/Businesses/${businessID}/zUsers"
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference(pathString)
    private val staffList: ArrayList<Staff> =  ArrayList()
    fun addStaff(staffName: String ,staffEmail: String, staffRole: String, callback: (Boolean) -> Unit) {
        val userId = databaseReference.push().key
        if (currentUser != null && userId != null) {
            val staff =  Staff( userId ,staffName,staffEmail, staffRole)
            databaseReference.child(userId).setValue(staff).addOnSuccessListener {
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
            callback(true)
        }
    }
    fun searchStaffWithEmail(email: String, callback: (String) -> Unit){
        if (currentUser != null) {
            databaseReference.orderByChild("staffEmail")
                .equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(postSnapshot in snapshot.children){
                                if (postSnapshot.exists()){
                                    callback(postSnapshot.key.toString())
                                    break
                                }
                            }
                        }else{
                            callback("")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
    private fun searchUserIDWithEmail(email: String, callback: (String) -> Unit){
        if (currentUser != null) {
            val dbRef = FirebaseDatabase.getInstance().reference
            dbRef.orderByChild("userEmail")
                .equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(postSnapshot in snapshot.children){
                                if (postSnapshot.exists()){
                                    callback(postSnapshot.key.toString())
                                    break
                                }
                            }
                        }else{
                            callback("")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

    }
    fun addBusinessToStaff(staffEmail: String,businessId : String,businessName: String, location: String , businessType: String, currency: String, creationTime: String,userPrivilege: String , callback: (Boolean) -> Unit) {

        if (currentUser != null) {
            var staffID = ""
            searchUserIDWithEmail(staffEmail){
                if(it!=""){
                    val dbRef = FirebaseDatabase.getInstance().reference
                    staffID = it
                    val business = Business(businessId,businessName.lowercase(),location,businessType,currency,creationTime,currentUser.uid, userPrivilege)
                    dbRef.child("${staffID}/Businesses").child(businessId).setValue(business).addOnSuccessListener {
                        callback (true)
                    }.addOnFailureListener {
                        callback(false)
                    }
                    callback(true)
                }else{
                    Toast.makeText(context, "${staffEmail} will see your business when they sign up!!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun removeStaff(staffID: String,staffEmail: String, callback: (Boolean) -> Unit){
        val newPathString = "$pathString/$staffID"
        val dbRef = FirebaseDatabase.getInstance().getReference(newPathString)
        //delete the business info under the staff user's node
        searchUserIDWithEmail(staffEmail) {
            if(it!=""){
                val userId=it
                dbRef.removeValue()
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference.child("$userId/Businesses").child(businessID!!).removeValue()
                            .addOnSuccessListener {
                            callback(true)
                            }

                    }
                    .addOnFailureListener {
                        callback(false)
                    }
                callback(true)
            }
        }


    }

    fun updateStaff(staffMap: Map<String,String>,staffID: String, callback: (Boolean) -> Unit){
        val newPathString = "$pathString/$staffID"
        val dbRef = FirebaseDatabase.getInstance().getReference(newPathString)
        //update the business info under the staff user's node
        searchUserIDWithEmail(staffMap["staffEmail"]!!.trim()) {
            if(it!=""){
                val userID = it
                dbRef.updateChildren(staffMap.toMap())
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference.child("$userID/Businesses").child(businessID!!)
                            .updateChildren(mapOf(Pair("userPrivilege",staffMap["staffRole"].toString()))).addOnSuccessListener {
                                callback(true)
                            }
                    }
                    .addOnFailureListener {
                        callback(true)
                    }
                callback(true)
            }
        }

    }
    fun getAllStaffMembers(callback: (ArrayList<Staff>) -> Unit){
        if (currentUser != null) {
            databaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(postSnapshot: DataSnapshot) {
                        if(postSnapshot.exists()){
                            for (snapshot in postSnapshot.children) {
                                        val staffData = snapshot.getValue(Staff::class.java)
                                        // Do something with the staff data
                                        if (staffData != null) {
                                            staffList.add(staffData)}
                            }
                        }
                        callback(staffList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
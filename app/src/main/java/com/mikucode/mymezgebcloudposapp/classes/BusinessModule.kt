package com.mikucode.mymezgebcloudposapp.classes

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BusinessModule(val sharedPreferences: SharedPreferences,val context: Context) {
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val pathString = "${currentUser?.uid}/Businesses"
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    val businessList : ArrayList<Business> = ArrayList()
    fun registerBusiness(businessName: String, location: String , businessType: String, currency: String, creationTime: String,userPrivilege: String , callback: (Boolean) -> Unit) {
        val businessId = databaseReference.child(pathString).push().key
        if (currentUser != null && businessId != null) {
            val business = Business(businessId,businessName.lowercase(),location,businessType,currency,creationTime,currentUser.uid, userPrivilege)
            databaseReference.child(currentUser.uid).updateChildren(mapOf(Pair("userEmail",currentUser.email)))
            databaseReference.child(pathString).child(businessId).setValue(business).addOnSuccessListener {
                callback (true)
            }.addOnFailureListener {
                callback(false)
            }
            callback(true)
        }

    }
    fun deleteBusiness(callback: (Boolean) -> Unit){
        popUpAlertDialog("Deleted"){
            if(it){
                val businessID = sharedPreferences.getString("businessID", "")
                val newPathString = "${currentUser?.uid}/Businesses/$businessID"
                val dbRef = FirebaseDatabase.getInstance().getReference(newPathString)
                dbRef.removeValue()
                    .addOnSuccessListener {
                        val editor = sharedPreferences.edit()
                        editor.clear()
                        editor.apply()
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
                callback(true)
            }
        }


    }

    fun updateBusiness(businessName: String, location: String , businessType: String, currency: String, creationTime: String, callback: (Boolean) -> Unit) {
        popUpAlertDialog("Updated"){
            if(it){
                val businessID = sharedPreferences.getString("businessID", "")
                val newPathString = "${currentUser?.uid}/Businesses/${businessID}"
                val dbRef = FirebaseDatabase.getInstance().getReference(newPathString)
                val business: Map<String, String> = mapOf(
                    Pair("businessName", businessName),
                    Pair("businessType", businessType),
                    Pair("creationTime", creationTime),
                    Pair("currency", currency),
                    Pair("location", location )
                )
                dbRef.updateChildren(business.toMap())
                    .addOnSuccessListener {
                        val editor = sharedPreferences.edit()
                        editor.putString("businessName", businessName )
                        editor.putString("currency" , currency)
                        editor.putString("businessType", businessType)
                        editor.putString("creationTime", creationTime)
                        editor.apply()
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
                callback(true)
            }}

    }
    // Function to get business with the same name
    fun getBusinessWithName(Name: String ,callback: (ArrayList<Business>) -> Unit) {

        if (currentUser != null) {
            databaseReference.child(pathString).orderByChild("businessName")
                .equalTo(Name.trim().lowercase())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        businessList.clear()
                        for (postSnapshot in snapshot.children) {
                            val business = postSnapshot.getValue(Business::class.java)
                            if (business != null) {
                                businessList.add(business)
                                break
                            }
                        }
                        callback(businessList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context,"error $error", Toast.LENGTH_SHORT).show()
                    }

                })
            databaseReference.keepSynced(true)
        }
    }
    private fun popUpAlertDialog(alertType:String, callback: (Boolean) -> Unit){
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are You sure? Business Will Be ${alertType} Permanently")
        builder.setPositiveButton("Yes") { dialog, which ->
            // Perform action when user clicks "Yes"
            // For example, show a toast message
            callback(true)
        }
        builder.setNegativeButton("No") { dialog, which ->
            // Perform action when user clicks "No"
            // For example, clear the text in the edit text
            callback(false)
        }
        builder.show()
    }
}
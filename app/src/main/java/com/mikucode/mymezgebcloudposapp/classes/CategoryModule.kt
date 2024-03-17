package com.mikucode.mymezgebcloudposapp.classes

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CategoryModule (val sharedPreferences: SharedPreferences, val context: Context) {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val currentBusiness = sharedPreferences.getString("businessID","")
    private val creatorID = sharedPreferences.getString("creatorID","")
    private val pathString = "${creatorID}/Businesses/${currentBusiness}/zCategories"
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference(pathString)
    val categoryList: ArrayList<Category> = ArrayList()
    fun addCategoryToDB(categoryName: String,callback: (Boolean) -> Unit) {
        val categoryID = databaseReference.push().key
        if (currentUser != null && categoryID != null) {
            val category = Category(categoryName,categoryID)
            databaseReference.child(categoryID).setValue(category).addOnSuccessListener {
                callback (true)
            }.addOnFailureListener {
                callback(false)
            }
            callback (true)
        }

    }
    fun getCategoryWithName(name: String, callback: (ArrayList<Category>) -> Unit) {

        if (currentUser != null) {
            databaseReference.orderByChild("categoryName")
                .equalTo(name.trim().lowercase())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        categoryList.clear()
                        for (postSnapshot in snapshot.children) {
                            val category = postSnapshot.getValue(Category::class.java)
                            if (category != null) {
                                categoryList.add(category)
                                break
                            }
                        }
                        callback(categoryList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }
    fun getCategoryWithID(categoryID: String, callback: (ArrayList<Category>) -> Unit) {

        if (currentUser != null) {
            databaseReference.orderByChild("categoryID")
                .equalTo(categoryID.trim())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        categoryList.clear()
                        for (postSnapshot in snapshot.children) {
                            val category = postSnapshot.getValue(Category::class.java)
                            if (category != null) {
                                categoryList.add(category)
                                break
                            }
                        }
                        callback(categoryList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }
    fun getAllCategories(callback: (ArrayList<Category>) -> Unit){
        if (currentUser != null) {
            databaseReference
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        categoryList.clear()
                        for (postSnapshot in snapshot.children) {
                            val category = postSnapshot.getValue(Category::class.java)
                            if (category != null) {
                                categoryList.add(category)
                            }
                        }
                        callback(categoryList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }
    fun updateCategory (categoryName: String, categoryID: String,callback: (Boolean) -> Unit) {
        popUpAlertDialog("Updated"){response->
            if(response){
                val newPathString = "$pathString/$categoryID"
                val dbRef = FirebaseDatabase.getInstance().reference
                val category: Map<String, String> = mapOf(
                    Pair("categoryID", categoryID),
                    Pair("categoryName", categoryName))
                dbRef.child(newPathString).updateChildren(category.toMap())
                    .addOnSuccessListener {
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
                callback (true)
            }

        }

        }
    fun deleteCategory(categoryID: String, callback: (Boolean) -> Unit){
        popUpAlertDialog("Deleted"){ response ->
            if(response) {
                val newPathString = "$pathString/$categoryID"
                val dbRef = FirebaseDatabase.getInstance().getReference(newPathString)
                dbRef.removeValue()
                    .addOnSuccessListener {
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
                callback(true)
            }
       }
    }
    fun checkIfCategoryHasItem(categoryID: String, callback: (Boolean) -> Unit){
        val itemModule = ItemModule(sharedPreferences,context)
        itemModule.getAllItems {
            val items  = it
            if(items.isNotEmpty()){
                for(item in items){
                    if(item.category == categoryID){
                        callback(true)
                        break
                    }
                }
            }else{
                callback(false)
            }
        }
    }

    fun popUpAlertDialog(alertType:String ,callback: (Boolean) -> Unit){
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are You sure? Item Will Be ${alertType} Permanently")
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
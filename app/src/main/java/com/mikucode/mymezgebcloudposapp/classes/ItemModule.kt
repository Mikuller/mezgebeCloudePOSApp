package com.mikucode.mymezgebcloudposapp.classes

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ItemModule (val sharedPreferences: SharedPreferences, val context: Context)
{
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val currentBusiness = sharedPreferences.getString("businessID","")
    private val creatorID = sharedPreferences.getString("creatorID","")

    private val currentUserPrivilege = sharedPreferences.getString("userPrivilege","")
    private val pathString = "${creatorID}/Businesses/${currentBusiness}/zItems"
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference(pathString)
    val itemList: ArrayList<Item> = ArrayList()
    var itemKEY  = ""
    fun addItemToDB(itemName: String, itemCategory: String, sellByOpt: String, sellingPrice: Float, costPrice: Float, stockValue: Float, currentDateTime: String,callback: (Boolean) -> Unit) {
        val ItemID = databaseReference.push().key
        if (currentUser != null && ItemID != null) {
            val Item = Item(itemName,itemCategory,sellByOpt,sellingPrice,costPrice,stockValue,currentDateTime,ItemID)
            databaseReference.child(ItemID).setValue(Item).addOnSuccessListener {
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
            callback(true)

        }

    }
    fun addItemVariantToDB(itemID: String, itemMap: Map<String, Any>, callback: (Boolean) -> Unit) {

        if (currentUser != null) {
        val dbRef = FirebaseDatabase.getInstance().reference
        val newVariantItemID = dbRef.child("$pathString/$itemID/Variants").push().key
        val variantID = dbRef.child("$pathString/$newVariantItemID/Variants").push().key

        val newPathString : String
            if( itemID == ""){//the variant is new and no item was added
            newPathString = "$pathString/$newVariantItemID/Variants/$variantID"

            dbRef.child("${pathString}/$newVariantItemID").updateChildren(mapOf(Pair("itemName",itemMap["itemName"])).toMap())
            dbRef.child(newPathString).setValue(itemMap.plus(Pair("itemID",variantID)).toMap()).addOnSuccessListener {
                    callback(true)
                }.addOnFailureListener {
                    callback(false)
                }
                callback(true)
            }else{
            newPathString = "$pathString/$itemID/Variants/$variantID"

            dbRef.child(newPathString).setValue(itemMap.plus(Pair("itemID",variantID)).toMap()).addOnSuccessListener {
                    callback(true)
                }.addOnFailureListener {
                    callback(false)
                }
                callback(true)
            }
        }
    }

    fun getItemKeyFromName(itemName: String, callback: (String) -> Unit) {

        if (currentUser != null) {
            databaseReference.orderByChild("itemName")
                .equalTo(itemName.trim().lowercase())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                        for(postSnapshot in snapshot.children){
                            if (postSnapshot.exists()){
                                itemKEY = postSnapshot.key.toString()
                                callback(itemKEY)

                            }else{
                                callback("")
                            }
                        }
                        }else{
                            callback("")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }
    fun getVariantKeyFromName(itemID: String,variantName: String, callback: (String) -> Unit) {

        if (currentUser != null) {
            val dbRef = FirebaseDatabase.getInstance().reference
            val newPath = "$pathString/$itemID/Variants"
            dbRef.child(newPath).orderByChild("variantName")
                .equalTo(variantName.trim().lowercase())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(postSnapshot in snapshot.children){
                                if (postSnapshot.exists()){
                                    itemKEY = postSnapshot.key.toString()
                                    callback(itemKEY)
                                }else{
                                    callback("")
                                }
                            }
                        }else{
                            callback("")
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    fun getVariantFromName(itemID: String, variantName: String, callback: (ArrayList<Item>) -> Unit) {
        if (currentUser != null) {
            val newPathString = "$pathString/$itemID/Variants"
            val dbRef = FirebaseDatabase.getInstance().reference
            dbRef.child(newPathString).orderByChild("variantName")
                .equalTo(variantName.trim().lowercase())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        itemList.clear()
                        for (postSnapshot in snapshot.children) {
                            val Item = postSnapshot.getValue(Item::class.java)
                            if (Item != null) {
                                itemList.add(Item)
                                break
                            }
                        }
                        callback(itemList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }

    }
    fun getItemFromName(itemName: String, callback: (ArrayList<Item>) -> Unit) {
        if (currentUser != null) {
            databaseReference.orderByChild("itemName")
                .equalTo(itemName.trim().lowercase())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        itemList.clear()
                        for (postSnapshot in snapshot.children) {
                            val Item = postSnapshot.getValue(Item::class.java)
                            if (Item != null) {
                                itemList.add(Item)
                                break
                            }
                        }
                        callback(itemList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    fun getAllItems(callback: (ArrayList<Item>) -> Unit){
        if (currentUser != null) {
            databaseReference
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(postSnapshot: DataSnapshot) {
                        if(postSnapshot.exists()){
                            for (snapshot in postSnapshot.children) {
                                if(snapshot.hasChild("Variants")){
                                val variantSnapshot = snapshot.child("Variants")
                                    for (variant in variantSnapshot.children) {
                                        val variantData = variant.getValue(Item::class.java)
                                        // Do something with the variant data
                                        if (variantData != null) {
                                        itemList.add(variantData)
                                           } }
                                }
                                else{
                                        val Item = snapshot.getValue(Item::class.java)
                                        if (Item != null) {
                                            itemList.add(Item)
                                        }
                                    }
                            }
                        }
                        callback(itemList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    fun updateItem (itemMap: Map<String,Any> , itemID: String ,callback: (Boolean) -> Unit) {
       //check what property is being updated, if map contains more than one pair then its from mangeItemDetails Activity
        //if item map size is only one then its from sales module and its for decrementing the item stock value
        //if the purpose is to decrement stock value don't pop up alert dialog
        if(itemMap.size!=1){
            popUpAlertDialog("Updated"){
                if(it){
                    val newPathString = "${pathString}/$itemID"
                    val dbRef = FirebaseDatabase.getInstance().getReference(newPathString)

                    dbRef.updateChildren(itemMap.toMap())
                        .addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener {
                            callback(false)
                        }
                    callback(true)
                }
            }
        }else{
            val newPathString = "${pathString}/$itemID"
            val dbRef = FirebaseDatabase.getInstance().getReference(newPathString)

            dbRef.updateChildren(itemMap.toMap())
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
            callback(true)
        }

    }
    fun deleteItem(itemID: String, callback: (Boolean) -> Unit){
        popUpAlertDialog("Deleted"){
            if(it){
                val newPathString = "$pathString/$itemID"
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

    fun deleteVariantItem(itemID: String, variantItemID: String , callback: (Boolean) -> Unit){
        popUpAlertDialog("Deleted"){
            if(it){
                val newPathString = "$pathString/$itemID/Variants/$variantItemID"
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
    fun updateVariantItem (itemMap: Map<String,Any> , itemID: String, variantItemID: String,callback: (Boolean) -> Unit) {
        //check what property is being updated, if map contains more than one pair then its from mangeItemDetails Activity
        //if item map size is only one then its from sales module and its for decrementing the item stock value
        //if the purpose is to decrement stock value don't pop up alert dialog
        if(itemMap.size!=1){
            popUpAlertDialog("Updated"){
                if(it){
                    val newPathString = "$pathString/$itemID/Variants/$variantItemID"
                    val dbRef = FirebaseDatabase.getInstance().reference
                    dbRef.child(newPathString).updateChildren(itemMap.toMap())
                        .addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener {

                            callback(true)
                        }
                    callback(true)
                }}
        }else{
            val newPathString = "$pathString/$itemID/Variants/$variantItemID"
            val dbRef = FirebaseDatabase.getInstance().reference
            dbRef.child(newPathString).updateChildren(itemMap.toMap())
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(true)
                }
            callback(true)
        }


    }
    fun getItemVariantFromBarCode(barCode: String, callback: (Item?) -> Unit){
        var resultItem: Item? = null
        if (currentUser != null) {
            databaseReference
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(postSnapshot: DataSnapshot) {
                        if(postSnapshot.exists()){
                            for (snapshot in postSnapshot.children) {
                                if(snapshot.hasChild("Variants")){
                                    val variantSnapshot = snapshot.child("Variants")
                                    for (variant in variantSnapshot.children) {
                                        val variantData = variant.getValue(Item::class.java)
                                        // Do something with the variant data
                                        if (variantData != null) {
                                            if (variantData.barCode == barCode) {
                                                resultItem = variantData
                                                break
                                            }
                                        } }
                                   // callback(resultItem)
                                }
                              //  callback(null)
                            }
                        }
                        callback(resultItem)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
        }

    private fun popUpAlertDialog(alertType:String, callback: (Boolean) -> Unit){
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

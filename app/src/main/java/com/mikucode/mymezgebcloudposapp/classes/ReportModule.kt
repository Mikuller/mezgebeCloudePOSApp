package com.mikucode.mymezgebcloudposapp.classes

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ReportModule(sharedPreferences: SharedPreferences, val context: Context) {
    private val itemModule =  ItemModule(sharedPreferences, context)
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val businessID = sharedPreferences.getString("businessID","")
    private val creatorID = sharedPreferences.getString("creatorID","")
    private val pathString = "${creatorID}/Businesses/${businessID}/zItems"
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference(pathString)
    var items: ArrayList<Item> = ArrayList()
    var expiredItems: ArrayList<Item> = ArrayList()
    var lowStockItems: ArrayList<Item> = ArrayList()
    fun getLowStockInventory(allItems: ArrayList<Item>): ArrayList<Item>{
        if (currentUser != null) {
            if(allItems.isNotEmpty()){
                // Fetch only items whose stock count is less than 3pcs
                for(item in allItems){
                    if (item.stockValue <= 3) {
                        lowStockItems.add(item)
                    }
                }
            }

        }
        return lowStockItems
    }

    fun getAllRemainingStockCount(context: Context, callback: (ArrayList<Item>) -> Unit) {
        if (currentUser != null) {
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    items.clear()
                    for (itemSnapshot in dataSnapshot.children) {

                        if (itemSnapshot.hasChild("Variants")) {
                            val variantSnapshot = itemSnapshot.child("Variants")
                            for (variant in variantSnapshot.children) {
                                val variantData = variant.getValue(Item::class.java)
                                // Do something with the variant data
                                if (variantData != null) {
                                    items.add(variantData)
                                }
                            }
                        } else {
                            val itemData = itemSnapshot.getValue(Item::class.java)
                            if (itemData != null) {
                                items.add(itemData)
                            }
                        }

                    }
                    callback(items)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Error While getting Items", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    // Function to track expiry date of an item
    fun getExpiredItems(allItems: ArrayList<Item>): ArrayList<Item> {
        if(allItems.isNotEmpty()){
            for (item1 in allItems){
                if(item1.expiryDate != ""){
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val expiryDate = formatter.parse(item1.expiryDate)
                    val today = Date()
                    val daysLeft = (((expiryDate?.time ?: 0) - today.time) / (1000 * 60 * 60 * 24)).toInt()
                    if (daysLeft <= item1.expiryReminderDate){
                        expiredItems.add(item1)
                    }
                }
            }
        }
        return expiredItems
    }

    fun checkStockStatusForAnItem(itemName: String, variantName: String, quantity: Int){
       if (variantName!=""){//it's a variant item
           itemModule.getItemKeyFromName(itemName){itemKEY->
               if(itemKEY!=""){
                   itemModule.getVariantFromName(itemKEY,variantName){
                       if (it.isNotEmpty()){
                           val item1 = it[0]
                           itemModule.getVariantKeyFromName(itemKEY,variantName){variantKEY->
                               if(variantKEY!=""){
                                   val remainingStock = item1.stockValue-quantity
                                   itemModule.updateVariantItem(mapOf(Pair("stockValue", remainingStock)),itemKEY,variantKEY){ rslt->
                                       if(rslt)  {
                                           if (remainingStock<=1){
                                               Toast.makeText(context,
                                                   "ITEM ${item1.itemName} ${item1.variantName} IS OUT OF STOCK, CHECK YOUR INVENTORY",
                                                   Toast.LENGTH_LONG).show()
                                           }
                                       }
                                   }

                               }
                           }

                       }
                   }
               }
           }
       }else{
           itemModule.getItemFromName(itemName){items->
               val remainingStock = items[0].stockValue-quantity
               val item1 = items[0]
           itemModule.getItemKeyFromName(itemName){itemKEY->
               if (itemKEY!=""){
                   itemModule.updateItem(mapOf(Pair("stockValue", remainingStock)),itemKEY){
                       if(it){
                               if (remainingStock<=1){
                                   Toast.makeText(context,
                                       "ITEM ${item1.itemName} IS OUT OF STOCK,PLEASE CHECK YOUR INVENTORY",
                                       Toast.LENGTH_LONG).show()
                               }
                       }
                   }
               }
           }}
       }

    }



}
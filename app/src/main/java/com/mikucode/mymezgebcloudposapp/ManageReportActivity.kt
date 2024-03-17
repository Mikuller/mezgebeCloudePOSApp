package com.mikucode.mymezgebcloudposapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.mikucode.mymezgebcloudposapp.classes.Item
import com.mikucode.mymezgebcloudposapp.classes.ReportModule

class ManageReportActivity : AppCompatActivity() {

    private lateinit var itemListAdapter: LowStockItemsListAdapter
    private lateinit var itemListRV: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var ReportModule: ReportModule
    private lateinit var parentLayout: RelativeLayout
    private var itemList: ArrayList<Item> = ArrayList()
    private var expiredItems: ArrayList<Item> = ArrayList()
    private var lowStockItems: ArrayList<Item> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_stock_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        itemListRV = findViewById<RecyclerView>(R.id.lowStockItemsRV)
        sharedPreferences = this@ManageReportActivity.getSharedPreferences("myPref", Context.MODE_PRIVATE)
        ReportModule = ReportModule(sharedPreferences,this)


        itemListAdapter = LowStockItemsListAdapter(this@ManageReportActivity, itemList, intent)
        itemListRV.adapter = itemListAdapter
        itemListRV.layoutManager = LinearLayoutManager(this, VERTICAL, false)

        parentLayout = findViewById(R.id.parentRL_Layout)


    }

    override fun onStart() {

        val intentVal = intent.getBooleanExtra("remainingStock",false)//this variable determines what to show
        val intentVal2 = intent.getBooleanExtra("expiredItems",false)
        if(intentVal){//show all remaining items
            supportActionBar?.title = "All Remaining Stock Amount"
            ReportModule.getAllRemainingStockCount(this@ManageReportActivity) { items: ArrayList<Item> ->
                if (items.isNotEmpty()) {
                    itemList.clear()
                    itemList.addAll(items)
                }
                else{
                    parentLayout.removeAllViews()
                    val nothingFoundView = LayoutInflater.from(this).inflate(R.layout.nothing_found_lay_out, parentLayout)
                    Toast.makeText(this, "NO Item Registered",Toast.LENGTH_SHORT).show()
                }
            }

        }else if(intentVal2){
            supportActionBar?.title = "Expiring Items"

            ReportModule.getAllRemainingStockCount(this@ManageReportActivity) { items: ArrayList<Item> ->
                if (items.isNotEmpty()) {

                   expiredItems.addAll(ReportModule.getExpiredItems(items))

                    if (expiredItems.isNotEmpty()) {
                        itemList.clear()
                        itemList.addAll(expiredItems)

                    }
                    else{
                        parentLayout.removeAllViews()
                        val nothingFoundView = LayoutInflater.from(this).inflate(R.layout.nothing_found_lay_out, parentLayout)
                        Toast.makeText(this, "GOOD, NO Item is Expiring",Toast.LENGTH_SHORT).show()
                    }

                }else{
                    parentLayout.removeAllViews()
                    val nothingFoundView = LayoutInflater.from(this).inflate(R.layout.nothing_found_lay_out, parentLayout)
                }
            }


        }

        else{//show low stock items
            supportActionBar?.title = "Low Stock Inventory Items"

            ReportModule.getAllRemainingStockCount(this@ManageReportActivity){ items->
                if(items.isNotEmpty()){
                    lowStockItems.addAll(ReportModule.getLowStockInventory(items))
                    if(lowStockItems.isNotEmpty()){
                        itemList.clear()
                        itemList.addAll(lowStockItems)
                    }
                    else{
                        parentLayout.removeAllViews()
                        val nothingFoundView = LayoutInflater.from(this).inflate(R.layout.nothing_found_lay_out, parentLayout)
                        Toast.makeText(this, "NO Item is Out of Stock",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    parentLayout.removeAllViews()
                    val nothingFoundView = LayoutInflater.from(this).inflate(R.layout.nothing_found_lay_out, parentLayout)
                    Toast.makeText(this, "NO Item is Added",Toast.LENGTH_SHORT).show()
                }

            }
        }
        super.onStart()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
class LowStockItemsListAdapter(var context: Context, private var stockItemsList: ArrayList<Item>, val intent: Intent): RecyclerView.Adapter<LowStockItemsListAdapter.ItemVH>(){


    class ItemVH(itemView: View): ViewHolder(itemView){
        var itemName = itemView.findViewById<TextView>(R.id.itemId)
        var stockValue = itemView.findViewById<TextView>(R.id.itemStockValue)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
        val view = LayoutInflater.from(context).inflate(R.layout.list_stock_items_vh, parent,false)
        return ItemVH(view)
    }

    override fun onBindViewHolder(holder: ItemVH, position: Int) {
        holder.itemName.text = stockItemsList[position].itemName+" "+stockItemsList[position].variantName
        val intentValue = intent.getBooleanExtra("expiredItems",false)
        if(intentValue){
            holder.stockValue.text = stockItemsList[position].expiryDate
        }else{
            holder.stockValue.text = stockItemsList[position].stockValue.toString()
        }

    }

    override fun getItemCount(): Int {
        return stockItemsList.size
    }

}
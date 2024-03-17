package com.mikucode.mymezgebcloudposapp.adaptersAndViewModels

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.classes.Item

class SalesItemAdapter(private val context: Context, private val salesItemList: ArrayList<Item>,
                       private val itemCounterMap: HashMap<String,Int>, private val sharedPreferences: SharedPreferences): RecyclerView.Adapter<ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.counter_sales_item_cardview,parent,false)
        return SalesItemVH(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentSalesItem = salesItemList[position]
        val viewHolder = holder as SalesItemVH

            holder.itemName.text = currentSalesItem.itemName.plus(" ${currentSalesItem.variantName}")
            holder.numberOfItems.text = itemCounterMap[currentSalesItem.itemID].toString().plus("x")///this will get how many times a certain item type clicked
            holder.sellingPrice.text = currentSalesItem.sellingPrice.toString().plus( " ${sharedPreferences.getString("currency", null)}")
            holder.totalPrice.text = (currentSalesItem.sellingPrice * itemCounterMap[currentSalesItem.itemID]!!).toString().plus( " ${sharedPreferences.getString("currency", null)}")


    }

    override fun getItemCount(): Int {
      return salesItemList.size
    }
    class SalesItemVH(itemView: View) : ViewHolder(itemView) {
        val numberOfItems: TextView = itemView.findViewById(R.id.numberOfItemsTxt)
        val itemName: TextView = itemView.findViewById(R.id.paymentMode)
        val sellingPrice = itemView.findViewById<TextView>(R.id.unitPrice)
        val totalPrice = itemView.findViewById<TextView>(R.id.TotalItemSellingPrice)
    }

}
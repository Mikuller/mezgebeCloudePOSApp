package com.mikucode.mymezgebcloudposapp.adaptersAndViewModels

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.mikucode.mymezgebcloudposapp.ManageItemDetails
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.classes.Item

class ItemListAdapter(val context: Context, private val itemList: ArrayList<Item>, private val sharedPreferences: SharedPreferences, val sharedViewModel: SharedViewModel):RecyclerView.Adapter<ViewHolder>()  {

    private var LAST_VIEW_TYPE = 1
    private var Bfr_LAST_VIEW_TYPE  = 0
    private var clickCounter: HashMap<String,Int> = hashMapOf()
    var ctr: Int = 0
    class ItemListVH(itemView: View) : ViewHolder(itemView) {
        var itemName: TextView = itemView.findViewById(R.id.TxtItemName)
        var variantName: TextView = itemView.findViewById(R.id.variantNameTxt)
        var price: TextView = itemView.findViewById(R.id.sellingPriceNum)
        val clickCount: TextView = itemView.findViewById(R.id.ClickCount)

        fun observeClickCount(itemID: String, clickCounterLiveData: LiveData<HashMap<String,Int>>){

            clickCounterLiveData.observe(itemView.context as LifecycleOwner) {
                if (it[itemID] != null) {
                    clickCount.visibility = VISIBLE
                    clickCount.text = it[itemID].toString()
                } else {
                    clickCount.visibility = INVISIBLE
                }
            }

        }

    }

    class AddItemVH(itemView: View) : ViewHolder(itemView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            0 -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.item_list_cardview, parent, false)
                ItemListVH(view)
            }
            else -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.add_item_card_view, parent, false)
                AddItemVH(view)

            }

        }


    }


    override fun getItemViewType(position: Int): Int {
        return if(position >= itemList.size){//if it is the last element of itemList do the ff
            LAST_VIEW_TYPE
        }else{
            Bfr_LAST_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int {
       return itemList.size + 1 //1 is added to include the add item card view
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder::class.java) {
            ItemListVH::class.java -> {
                val viewHolder = holder as ItemListVH
                val currentItem = itemList[position]

                holder.itemName.text = currentItem.itemName
                holder.variantName.text = currentItem.variantName
                holder.price.text = currentItem.sellingPrice.toString().plus(" ${sharedPreferences.getString("currency", null)}")

                holder.observeClickCount(currentItem.itemID,sharedViewModel.clickedItemCounter)
                holder.itemView.setOnClickListener {
                    sharedViewModel.countClick(currentItem.itemID)
                    holder.observeClickCount(currentItem.itemID,sharedViewModel.clickedItemCounter)
                    sharedViewModel.addItem(currentItem)
                }
            }
            AddItemVH::class.java -> {
                val viewHolder = holder as AddItemVH
                if(sharedPreferences.getString("userPrivilege",null).equals("CLERK")){
                    //make user unable to add item without owner privilege
                    holder.itemView.visibility = View.INVISIBLE
                }else{
                    holder.itemView.setOnClickListener {

                        val intent = Intent(context,ManageItemDetails::class.java)
                        context.startActivity(intent)

                    }
                }
            }
        }
    }


}
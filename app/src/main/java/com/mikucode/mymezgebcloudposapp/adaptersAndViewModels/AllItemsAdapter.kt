package com.mikucode.mymezgebcloudposapp.adaptersAndViewModels

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.classes.Item

class AllItemListAdapter(var context: Context, private val generalItemList: MutableList<String>, private val groupedVariantItem: Map<String, List<Item>>): RecyclerView.Adapter<AllItemListAdapter.itemVH>(){

    class itemVH(itemView: View): RecyclerView.ViewHolder(itemView){
        var itemName = itemView.findViewById<TextView>(R.id.itemName)
        var stockValue = itemView.findViewById<TextView>(R.id.itemStockValue)
        var variantListPortion = itemView.findViewById<RelativeLayout>(R.id.itemVariantsList)
        var variantListRV = itemView.findViewById<RecyclerView>(R.id.itemVariantsRV)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): itemVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.show_items_and_variants_layout, parent,false)
        return itemVH(view)
    }

    override fun onBindViewHolder(holder: itemVH, position: Int) {
        holder.itemView.setBackgroundResource(R.drawable.purple_round_shape)
        holder.itemName.text = generalItemList[position]
        holder.stockValue.visibility = View.INVISIBLE
        val variantsInItem = groupedVariantItem[generalItemList[position]]

        if(variantsInItem!![0].variantName == ""){//if the item is single item not a variant
            holder.variantListPortion.visibility = View.INVISIBLE
        }else{
            val variantsListInnerAdapter = VariantsListInnerAdapter(context, variantsInItem)
            holder.variantListRV.apply {
                layoutManager = GridLayoutManager(context,3)
                adapter = variantsListInnerAdapter
            }
        }

    }

    override fun getItemCount(): Int {
        return generalItemList.size
    }

}
class VariantsListInnerAdapter(var context: Context, private var variantList: List<Item> ?): RecyclerView.Adapter<VariantsListInnerAdapter.VariantVH>(){
    class VariantVH(itemView: View): RecyclerView.ViewHolder(itemView) {
        var variantName: TextView = itemView.findViewById<TextView>(R.id.variantNameTxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.show_variant_name_for_inner_rv, parent, false)
        return VariantVH(view)
    }

    override fun onBindViewHolder(holder: VariantVH, position: Int) {
        holder.variantName.text = variantList?.get(position)?.variantName ?: "???"
    }

    override fun getItemCount(): Int {
        return variantList?.size ?: 0
    }

}
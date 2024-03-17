package com.mikucode.mymezgebcloudposapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.AllItemListAdapter
import com.mikucode.mymezgebcloudposapp.classes.Item
import com.mikucode.mymezgebcloudposapp.classes.ItemModule
import kotlin.collections.ArrayList

class AllItems : Fragment() {
    private lateinit var allItemRV: RecyclerView
    private lateinit var AllItemListAdapter: AllItemListAdapter
    private var itemList: ArrayList<Item> = ArrayList()
    private var generalItemsList : MutableList<String> = mutableListOf()
    private var groupedVariantItems : Map<String, List<Item>> = mutableMapOf()
    lateinit var sharedPreferences : SharedPreferences
    lateinit var itemModule: ItemModule
    private fun initialization(view: View) {
        sharedPreferences= requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE)
        allItemRV = view.findViewById(R.id.allItems)
        AllItemListAdapter = AllItemListAdapter(requireContext(), generalItemsList,groupedVariantItems)
        allItemRV.adapter = AllItemListAdapter
        allItemRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
        itemModule = ItemModule(sharedPreferences,requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_items, container, false)

        initialization(view)

        itemModule.getAllItems { items->
            if (items.isNotEmpty()){
                itemList.clear()
                groupedVariantItems = mutableMapOf()
                generalItemsList.clear()
                itemList.addAll(items)


                groupedVariantItems = itemList.groupBy { it.itemName }
                generalItemsList = groupedVariantItems.keys.toMutableList()
                AllItemListAdapter.notifyDataSetChanged()
                println("hello ${groupedVariantItems.size} ${generalItemsList.size}")

            }else{
                Snackbar.make(requireContext(),view ,"No Item Added Yet", Snackbar.LENGTH_SHORT).show()
            }


        }
        AllItemListAdapter.notifyDataSetChanged()

        return view
    }



}

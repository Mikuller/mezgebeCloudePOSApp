package com.mikucode.mymezgebcloudposapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.internal.TextWatcherAdapter
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.SharedViewModel
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.ItemListAdapter
import com.mikucode.mymezgebcloudposapp.classes.Item
import com.mikucode.mymezgebcloudposapp.classes.ItemModule

class ItemsToSellFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var itemList: ArrayList<Item>

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var barCodeReaderBtn: ImageView
    private lateinit var searchBar: EditText
    private lateinit var itemModule: ItemModule
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view: View = inflater.inflate(R.layout.fragment_items, container, false)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedPreferences  = requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE)
        itemModule = ItemModule(sharedPreferences,requireContext())
        itemsRecyclerView = view.findViewById(R.id.itemListRV)
        itemList = ArrayList()
        itemListAdapter = ItemListAdapter( requireContext(), itemList, sharedPreferences, sharedViewModel)
        itemsRecyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        itemsRecyclerView.adapter = itemListAdapter

        barCodeReaderBtn = view.findViewById(R.id.QRreaderBtn)
        barCodeReaderBtn.setOnClickListener {

            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container, BarCodeReaderFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
        
        searchBar = view.findViewById(R.id.search_bar_EditTxt)
        searchBar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                // Filter the list based on the search query'
                    val filteredList = filterItems(s.toString())
                    itemList.clear()
                    itemList.addAll(filteredList)
                    itemListAdapter.notifyDataSetChanged()

            }
        })

        getItemsFromDB()
       return view
    }
    fun filterItems(query: String): List<Item> {
        val filteredList = mutableListOf<Item>()
        if(query.isBlank()){
            return itemList
        }

        for (item in itemList) {
            if (item.itemName.contains(query, ignoreCase = true)) {
                        filteredList.add(item)
                    }
                }

        return filteredList
    }
    override fun onStart() {
        itemList.clear()
        super.onStart()
    }

    private fun getItemsFromDB() {
        itemModule.getAllItems { item ->
            if(item.isNotEmpty()){
                itemList.clear()
                itemList.addAll(item)
                itemListAdapter.notifyDataSetChanged()
            }else{
                itemList.clear()
                if(context!=null){
                    Toast.makeText(requireContext(), "Add Items to start selling", Toast.LENGTH_LONG).show()
                }
                itemListAdapter.notifyDataSetChanged()
            }
        }
    }

}
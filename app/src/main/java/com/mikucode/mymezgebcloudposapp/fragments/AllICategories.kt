package com.mikucode.mymezgebcloudposapp.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mikucode.mymezgebcloudposapp.ManageCategoryDetail
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.classes.Category
import com.mikucode.mymezgebcloudposapp.classes.CategoryModule

class AllItemCategories : Fragment() {
    private lateinit var allCategoryRV: RecyclerView
    private lateinit var AllCategoryListAdapter: AllCategoryListAdapter
    private var categoryList: ArrayList<Category> = ArrayList()
    private lateinit var categoryModule: CategoryModule
    private lateinit var sharedPreferences: SharedPreferences

    private fun initialization(view: View) {
        sharedPreferences  = requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE)
        allCategoryRV = view.findViewById(R.id.allCategories)
        AllCategoryListAdapter = AllCategoryListAdapter(requireContext(),categoryList)
        allCategoryRV.adapter = AllCategoryListAdapter
        allCategoryRV.layoutManager = LinearLayoutManager(requireContext(), VERTICAL,false)
        categoryModule =   CategoryModule(sharedPreferences,requireContext())

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_all_catagory, container, false)


        initialization(view)

        categoryModule.getAllCategories {

            if (it.isNotEmpty()){
                categoryList.clear()
                categoryList.addAll(it)
            }else {
                categoryList.clear()
                if(!requireContext().equals(null)){
                    Snackbar.make(requireContext(),view ,"No Category Added Yet", Snackbar.LENGTH_SHORT).show()
                }
            }
            AllCategoryListAdapter.notifyDataSetChanged()
        }


        return view
    }


}
class AllCategoryListAdapter(var context: Context, private var categoryList: ArrayList<Category>): RecyclerView.Adapter<AllCategoryListAdapter.CategoryVH>(){

    class CategoryVH(itemView: View): RecyclerView.ViewHolder(itemView){
        var categoryName = itemView.findViewById<TextView>(R.id.itemId)
        var xx = itemView.findViewById<TextView>(R.id.itemStockValue)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val view = LayoutInflater.from(context).inflate(R.layout.list_stock_items_vh, parent,false)

        return CategoryVH(view)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        holder.itemView.setBackgroundResource(R.drawable.purple_round_shape)
        holder.categoryName.text = categoryList[position].categoryName
        holder.xx.visibility = View.INVISIBLE
        holder.itemView.setOnClickListener {
            val intent = Intent(context,ManageCategoryDetail::class.java)
            intent.putExtra("categoryName", categoryList[position].categoryName)
            intent.putExtra("categoryID", categoryList[position].categoryID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }


}

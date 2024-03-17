package com.mikucode.mymezgebcloudposapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.TodaySalesAdapter
import com.mikucode.mymezgebcloudposapp.classes.Sale
import com.mikucode.mymezgebcloudposapp.classes.SalesModule
import java.text.SimpleDateFormat
import java.util.*

class TodaySalesFragment : Fragment() {
    private lateinit var salesModule: SalesModule
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var todaySalesAdapter: TodaySalesAdapter
    private lateinit var todaySalesRV: RecyclerView
    private lateinit var parentLayout: RelativeLayout
    private var salesTimeStamps: ArrayList<Date> = ArrayList()
    private var allDailySales : ArrayList<Sale> = ArrayList()
    private var concurrentSales: Map<Date?, List<Sale>> = mutableMapOf()//this will be used to group concurrent sales
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today_sales, container, false)
        sharedPreferences = requireContext().getSharedPreferences("myPref", Context.MODE_PRIVATE)
        salesModule = SalesModule(sharedPreferences,requireActivity())
        todaySalesAdapter = TodaySalesAdapter(requireContext(),salesTimeStamps, concurrentSales)
        todaySalesRV = view.findViewById(R.id.todaySalesRV)
        todaySalesRV.layoutManager = LinearLayoutManager(requireContext(), VERTICAL, true)
        todaySalesRV.adapter = todaySalesAdapter
        parentLayout = view.findViewById(R.id.parentLayout)
        salesModule.getSalesReport(getCurrentDate(),requireContext()){ sales1 ->//this callback function allows compiler to not return from the function without getting the required data
            allDailySales.clear()
            salesTimeStamps.clear()

            if(sales1.isNotEmpty()){
                allDailySales.addAll(sales1)
                concurrentSales = allDailySales.groupBy { it.date }

                for(salesTime in concurrentSales.keys){
                    salesTimeStamps.add(salesTime!!)
                }

            }
            else{
                parentLayout.removeAllViews()
                val nothingFoundView = LayoutInflater.from(requireContext()).inflate(R.layout.nothing_found_lay_out, parentLayout)

            }

        }

        todaySalesAdapter.notifyItemInserted(salesTimeStamps.size)
        return view
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }
}
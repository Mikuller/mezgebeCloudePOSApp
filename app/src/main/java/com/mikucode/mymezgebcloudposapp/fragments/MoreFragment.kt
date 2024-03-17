package com.mikucode.mymezgebcloudposapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.classes.ReportModule
import com.mikucode.mymezgebcloudposapp.classes.SalesModule
import com.mikucode.mymezgebcloudposapp.classes.StaffManagementModule

class MoreFragment : Fragment() {
    private lateinit var profitVal : TextView
    private lateinit var lowStockVal : TextView
    private lateinit var staffCount: TextView
    private lateinit var salesModule: SalesModule
    private lateinit var reportModule: ReportModule
    private lateinit var staffManagementModule: StaffManagementModule
    private lateinit var sharedPreferences: SharedPreferences
    fun initialization(view: View){
        profitVal = view.findViewById(R.id.profitVal)
        lowStockVal = view.findViewById(R.id.lowStocksVal)
        staffCount = view.findViewById(R.id.staffCount)
        sharedPreferences = requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE)
        salesModule = SalesModule(sharedPreferences,requireActivity())
        reportModule = ReportModule(sharedPreferences,requireContext())
        staffManagementModule = StaffManagementModule(sharedPreferences,requireContext())

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_more, container, false)
        initialization(view)
        calculateProfit()
        calculateLowStock()
        calculateNumberOfStaff()
        return view
    }

    private fun calculateNumberOfStaff() {
        staffManagementModule.getAllStaffMembers { it ->
            if(it.isNotEmpty()){
                staffCount.text = it.size.toString()
            }
        }
    }

    private fun calculateLowStock() {
        reportModule.getAllRemainingStockCount(requireContext()){allItems ->
            if(allItems.isNotEmpty()){
               lowStockVal.text = reportModule.getLowStockInventory(allItems).size.toString()
            }
        }
    }

    private fun calculateProfit() {
        var totalProfit = 0.0F
        salesModule.getAllSales { sales ->
            if(sales.isNotEmpty()){
                for(sales1 in sales){
                    totalProfit += sales1.profit
                }
                val currency = sharedPreferences.getString("currency", "")
                profitVal.text = "$currency $totalProfit"
            }
        }
    }
}
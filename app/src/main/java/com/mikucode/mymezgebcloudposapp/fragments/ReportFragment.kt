package com.mikucode.mymezgebcloudposapp.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.mikucode.mymezgebcloudposapp.ManageReportActivity
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.classes.ReportModule
import com.mikucode.mymezgebcloudposapp.classes.SalesModule
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {
    private lateinit var txtLowStockItem: TextView
    private lateinit var txtRemainingStock: TextView
    private lateinit var txtExpiredItem: TextView
    private lateinit var daySelector: TextView
    private lateinit var txtTotalSales: TextView
    private lateinit var txtTotalProfit: TextView
    private lateinit var txtSalesCount: TextView
    private lateinit var txtTotalTax: TextView
    private lateinit var lowStockInventoryRL: RelativeLayout
    private lateinit var remainingStocksRL: RelativeLayout
    private lateinit var expiredItemsRL: RelativeLayout

    private lateinit var reportModule: ReportModule
    private lateinit var salesModule: SalesModule
    private lateinit var sharedPreferences: SharedPreferences

    private fun initialization(view: View) {
        txtLowStockItem = view.findViewById(R.id.txtLowStocksValue)
        txtRemainingStock = view.findViewById(R.id.remainingStocksValue)
        lowStockInventoryRL = view.findViewById(R.id.lowStocksValContainer)
        remainingStocksRL = view.findViewById(R.id.remainingStocksContainer)
        expiredItemsRL = view.findViewById(R.id.expiredItemsContainer)
        daySelector = view.findViewById(R.id.daySelectorIcon)
        txtTotalSales = view.findViewById(R.id.totalSalesValue)
        txtTotalProfit = view.findViewById(R.id.totalProfitValue)
        txtSalesCount = view.findViewById(R.id.salesCountValue)
        txtTotalTax = view.findViewById(R.id.totalTaxValue)
        txtExpiredItem = view.findViewById(R.id.ExpiryItemsValue)
        sharedPreferences = requireContext().getSharedPreferences("myPref", Context.MODE_PRIVATE)
        reportModule = ReportModule(sharedPreferences, requireContext())
        salesModule = SalesModule(sharedPreferences,requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        initialization(view)



        //display the first item
        reportModule.getAllRemainingStockCount(requireContext()) { items ->
            if (items.isNotEmpty()) {
                txtRemainingStock.text = items.size.toString()
                //Display the first low stock item
               val lowStockCount = reportModule.getLowStockInventory(items)
               if(lowStockCount.isNotEmpty()){
                       txtLowStockItem.text = lowStockCount.size.toString()
               }
                val expiredItemCount = reportModule.getExpiredItems(items)
                if(expiredItemCount.isNotEmpty()){
                    txtExpiredItem.text = expiredItemCount.size.toString()
                }

            }
        }

        lowStockInventoryRL.setOnClickListener {
            val intent = Intent(requireContext(),ManageReportActivity::class.java)
            startActivity(intent)
        }

        remainingStocksRL.setOnClickListener {
            val intent = Intent(requireContext(),ManageReportActivity::class.java)
            intent.putExtra("remainingStock", true)
            startActivity(intent)
        }

        expiredItemsRL.setOnClickListener {
            val intent = Intent(requireContext(),ManageReportActivity::class.java)
            intent.putExtra("expiredItems", true)
            startActivity(intent)
        }

        daySelector.addTextChangedListener { it ->
            var totalSales: Float = 0.0F
            var totalProfit: Float = 0.0F
            var totalTax: Float = 0.0F
            val pickedDate = it.toString()
            salesModule.getSalesReport(pickedDate,requireContext()){ salesList ->
                if(salesList.isNotEmpty()){
                    val salesCount = salesList.groupBy { it.date }.size//this will group concurrent sales
                    for(sales in salesList){
                        totalSales += (sales.price * sales.quantity)
                        totalProfit += sales.profit
                        totalTax +=sales.totalTax
                    }
                    txtSalesCount.text = salesCount.toString()
                    txtTotalTax.text = totalTax.toString()
                    txtTotalSales.text = totalSales.toString().plus("${sharedPreferences.getString("currency", null)}")
                    txtTotalProfit.text = totalProfit.toString().plus("${sharedPreferences.getString("currency", null)}")
                }else{
                    txtSalesCount.text = ""
                    txtTotalTax.text = ""
                    txtTotalSales.text = ""
                    txtTotalProfit.text = ""
                }
            }
        }
        daySelector.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    // Set the selected date on the text view
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, monthOfYear)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    daySelector.text = dateFormat.format(selectedDate.time)
                    // Store the formatted date in the variable txtExpiryDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }








        return view
    }

    override fun onStart() {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDateTimeString = dateFormat.format(currentDate)
        var totalSales: Float = 0.0F
        daySelector.text = currentDateTimeString
        salesModule.getSalesReport(currentDateTimeString,requireContext()){ salesList ->
            if(salesList.isNotEmpty()){
                for(sales in salesList){
                    totalSales += (sales.price * sales.quantity)
                }
                txtTotalSales.text = totalSales.toString().plus("${sharedPreferences.getString("currency", null)}")
            }

        }
        super.onStart()
    }


}
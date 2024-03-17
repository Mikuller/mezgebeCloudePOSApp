package com.mikucode.mymezgebcloudposapp.fragments

import android.Manifest
import android.content.ComponentCallbacks
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.SalesItemAdapter
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.SharedViewModel
import com.mikucode.mymezgebcloudposapp.classes.Item
import com.mikucode.mymezgebcloudposapp.classes.ReportModule
import com.mikucode.mymezgebcloudposapp.classes.Sale
import com.mikucode.mymezgebcloudposapp.classes.SalesModule
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set


class CounterFragment : Fragment() {

    val PERMISSION_REQUEST_CODE = 44

    private lateinit var addNewSales: CardView
    private lateinit var salesUI: ScrollView
    private lateinit var parentLayout: RelativeLayout
    private lateinit var clearBtn: Button
    private lateinit var addMoreItemBtn: Button
    private lateinit var salesItemRV: RecyclerView
    private lateinit var itemListAdapter: SalesItemAdapter
    private var salesItemTypeList: ArrayList<Item> = ArrayList()
    private var itemCountMap = HashMap<String, Int>()
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var grandTotalTxt: TextView
    private lateinit var chargeBtn: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var salesModule: SalesModule
    private lateinit var reportModule: ReportModule
    private lateinit var noPendingSalesTxt: TextView

    private lateinit var currentSalesDateTime: Date
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private fun initialization(view: View) {
        sharedPreferences = requireContext().getSharedPreferences("myPref", Context.MODE_PRIVATE)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        parentLayout = view.findViewById(R.id.parentLayout)
        salesUI = view.findViewById(R.id.salesUI)
        clearBtn = view.findViewById(R.id.clearBtn)
        chargeBtn = view.findViewById(R.id.chargeBtn)
        addMoreItemBtn = view.findViewById(R.id.addNewItemBtn)
        itemListAdapter = SalesItemAdapter(requireContext(), salesItemTypeList, itemCountMap, sharedPreferences)
        salesItemRV = view.findViewById(R.id.salesItemsList)
        salesItemRV.layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
        salesItemRV.adapter = itemListAdapter
        salesModule = SalesModule(sharedPreferences,requireActivity())
        addNewSales = view.findViewById(R.id.addSalesCV)
        reportModule = ReportModule(sharedPreferences, requireContext())
        noPendingSalesTxt = view.findViewById(R.id.noPendingSaleTxt)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_counter, container, false)
        initialization(view)
        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                salesModule.getConcurrentSales(getDateString(currentSalesDateTime),currentSalesDateTime,requireContext()){ it ->
                    val salesList = it
                    if(salesList.isNotEmpty()){
                        createAndSavePDF(currentSalesDateTime,salesList){
                            if (it){
                                val businessName = sharedPreferences.getString("businessName", "")
                                Toast.makeText(context,"Receipt Saved at:- internal_storage/Mezgebe Sales Receipt/$businessName",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            } else {
                // Permission denied
                Toast.makeText(
                    requireContext(),
                    "Permission denied. Cannot generate PDF.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        addNewSales.setOnClickListener {
            //go to items fragment when add new sales card view is clicked
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.navBar)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, ItemsToSellFragment()).commit()
            bottomNav?.selectedItemId = R.id.items
        }

        grandTotalTxt =
            view.findViewById(R.id.grandTotal)//this had to be initialized here to avoid null exception

        sharedViewModel.items.observe(requireActivity()) { items ->
            //sharedViewModel holds ArrayList of all Items that was clicked inside itemFragment
            //but since there are redundant Item Objects inside that arrayList it has to be Filtered

            if (items.size >= 1) {
                itemCountMap.clear()//itemCountMap Maps item unique ID with number of times that item was clicked inside itemsFragments(which in other word is the quantity of item sold)
                salesItemTypeList.clear()//salesITemType ArrayList holds Item Objects whose unique ID is used as a key inside itemCountMap
                for (item in items) {
                    if (itemCountMap.containsKey(item.itemID)) {
                        itemCountMap[item.itemID] = itemCountMap[item.itemID]!! + 1
                    } else {
                        itemCountMap[item.itemID] = 1
                    }
                }

                //salesItemTypeList is the arrayList of filtered items
                for (itemID in itemCountMap.keys) {
                    items.findLast { it.itemID == itemID }
                        ?.let { it1 -> salesItemTypeList.add(it1) }
                }


                parentLayout.removeAllViews()
                parentLayout.addView(salesUI)

                var grandTotal = 0F
                for (ID in itemCountMap.keys) {
                    for (item in salesItemTypeList) {
                        if (item.itemID == ID) {
                            grandTotal += (item.sellingPrice * itemCountMap[ID]!!)
                        }
                    }
                }//calculate grand Total here
                grandTotalTxt.text =
                    grandTotal.toString().plus(" ${sharedPreferences.getString("currency", null)}")
                itemListAdapter.notifyDataSetChanged()

            } else {
                parentLayout.removeAllViews()
                parentLayout.addView(addNewSales)
                parentLayout.addView(noPendingSalesTxt)
            }

        }
        clearBtn.setOnClickListener {
            sharedViewModel.clearValues()
            sharedViewModel.resetClickCount()
        }
        addMoreItemBtn.setOnClickListener {
            // Find the menu item by ID and set it as selected  // Find the menu item by ID and set it as selected
            val bottomNavigationView =
                requireActivity().findViewById<BottomNavigationView>(R.id.navBar)
            val menuItem = bottomNavigationView.menu.findItem(R.id.items)
            menuItem?.isChecked = true
            // Perform any additional actions or fragment transactions based on the selected menu item
            val fragment1 = ItemsToSellFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(container!!.id, fragment1)
                .commit()
        }
        chargeBtn.setOnClickListener {
            //register Sales

            currentSalesDateTime = Date()
            for (ID in itemCountMap.keys) {//here ID(which is item ID) is a key for quantity of item sold
                for (item in salesItemTypeList) {
                    if (item.itemID == ID) {
                        val itemID = item.itemName + " " + item.variantName
                        val quantity = itemCountMap[ID]!!
                        val sellingPrice = item.sellingPrice
                        val profit = (item.sellingPrice - item.costPrice) * quantity
                        val totalTax = (sellingPrice * quantity) * ((item.taxPercentage / 100))
                        //check stockValue for item and also decrement th sold quantity from it
                        reportModule.checkStockStatusForAnItem(
                            item.itemName.trim().lowercase(),
                            item.variantName.trim().lowercase(),
                            quantity
                        )
                        salesModule.registerSale(
                            itemID,
                            quantity,
                            sellingPrice,
                            currentSalesDateTime,
                            profit,
                            totalTax
                        )
                    }
                }
            }

            //Prompt user for Receipt
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(getText(R.string.would_u_like_receipt))
            builder.setPositiveButton(getText(R.string.yes)) { dialog, which ->
                val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission not granted, request it
                    requestPermissionLauncher.launch(permission)
                } else {
                    salesModule.getConcurrentSales(getDateString(currentSalesDateTime),currentSalesDateTime,requireContext()){ it ->
                        val salesList = it
                        if(salesList.isNotEmpty()){
                            createAndSavePDF(currentSalesDateTime,salesList){
                                if (it){
                                    val businessName = sharedPreferences.getString("businessName", "")
                                    Toast.makeText(context,"Receipt Saved at:- internal_storage/Mezgebe Sales Receipt/$businessName",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }


                }
            }
            builder.setNegativeButton(getText(R.string.no)) { dialog, which ->

            }
            builder.show()

            Toast.makeText(requireContext(), getText(R.string.Sales_is_Successfully_Registered), Toast.LENGTH_SHORT)
                .show()
            sharedViewModel.clearValues()
            sharedViewModel.resetClickCount()
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun createAndSavePDF(currentSalesDateTime: Date, salesList: ArrayList<Sale>, callback: (Boolean) -> Unit) {

        // Permission already granted
                salesModule.generateReceiptPDF(currentSalesDateTime,salesList){
                    if(it){
                        callback(true)
                    }else{
                        Toast.makeText(context,"wait ${salesList.size}",Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }
    }

    fun getDateString(currentDateTime: Date): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(currentDateTime)
    }

}


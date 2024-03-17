package com.mikucode.mymezgebcloudposapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.SharedViewModel
import com.mikucode.mymezgebcloudposapp.classes.CategoryModule
import com.mikucode.mymezgebcloudposapp.classes.ItemModule
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ManageItemDetails : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val currentUser = Firebase.auth.currentUser
    private lateinit var simpleModeBtn: Button
    private lateinit var advancedModeBtn: Button
    private lateinit var frameLayout: FrameLayout
    private lateinit var sellByMtdLayout: LinearLayout
    private lateinit var sellByOption: TextView
    private lateinit var trackExpiryCB: CheckBox
    private lateinit var trackExpiryInfo: LinearLayout
    private lateinit var simpleModeLayout: View
    private lateinit var advancedModeLayout: View
    private lateinit var addTaxCB: CheckBox
    private lateinit var addTaxInfo: LinearLayout
    private lateinit var addBarCodeCB: CheckBox
    private lateinit var addBarCodeInfo: LinearLayout

    private var categoryList: ArrayList<String> = ArrayList()
    private lateinit var edtItemName: EditText
    private lateinit var itemCategory: TextView
    private lateinit var categoryModule: CategoryModule


    private lateinit var sellingPrice: EditText
    private lateinit var costPrice: EditText
    private lateinit var stockValue: EditText
    private lateinit var simpleAddItemBtn: Button
    private lateinit var advancedAddItemBtn: Button
    private lateinit var modeSelectorLayOut: LinearLayout

    private lateinit var edtVariantName: EditText
    private lateinit var expiryDate: TextView
    private lateinit var expiryDateReminderDays: EditText
    private lateinit var taxPercentage: EditText
    private lateinit var categoryView: LinearLayout
    lateinit var updateBtnSimple: Button//simple mode update btn
    lateinit var deleteBtnSimple: Button//simple mode delete btn
    lateinit var updateBtnAdv: Button
    lateinit var deleteBtnAdv: Button
    private lateinit var edtBarCode: EditText
    private lateinit var btnScanBarCode: Button
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var itemModule: ItemModule

    private fun initialization() {
        sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        simpleModeBtn = findViewById(R.id.simpleMode)
        advancedModeBtn = findViewById(R.id.advancedMode)
        frameLayout = findViewById(R.id.frameContainer)
        sellByMtdLayout = findViewById(R.id.sellBy)
        sellByOption = findViewById(R.id.sellByOpt)
        simpleModeLayout = layoutInflater.inflate(R.layout.simple_mode_add_item, frameLayout, false)
        advancedModeLayout = layoutInflater.inflate(R.layout.advanced_mode_add_item, frameLayout, false)
        categoryView = findViewById(R.id.Category)
        trackExpiryCB = advancedModeLayout.findViewById(R.id.trackExpiryCB)
        trackExpiryInfo = advancedModeLayout.findViewById(R.id.belowPanel1)
        addTaxCB = advancedModeLayout.findViewById(R.id.addTaxCB)
        addTaxInfo = advancedModeLayout.findViewById(R.id.belowPanel2)
        addBarCodeCB = advancedModeLayout.findViewById(R.id.addBarCodeCB)
        addBarCodeInfo = advancedModeLayout.findViewById(R.id.belowPanel3)
        edtItemName = findViewById(R.id.editItemName)
        itemCategory = findViewById(R.id.editItemCategory)


        categoryModule = CategoryModule(sharedPreferences, this)
        modeSelectorLayOut = findViewById(R.id.simpleAdvancedLayout)
        updateBtnSimple = simpleModeLayout.findViewById(R.id.updateBtn)
        deleteBtnSimple = simpleModeLayout.findViewById(R.id.deleteBtn)
        updateBtnAdv = advancedModeLayout.findViewById(R.id.updateBtn)
        deleteBtnAdv = advancedModeLayout.findViewById(R.id.deleteBtn)

        simpleAddItemBtn = simpleModeLayout.findViewById(R.id.addItemBtn)
        advancedAddItemBtn = advancedModeLayout.findViewById(R.id.addItemVariant)


        edtVariantName = advancedModeLayout.findViewById(R.id.variantName)
        expiryDate = advancedModeLayout.findViewById(R.id.edtExpiryDate)

        taxPercentage = advancedModeLayout.findViewById(R.id.taxPercentage)
        expiryDateReminderDays = advancedModeLayout.findViewById(R.id.edtExpiryDateReminder)

        edtBarCode = advancedModeLayout.findViewById(R.id.edtBarCode)
        btnScanBarCode = advancedModeLayout.findViewById(R.id.btnScanBarCode)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        itemModule = ItemModule(sharedPreferences, this)
    }

    //this will get the result of the bar code read by BarcodeScannerActivity
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    edtBarCode.setText(data.getStringExtra("barCode"))
                }
            }
        }

    private fun validateEditTextFields(vararg editTexts: EditText): Boolean {
        for (editTxt in editTexts) {
            if (editTxt.text.toString().isEmpty()) {
                return false
            }
        }
        return true
    }
    private fun validateTextFields(vararg textViews: TextView): Boolean {
        for (textView in textViews) {
            if (textView.text.toString().isEmpty()) {
                return false
            }
        }
        return true
    }
    private fun validateFieldsWithCheckBox(taxEdt: EditText,barCodeEdt: EditText,expReminEdt: EditText, expireDate: TextView): Boolean {
        if(addTaxCB.isChecked){
            if (taxEdt.text.toString().isEmpty()) {
                return false
            }
        }
        if(addBarCodeCB.isChecked){
            if (barCodeEdt.text.toString().isEmpty()) {
                return false
            }
        }
        if(trackExpiryCB.isChecked){
            if (expReminEdt.text.toString().isEmpty() || expireDate.text.toString().isEmpty()) {
                return false
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_details)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initialization()

        //handle simple and advanced item adding mode
        switchAddingMode(simpleModeBtn, advancedModeBtn)

        //manage sell by option
        manageSellBy(sellByMtdLayout)

        //get Category Names
        getCategory(categoryView)

        //Manage Item without variant
        manageItemSimpleMode(simpleAddItemBtn, updateBtnSimple, deleteBtnSimple)

        //addItem with Variant
        manageItemAdvancedMode(advancedAddItemBtn, expiryDate, updateBtnAdv, deleteBtnAdv)

        //barCode Implementation
        btnScanBarCode.setOnClickListener {
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startForResult.launch(intent)

        }

        //below decides whether to prompt user with alert message when they want to edit itemName
        //while updating an itemVariant changing the itemName Will create a new item not a variant of an item
        edtItemName.setOnClickListener {
            if (deleteBtnSimple.isInvisible && deleteBtnSimple.isInvisible && deleteBtnAdv.isVisible && updateBtnAdv.isVisible) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("${getString(R.string.warning_for_itemName_edit)} ${edtItemName.text}?")
                builder.setPositiveButton("Yes") { dialog, which ->
                    // Perform action when user clicks "Yes"
                    // For example, show a toast message
                    edtItemName.setText("")
                }
                builder.setNegativeButton("No") { dialog, which ->
                    // Perform action when user clicks "No"
                    // For example, clear the text in the edit text
                }
                builder.show()
            }
        }

    }

    private fun getCategory(categoryView: LinearLayout) {
        val businessID = sharedPreferences.getString("businessID", "")
        categoryModule.getAllCategories {
            categoryList.clear()
            if (it.isNotEmpty()) {
                for (category in it) {
                    categoryList.add(category.categoryName)
                }
            } else {
                Snackbar.make(
                    this@ManageItemDetails,
                    categoryView,
                    getString(R.string.add_category_pls),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.add_now)) {
                    val intent = Intent(this@ManageItemDetails, ManageCategoryDetail::class.java)
                    startActivity(intent)
                    finish()
                }.show()
            }
        }
        categoryView.setOnClickListener {
            if (categoryList.size == 0) {//if there is no category inserted prompt user to insert one
                Snackbar.make(
                    this@ManageItemDetails,
                    categoryView,
                    getString(R.string.add_category_pls),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.add_now)) {
                    val intent = Intent(this@ManageItemDetails, ManageCategoryDetail::class.java)
                    startActivity(intent)
                    finish()
                }.show()
            } else {
                var selectedOption = 0
                val builder = AlertDialog.Builder(this@ManageItemDetails)
                val options: Array<String> = categoryList.toTypedArray()
                builder.setSingleChoiceItems(
                    categoryList.toArray(options),
                    selectedOption
                ) { dialog, which ->
                    selectedOption = which
                    itemCategory.text = categoryList[selectedOption]
                    dialog.dismiss()
                }
                builder.show()
            }

        }
    }

    private fun manageItemAdvancedMode(
        addItemVariantBtn: Button,
        expiryDate: TextView,
        updateBtnAdv: Button,
        deleteBtnAdv: Button
    ) {
        sellingPrice = advancedModeLayout.findViewById(R.id.sellingPrice)
        costPrice = advancedModeLayout.findViewById(R.id.costPrice)
        stockValue = advancedModeLayout.findViewById(R.id.stockValue)
        expiryDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    // Set the selected date on the text view
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, monthOfYear)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    expiryDate.text = dateFormat.format(selectedDate.time)
                    // Store the formatted date in the variable txtExpiryDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
        addItemVariantBtn.setOnClickListener {

            if (validateEditTextFields(
                    edtItemName,
                    sellingPrice,
                    costPrice,
                    edtVariantName
                ) && validateTextFields(itemCategory, sellByOption, itemCategory)
                && validateFieldsWithCheckBox(taxPercentage, edtBarCode,expiryDateReminderDays, expiryDate )

            ) {
                //get category ID not name
                categoryModule.getCategoryWithName(itemCategory.text.toString()){category->
                    if(category.isNotEmpty()){
                val itemName = edtItemName.text.toString().trim().lowercase()
                val itemCategory = category[0].categoryID
                val sellByOpt = sellByOption.text.toString()
                val sellingPrice = sellingPrice.text.toString().toFloat()
                val costPrice = costPrice.text.toString().toFloat()
                val stockValue = stockValue.text.toString().toFloat()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa", Locale.getDefault())
                val date = Date()
                val currentDateTime = dateFormat.format(date)
                val variantName1 = edtVariantName.text.toString().trim().lowercase()

                val txtExpiryDate = expiryDate.text.toString()
                val expiryDateReminder = if(trackExpiryCB.isChecked) {expiryDateReminderDays.text.toString().toInt()} else{ 0 }
                val taxPercentage = if(addTaxCB.isChecked){taxPercentage.text.toString().toFloat()} else{ 0.0F }
                val barCode = edtBarCode.text.toString()


                val itemMap: Map<String, Any> = mapOf(
                    Pair("itemName", itemName.trim().lowercase()),
                    Pair("variantName", variantName1.trim().lowercase()),
                    Pair("barCode", barCode),
                    Pair("creationTime", currentDateTime),
                    Pair("category", itemCategory),
                    Pair("costPrice", costPrice),
                    Pair("creationTime", currentDateTime),
                    Pair("sellingPrice", sellingPrice),
                    Pair("stockValue", stockValue),
                    Pair("expiryDate", txtExpiryDate),
                    Pair("expiryReminderDate", expiryDateReminder),
                    Pair("sellByMtd", sellByOpt),
                    Pair("taxPercentage", taxPercentage)
                )
                itemModule.getItemKeyFromName(itemName.trim().lowercase()) { itemKEY ->
                    if (itemKEY != "") {//If Item is already Added
                        itemModule.getVariantFromName(
                            itemKEY,
                            variantName1.trim().lowercase()
                        ) { variant ->
                            if (variant.isEmpty()) {
                                itemModule.addItemVariantToDB(
                                    itemKEY,
                                    itemMap
                                ) { addVariantSuccess ->
                                    if (addVariantSuccess) {
                                        // Variant data added successfully, show success message
                                        Toast.makeText(
                                            this@ManageItemDetails,
                                            getString(R.string.variant_added),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent =
                                            Intent(
                                                this@ManageItemDetails,
                                                InventoryManagementActivity::class.java
                                            )
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        // Failed to add variant data, show error message
                                        Toast.makeText(
                                            this@ManageItemDetails,
                                            getString(R.string.task_failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this@ManageItemDetails,
                                    getString(R.string.variant_alread_exist),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {//if no item is added and this is a variant

                        itemModule.addItemVariantToDB("", itemMap) { addVariantSuccess ->
                            if (addVariantSuccess) {
                                // Variant data added successfully, show success message
                                Toast.makeText(
                                    this@ManageItemDetails,
                                    getString(R.string.variant_added),
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent =
                                    Intent(this@ManageItemDetails, InventoryManagementActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Failed to add variant data, show error message
                                Toast.makeText(
                                    this@ManageItemDetails,
                                    getString(R.string.task_failed), Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }

                } } }
            } else {
                Snackbar.make(
                    this,
                    addItemVariantBtn,
                    getString(R.string.please_fill_out_all_the_field),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        updateBtnAdv.setOnClickListener {

            if (validateEditTextFields(
                    edtItemName,
                    sellingPrice,
                    costPrice,
                    edtVariantName,
                    expiryDateReminderDays,
                    taxPercentage,
                    edtBarCode
                ) && validateTextFields(itemCategory, sellByOption, itemCategory, expiryDate)
            ) {
                val itemName = edtItemName.text.toString().trim().lowercase()
                val itemCategory = itemCategory.text.toString()
                val sellByOpt = sellByOption.text.toString()
                val sellingPrice = sellingPrice.text.toString().toFloat()
                val costPrice = costPrice.text.toString().toFloat()
                val stockValue = stockValue.text.toString().toFloat()
                val variantName = edtVariantName.text.toString().trim().lowercase()
                val txtExpiryDate = expiryDate.text.toString()
                val expiryDateReminder = expiryDateReminderDays.text.toString().toInt()
                val taxPercentage = taxPercentage.text.toString().toFloat()
                val barCode = edtBarCode.text.toString()
                //the parameter is intentVal, which indicates the origin of this activity(from where it was opened from)
                val intentVal = intent.getStringArrayExtra("itemInfo")
                val itemMap: Map<String, Any> = mapOf(
                    Pair("itemName", itemName.trim().lowercase()),
                    Pair("variantName", variantName.trim().lowercase()),
                    Pair("barCode", barCode),
                    Pair("category", itemCategory),
                    Pair("costPrice", costPrice),
                    Pair("sellingPrice", sellingPrice),
                    Pair("stockValue", stockValue),
                    Pair("expiryDate", txtExpiryDate),
                    Pair("expiryReminderDate", expiryDateReminder),
                    Pair("sellByMtd", sellByOpt),
                    Pair("taxPercentage", taxPercentage)
                )
                itemModule.getItemKeyFromName(itemName.trim().lowercase()) { itemKEY ->
                    val oldVariantName = intentVal?.get(2) ?: ""
                    if (itemKEY != "") {
                        itemModule.getVariantKeyFromName(
                            itemKEY,
                            oldVariantName.trim().lowercase()
                        ) { variantsKEY ->
                            if (variantsKEY != "") {//there definitely will be a variant
                                itemModule.getVariantFromName(
                                    itemKEY,
                                    oldVariantName.trim().lowercase()
                                ) {//check for variantName redundancy
                                    if (it.isNotEmpty()) {
                                        itemModule.updateVariantItem(
                                            itemMap,
                                            itemKEY,
                                            variantsKEY
                                        ) { updateSuccess ->
                                            if (updateSuccess) {
                                                // Variant data updated successfully, show success message
                                                Toast.makeText(
                                                    this@ManageItemDetails,
                                                    getString(R.string.update_success),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                val intent =
                                                    Intent(
                                                        this@ManageItemDetails,
                                                        InventoryManagementActivity::class.java
                                                    )
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                // Failed to update variant data, show error message
                                                Toast.makeText(
                                                    this@ManageItemDetails,
                                                    getString(R.string.task_failed),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                        }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            getString(R.string.item_not_found),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            }else{
                                Toast.makeText(
                                    this,
                                    getString(R.string.item_exist_already),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    } else {//encase user changes the item name of a variant

                                itemModule.addItemVariantToDB("", itemMap) {
                                    if (it) {
                                        Toast.makeText(
                                            this,
                                            getString(R.string.new_variant_added),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent =
                                            Intent(
                                                this@ManageItemDetails,
                                                InventoryManagementActivity::class.java
                                            )
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            getString(R.string.task_failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }
                            }
                }


            } else {
                Snackbar.make(
                    this,
                    simpleAddItemBtn,
                    getString(R.string.please_fill_out_all_the_field),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        deleteBtnAdv.setOnClickListener {
            val intentVal = intent.getStringArrayExtra("itemInfo")
            val itemVariantName = intentVal?.get(1) ?: ""
           itemModule.getItemKeyFromName(itemVariantName.trim().lowercase()){itemKEY->
               if(itemKEY != ""){
                   val variantName = intentVal?.get(2)
                   if (variantName != null) {
                       itemModule.getVariantKeyFromName(itemKEY,variantName.trim().lowercase()){
                           if(it != ""){
                               itemModule.deleteVariantItem(itemKEY,it){ deleteSuccess->
                                   if (deleteSuccess){

                                       Toast.makeText(
                                           this@ManageItemDetails,
                                           getString(R.string.variant_deleted),
                                           Toast.LENGTH_SHORT
                                       ).show()
                                       val intent = Intent(this@ManageItemDetails, InventoryManagementActivity::class.java)
                                       startActivity(intent)
                                       finish()
                                   }else{
                                       Toast.makeText(
                                           this@ManageItemDetails,
                                           getString(R.string.task_failed),
                                           Toast.LENGTH_SHORT
                                       ).show()
                                   }
                               }
                               }else{
                               Toast.makeText(
                                   this@ManageItemDetails,
                                   getString(R.string.item_deleted),
                                   Toast.LENGTH_SHORT
                               ).show()
                           }
                       }
                   }
               }
           }
        }
    }

    private fun manageItemSimpleMode(
        addItemBtn: Button,
        updateBtnSimple: Button,
        deleteBtnSimple: Button
    ) {


        addItemBtn.setOnClickListener {
            sellingPrice = simpleModeLayout.findViewById(R.id.sellingPrice)
            costPrice = simpleModeLayout.findViewById(R.id.costPrice)
            stockValue = simpleModeLayout.findViewById(R.id.stockValue)


            if (validateEditTextFields(
                    edtItemName,
                    sellingPrice,
                    costPrice,
                    stockValue
                ) && validateTextFields(sellByOption, itemCategory)
            ) {
                //get category ID not name
                categoryModule.getCategoryWithName(itemCategory.text.toString()){category->
                    if(category.isNotEmpty()){

                        val itemName = edtItemName.text.toString()
                        val itemCategory = category[0].categoryID
                        val sellByOpt = sellByOption.text.toString()
                        val sellingPrice = sellingPrice.text.toString().toFloat()
                        val costPrice = costPrice.text.toString().toFloat()
                        val stockValue = stockValue.text.toString().toFloat()
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa", Locale.getDefault())
                        val date = Date()
                        val currentDateTime = dateFormat.format(date)

                        itemModule.getItemKeyFromName(itemName.trim().lowercase()) {
                            if (it == "") {
                                itemModule.addItemToDB(
                                    itemName.lowercase().trim(),
                                    itemCategory,
                                    sellByOpt,
                                    sellingPrice,
                                    costPrice,
                                    stockValue,
                                    currentDateTime
                                ) { addItemSuccess ->
                                    if (addItemSuccess) {
                                        Toast.makeText(
                                            this@ManageItemDetails,
                                            getString(R.string.item_added),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent =
                                            Intent(this@ManageItemDetails, InventoryManagementActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@ManageItemDetails,
                                            getString(R.string.task_failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this@ManageItemDetails,
                                    getString(R.string.item_exist_already),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }



            } else {
                Snackbar.make(
                    this,
                    simpleAddItemBtn,
                    getString(R.string.please_fill_out_all_the_field),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        updateBtnSimple.setOnClickListener {
            sellingPrice = simpleModeLayout.findViewById(R.id.sellingPrice)
            costPrice = simpleModeLayout.findViewById(R.id.costPrice)
            stockValue = simpleModeLayout.findViewById(R.id.stockValue)

            if (validateEditTextFields(edtItemName, sellingPrice, costPrice, stockValue)
                && validateTextFields(sellByOption, itemCategory)
            ) {
                val intentVal = intent.getStringArrayExtra("itemInfo")
                val itemID = intentVal?.get(0)
                val oldItemName = intentVal?.get(1)
                val itemName = edtItemName.text.toString()
                val itemCategory = itemCategory.text.toString()
                val sellByOpt = sellByOption.text.toString()
                val sellingPrice = sellingPrice.text.toString().toFloat()
                val costPrice = costPrice.text.toString().toFloat()
                val stockValue = stockValue.text.toString().toFloat()

                val items: Map<String, Any> = mapOf(
                    Pair("itemName", itemName.lowercase().trim()),
                    Pair("category", itemCategory),
                    Pair("costPrice", costPrice),
                    Pair("sellingPrice", sellingPrice),
                    Pair("stockValue", stockValue),
                    Pair("sellByMtd", sellByOpt)
                )
                itemModule.getItemKeyFromName(oldItemName?.trim()?.lowercase() ?: "") {
                    if (it != "") {
                        itemModule.updateItem(items, itemID!!) { updateSuccess ->
                            if (updateSuccess) {
                                Toast.makeText(
                                    this@ManageItemDetails,
                                    getString(R.string.item_updated),
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@ManageItemDetails, InventoryManagementActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@ManageItemDetails,
                                    getString(R.string.task_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@ManageItemDetails,
                            getString(R.string.item_exist_already),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }} else {
                Snackbar.make(
                    this,
                    updateBtnSimple,
                    getString(R.string.please_fill_out_all_the_field),
                    Snackbar.LENGTH_SHORT
                ).show()
            }

        }

        deleteBtnSimple.setOnClickListener {
            val intentVal = intent.getStringArrayExtra("itemInfo")
            val itemID = intentVal?.get(0)
            val itemName = intentVal?.get(1)
            if(!itemID.isNullOrBlank()){
            itemModule.deleteItem(itemID) { deleteSuccess ->
                if (deleteSuccess) {
                    Toast.makeText(
                        this@ManageItemDetails,
                        getString(R.string.item_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@ManageItemDetails, InventoryManagementActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@ManageItemDetails,
                        getString(R.string.task_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }}else{//DON'T DELETE , this is done to avoid all item being deleted, Be careful
                if (itemName != null) {
                    itemModule.getItemKeyFromName(itemName){ key->
                        if (key!="") {
                            itemModule.deleteItem(key) { deleteSuccess ->
                                if (deleteSuccess) {
                                    Toast.makeText(
                                        this@ManageItemDetails,
                                        getString(R.string.item_deleted),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this@ManageItemDetails, InventoryManagementActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@ManageItemDetails,
                                        getString(R.string.task_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@ManageItemDetails,
                                getString(R.string.task_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun manageSellBy(sellByMtdLayout: LinearLayout) {
        sellByMtdLayout.setOnClickListener {
            val options = arrayOf(getString(R.string.unit), getString(R.string.fraction))
            var selectedOption = 0
            val builder = AlertDialog.Builder(this)
            builder.setSingleChoiceItems(options, selectedOption) { dialog, which ->
                selectedOption = which
                sellByOption.text = options[selectedOption]
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun switchAddingMode(simpleModeBtn: Button, advancedModeBtn: Button) {

        simpleModeBtn.setOnClickListener {

            it.background = ContextCompat.getDrawable(this, R.drawable.button_shape)
            simpleModeBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
            advancedModeBtn.background =
                ContextCompat.getDrawable(this, R.drawable.gray_round_shape)
            advancedModeBtn.setTextColor(ContextCompat.getColor(this, R.color.black))

            frameLayout.removeAllViews()
            frameLayout.addView(simpleModeLayout)
        }
        advancedModeBtn.setOnClickListener {

            it.background = ContextCompat.getDrawable(this, R.drawable.button_shape)
            advancedModeBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
            simpleModeBtn.background = ContextCompat.getDrawable(this, R.drawable.gray_round_shape)
            simpleModeBtn.setTextColor(ContextCompat.getColor(this, R.color.black))

            frameLayout.removeAllViews()
            frameLayout.addView(advancedModeLayout)

            //show and hide details when check box is checked
            trackExpiryInfo.visibility = View.GONE
            trackExpiryCB.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    trackExpiryInfo.visibility = View.VISIBLE
                } else {
                    trackExpiryInfo.visibility = View.GONE
                }
            }
            addTaxInfo.visibility = View.GONE
            addTaxCB.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    addTaxInfo.visibility = View.VISIBLE
                } else {
                    addTaxInfo.visibility = View.GONE
                }
            }
            addBarCodeInfo.visibility = View.GONE
            addBarCodeCB.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    addBarCodeInfo.visibility = View.VISIBLE
                } else {
                    addBarCodeInfo.visibility = View.GONE
                }
            }

        }
    }

    override fun onStart() {
        //the following code decides what to show when this activity is displayed
        //the parameter is intentVal, which indicates the origin of this activity(from where it was opened from)
        val intentVal = intent.getStringArrayExtra("itemInfo")
        if (intentVal != null) {
            if (intentVal[2] == "") {//item is not variant
                simpleAddItemBtn.visibility = View.INVISIBLE
                simpleModeBtn.performClick()
                modeSelectorLayOut.visibility = View.GONE

                sellingPrice = simpleModeLayout.findViewById(R.id.sellingPrice)
                costPrice = simpleModeLayout.findViewById(R.id.costPrice)
                stockValue = simpleModeLayout.findViewById(R.id.stockValue)
                itemModule.getItemFromName(intentVal[1]) { item ->
                    if (item.isNotEmpty()) {
                        categoryModule.getCategoryWithID(item[0].category){
                            if(it.isNotEmpty()){
                                edtItemName.setText(item[0].itemName)
                                itemCategory.text = it[0].categoryName
                                sellByOption.text = item[0].sellByMtd
                                sellingPrice.setText(item[0].sellingPrice.toString())
                                costPrice.setText(item[0].costPrice.toString())
                                stockValue.setText(item[0].stockValue.toString())
                            }}

                    }
                }

            } else {//item is variant
                updateBtnSimple.visibility = View.INVISIBLE
                deleteBtnSimple.visibility = View.INVISIBLE
                updateBtnAdv.visibility = View.VISIBLE
                deleteBtnAdv.visibility = View.VISIBLE
                advancedAddItemBtn.visibility = View.INVISIBLE
                advancedModeBtn.performClick()
                addTaxCB.isChecked = true
                addBarCodeCB.isChecked = true
                trackExpiryCB.isChecked = true
                modeSelectorLayOut.visibility = View.GONE

                sellingPrice = advancedModeLayout.findViewById(R.id.sellingPrice)
                costPrice = advancedModeLayout.findViewById(R.id.costPrice)
                stockValue = advancedModeLayout.findViewById(R.id.stockValue)
                itemModule.getItemKeyFromName(intentVal[1]) { itemKey ->
                    if (itemKey != "") {
                        itemModule.getVariantFromName(
                            itemKey, intentVal[2].lowercase().trim()
                        ) { variantItem ->
                            if (variantItem.isNotEmpty()) {
                                categoryModule.getCategoryWithID(variantItem[0].category){
                                    if(it.isNotEmpty()){
                                        edtItemName.setText(variantItem[0].itemName)
                                        itemCategory.text = it[0].categoryName
                                        sellByOption.text = variantItem[0].sellByMtd
                                        sellingPrice.setText(variantItem[0].sellingPrice.toString())
                                        costPrice.setText(variantItem[0].costPrice.toString())
                                        stockValue.setText(variantItem[0].stockValue.toString())
                                        expiryDate.text = variantItem[0].expiryDate
                                        expiryDateReminderDays.setText(variantItem[0].expiryReminderDate.toString())
                                        edtVariantName.setText(variantItem[0].variantName)
                                        taxPercentage.setText(variantItem[0].taxPercentage.toString())
                                        edtBarCode.setText(variantItem[0].barCode)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            updateBtnSimple.visibility = View.INVISIBLE
            deleteBtnSimple.visibility = View.INVISIBLE
            updateBtnAdv.visibility = View.INVISIBLE
            deleteBtnAdv.visibility = View.INVISIBLE
        }

        super.onStart()
    }
}
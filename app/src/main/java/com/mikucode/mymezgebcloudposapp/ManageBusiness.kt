package com.mikucode.mymezgebcloudposapp



import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mikucode.mymezgebcloudposapp.classes.BusinessModule
import java.text.SimpleDateFormat
import java.util.*


class ManageBusiness : AppCompatActivity() {

    private lateinit var edtLocRegion: TextView
    private lateinit var edtLocCity: TextView
    private lateinit var edtBusinessType: TextView
    private lateinit var edtCurrencyType: TextView
    private lateinit var edtBusinessName: EditText
    private lateinit var saveBtn: Button
    private lateinit var updateBtn: Button
    private lateinit var deleteBtn: Button
    private val currentUser  = Firebase.auth.currentUser
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var parentLayout: RelativeLayout
    var businessID = ""
    var pathString = ""
    private lateinit var businessModule: BusinessModule
    private fun initialization() {
        edtBusinessName = findViewById(R.id.editBusinessName)
        edtLocRegion = findViewById(R.id.editBusinessAddressRegion)
        edtLocRegion.inputType = InputType.TYPE_NULL
        edtLocCity = findViewById(R.id.editBusinessAddressCity)
        edtBusinessType = findViewById(R.id.editBusinessType)
        edtCurrencyType = findViewById(R.id.editBusinessCurrency)
        saveBtn = findViewById(R.id.saveBtn)
        sharedPreferences = getSharedPreferences("myPref", android.content.Context.MODE_PRIVATE)
        parentLayout = findViewById(R.id.addBusinessParentLayout)
        updateBtn = findViewById(R.id.updateBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        businessModule = BusinessModule(sharedPreferences, this@ManageBusiness)
        businessID = sharedPreferences.getString("businessID","").toString()
        pathString = ("${currentUser?.uid}/Businesses/${businessID}")

    }
    private fun validateFields( editText: EditText, vararg textViews: TextView): Boolean {
        if (editText.text.toString().isEmpty()) {
            return false
        }
        for (textView in textViews) {
            if (textView.text.toString().isEmpty()) {
                return false
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_business)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initialization()

        //GetBusiness Address
        showBusinessAddressOptions(edtLocRegion,edtLocCity)
        //GetBusiness Type
        showBusinessTypeOptions(edtBusinessType)
        //GetCurrency
        showBusinessCurrencyOptions(edtCurrencyType)
        //Data Saving Process
        createBusiness(saveBtn)
        //update business
        updateBusiness(updateBtn)
        //delete business
        deleteBusiness(deleteBtn)


    }

    private fun deleteBusiness(deleteBtn: Button) {
        deleteBtn.setOnClickListener {
            businessModule.deleteBusiness {
                if(it){
                Toast.makeText(this, getString(R.string.Business_Deleted_Successfully),Toast.LENGTH_SHORT).show()
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() }
                else{
                Toast.makeText(this,getString(R.string.failed_business_not_deleted),Toast.LENGTH_SHORT).show()
            }
        }
    }}

    private fun updateBusiness(updateBtn: Button) {

        updateBtn.setOnClickListener {
            val currentBusiness = sharedPreferences.getString("businessName","")
            if(validateFields(edtBusinessName, edtLocCity,edtLocCity,edtBusinessType,edtCurrencyType)){
                businessModule.getBusinessWithName(currentBusiness!!.lowercase().trim()){
                    if (it.isNotEmpty()) {
                        val businessLocation: String = edtLocRegion.text.toString()+ " " + edtLocCity.text.toString()
                        val creationTime = sharedPreferences.getString("creationTime","")
                        businessModule.updateBusiness(edtBusinessName.text.toString().lowercase().trim(),
                            businessLocation,edtBusinessType.text.toString(),
                            edtCurrencyType.text.toString(),
                            creationTime!!){ updateSuccess ->
                            if(updateSuccess){
                                Toast.makeText(this, getString(R.string.business_updated_successfully), Toast.LENGTH_LONG).show()
                                val editor = sharedPreferences.edit()
                                editor.clear()
                                editor.apply()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else{
                                Toast.makeText(this, getString(R.string.failed_business_not_updated), Toast.LENGTH_LONG).show()
                            }
                        }
                    }else{
                        Toast.makeText(this, getString(R.string.business_doesn_exists_not_updated), Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Snackbar.make(this,updateBtn,getString(R.string.please_fill_out_all_the_field),Snackbar.LENGTH_SHORT).show()
            }


        }
    }

    override fun onStart() {
        if(intent.getBooleanExtra("toEditBusiness",false)){
            //switch from saving mode to update/delete
            parentLayout.removeView(saveBtn)
            edtBusinessName.setText(sharedPreferences.getString("businessName",""))
            edtBusinessType.text = sharedPreferences.getString("businessType", "")
            edtCurrencyType.text = sharedPreferences.getString("currency", "")

        }else{
            parentLayout.removeView(updateBtn)
            parentLayout.removeView(deleteBtn)
        }
        super.onStart()
    }

    private fun createBusiness(saveBtn: Button) {
        saveBtn.setOnClickListener {
            if (validateFields((edtBusinessName), edtLocRegion, edtLocCity, edtCurrencyType,edtBusinessType)) {

                // *** Fields are valid, do something ***
            val businessName = edtBusinessName.text.toString()
            val businessType = edtBusinessType.text.toString()
            val currencyType = edtCurrencyType.text.toString()
            val businessLoc = edtLocRegion.text.toString().plus(" , ").plus(edtLocCity.text.toString())
            val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa", Locale.getDefault()) ; val date = Date()
            val currentDateTime = dateFormat.format(date)
                businessModule.getBusinessWithName(businessName.lowercase().trim()){
                    if (it.isEmpty()){
                        businessModule.registerBusiness(businessName.lowercase(),businessLoc,businessType,currencyType,currentDateTime, "OWNER"){ businessRegistered->
                            if (businessRegistered){
                                Toast.makeText(this, getString(R.string.business_created_successfully), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this, getString(R.string.task_failed), Toast.LENGTH_LONG).show()
                            }
                        }
                    }else{
                        Toast.makeText(this,getString(R.string.business_already_exists_not_saved), Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                Snackbar.make(this,saveBtn,getString(R.string.please_fill_out_all_the_field),Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBusinessCurrencyOptions(currencyType: TextView) {
     val currencyList = arrayOf( "Ethiopian Birr(ETB)" ,"US dollar (USD)" ,"English Euro (EUR)", "British Pound(GBP)"  )
        val currencySign = arrayOf( "ብር" ,"$" ,"€", "£"  )
        var selectedItem = 0
        currencyType.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setSingleChoiceItems(currencyList,selectedItem){ dialog, item ->
                selectedItem = item
                currencyType.text = currencySign[selectedItem]
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun showBusinessTypeOptions(businessType: TextView) {

        var selectedType = 0
        val typesOfBusiness: Array<String> = resources.getStringArray(R.array.Business_type)
        businessType.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.select_business_type))
            builder.setSingleChoiceItems(typesOfBusiness,selectedType){ dialog, which ->
                selectedType = which
                businessType.text = typesOfBusiness[selectedType]
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun showBusinessAddressOptions(locRegion: TextView, locCity: TextView) {

        var selectedCountryIndex= 0
        var selectedCityIndex =0
        val countries: Array<String> = resources.getStringArray(R.array.ethiopia_regions)
        val cities: Array<Array<String>> = arrayOf(
            resources.getStringArray(R.array.addis_ababa_cities),
            resources.getStringArray(R.array.afar_cities),
            resources.getStringArray(R.array.amhara_cities) ,
            resources.getStringArray(R.array.Benishangul_Gumuz_cities),
            resources.getStringArray(R.array.Dire_Dawa_cities),
            resources.getStringArray(R.array.Gambela_cities),
            resources.getStringArray(R.array.Harari_cities),
            resources.getStringArray(R.array.Oromia_cities),
            resources.getStringArray(R.array.Somali_cities),
            resources.getStringArray(R.array.SNNP_cities),
            resources.getStringArray(R.array.Tigray_cities),
        )

       locRegion.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.select_your_region))
            builder.setSingleChoiceItems(countries, selectedCountryIndex) { dialog, which ->
                selectedCountryIndex = which
                locRegion.text = countries[selectedCountryIndex]
                dialog.dismiss()
            }
            builder.show()
        }
        locCity.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.select_your_city))
            builder.setSingleChoiceItems(cities[selectedCountryIndex], selectedCityIndex) { dialog, which ->
                selectedCityIndex = which
                locCity.text = cities[selectedCountryIndex][selectedCityIndex]
                dialog.dismiss()
            }
            builder.show()

        }
    }



}
package com.mikucode.mymezgebcloudposapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mikucode.mymezgebcloudposapp.classes.BusinessModule
import com.mikucode.mymezgebcloudposapp.classes.StaffManagementModule

class ManageStaffDetails : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var staffModule: StaffManagementModule

    private lateinit var saveStaffBtn: Button
    private lateinit var updateStaffBtn: Button
    private lateinit var ownerRoleBtn: CardView
    private lateinit var clerkRoleBtn: CardView
    private lateinit var edtStaffName: EditText
    private lateinit var edtStaffEmail:EditText
    private lateinit var staffManagementModule: StaffManagementModule
    private lateinit var businessModule: BusinessModule
    var privilegeSelection = ""
    private fun initialization(){
        sharedPreferences = getSharedPreferences("myPref", MODE_PRIVATE)
        staffModule = StaffManagementModule(sharedPreferences, this@ManageStaffDetails)
        businessModule = BusinessModule(sharedPreferences,this@ManageStaffDetails)
        staffManagementModule = StaffManagementModule(sharedPreferences,this)
        saveStaffBtn = findViewById(R.id.saveBtn)
        updateStaffBtn = findViewById(R.id.updateBtn)
        ownerRoleBtn = findViewById(R.id.ownerPrivilege)
        clerkRoleBtn = findViewById(R.id.clerkPrivilege)
        edtStaffName =  findViewById(R.id.edtStaffName)
        edtStaffEmail = findViewById(R.id.edtStaffEmail)

    }
    private fun verifyInput(vararg editText: EditText): Boolean{

            for(edtText in editText) {
                if (edtText.text.toString().isNotEmpty()) {
                    return true
                }
            }
            return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_staff_details)
        initialization()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ownerRoleBtn.setOnClickListener {
            privilegeSelection = "OWNER"
            clerkRoleBtn.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            ownerRoleBtn.setCardBackgroundColor(ContextCompat.getColor(this, R.color.my_color))
        }
        clerkRoleBtn.setOnClickListener {
            privilegeSelection = "CLERK"
            ownerRoleBtn.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            clerkRoleBtn.setCardBackgroundColor(ContextCompat.getColor(this, R.color.my_color))
        }


        saveStaffBtn.setOnClickListener {
            val name = edtStaffName.text.toString()
            val email = edtStaffEmail.text.toString()

            if(verifyInput(edtStaffEmail,edtStaffName)){
                staffModule.searchStaffWithEmail(email){
                    if(it==""){
                        staffModule.addStaff(name,email, privilegeSelection){ it1 ->
                            if(it1){
                                val businessName = sharedPreferences.getString("businessName","" )
                                val businessCurrency = sharedPreferences.getString("currency", "")
                                val businessType = sharedPreferences.getString("businessType", "")
                                val businessTime = sharedPreferences.getString("creationTime","")
                                val businessID= sharedPreferences.getString("businessID","")
                                staffModule.addBusinessToStaff(email,businessID!!,businessName!!,"",businessType!!,businessCurrency!!,businessTime!!,privilegeSelection){
                                    if(it){
                                        Toast.makeText(this@ManageStaffDetails, getString(R.string.staff_added),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                finish()
                                val intent = Intent(this, StaffManagementActivity::class.java)
                                startActivity(intent)

                            }else{
                                Toast.makeText(this@ManageStaffDetails, getString(R.string.failed_to_addStaff),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }}else{
                            Toast.makeText(this@ManageStaffDetails, getString(R.string.staff_already_exist),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }



            }
            else if(privilegeSelection == ""){
                Snackbar.make(this@ManageStaffDetails, this.saveStaffBtn, getString(R.string.add_role_pls), Snackbar.LENGTH_LONG).show()
            }
            else{
                Snackbar.make(this@ManageStaffDetails, this.saveStaffBtn, getString(R.string.please_fill_out_all_the_field), Snackbar.LENGTH_LONG).show()
            }
        }
        updateStaffBtn.setOnClickListener {
            val staffName = edtStaffName.text.toString()
            val staffEmail = edtStaffEmail.text.toString()
            val staffRole = privilegeSelection
            val staffMap: Map<String, String> =
                mapOf(Pair("staffName",staffName),
                    Pair("staffEmail",staffEmail),
                    Pair("staffRole",staffRole))
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.are_you_sure_to_update))
            builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->

                val staffID = intent.getStringExtra("staffID")
                staffManagementModule.updateStaff(staffMap, staffID!!){
                    if(it){
                        Toast.makeText(this, getString(R.string.staff_updated), Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, StaffManagementActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, getString(R.string.task_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            builder.setNegativeButton(getString(R.string.no)) { dialog, which ->
                //Do nothing
            }
            builder.show()
        }


    }

    override fun onStart() {
       val staffName = intent.getStringExtra("staffName")
       val staffEmail = intent.getStringExtra("staffEmail")
       val staffRole = intent.getStringExtra("staffRole")
       if(staffName!=null){
           saveStaffBtn.visibility = View.GONE
           edtStaffName.setText(staffName)
           edtStaffEmail.setText(staffEmail)
           if(staffRole=="OWNER"){
               ownerRoleBtn.setCardBackgroundColor(ContextCompat.getColor(this, R.color.my_color))
               privilegeSelection = "OWNER"
           }else{
               clerkRoleBtn.setCardBackgroundColor(ContextCompat.getColor(this, R.color.my_color))
               privilegeSelection = "CLERK"
           }
       }else{
           updateStaffBtn.visibility = View.GONE
       }

        super.onStart()
    }
}
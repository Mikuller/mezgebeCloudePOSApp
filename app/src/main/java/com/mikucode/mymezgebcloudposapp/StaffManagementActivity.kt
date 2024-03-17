package com.mikucode.mymezgebcloudposapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.StaffListAdapter
import com.mikucode.mymezgebcloudposapp.classes.Staff
import com.mikucode.mymezgebcloudposapp.classes.StaffManagementModule

class StaffManagementActivity : AppCompatActivity() {
    private lateinit var addStaffBtn: FloatingActionButton
    private lateinit var staffListRV: RecyclerView
    private lateinit var staffListAdapter: StaffListAdapter
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var staffManagementModule: StaffManagementModule
    private var staffList: ArrayList<Staff> = ArrayList()
    private fun initialization(){
        addStaffBtn = findViewById(R.id.addStaffBnt)
        staffListRV = findViewById(R.id.staffRV)
        sharedPreferences= getSharedPreferences("myPref", Context.MODE_PRIVATE)
        staffListAdapter = StaffListAdapter(staffList,sharedPreferences, this)
        staffManagementModule = StaffManagementModule(sharedPreferences, this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_management)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initialization()
        staffManagementModule.getAllStaffMembers{
            staffList.clear()
            if(it.isNotEmpty()){
                staffList.addAll(it)
            }else{
                Toast.makeText(applicationContext, getString(R.string.no_staff), Toast.LENGTH_SHORT).show()
            }
            staffListAdapter.notifyDataSetChanged()
        }
        staffListRV.adapter = staffListAdapter
        staffListRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)

        addStaffBtn.setOnClickListener {
            val intent = Intent(this, ManageStaffDetails::class.java)
            startActivity(intent)
        }
    }
}
package com.mikucode.mymezgebcloudposapp.adaptersAndViewModels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.mikucode.mymezgebcloudposapp.ManageStaffDetails
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.StaffManagementActivity
import com.mikucode.mymezgebcloudposapp.classes.Staff
import com.mikucode.mymezgebcloudposapp.classes.StaffManagementModule

class StaffListAdapter(private val staffList: ArrayList<Staff>, val sharedPreferences: SharedPreferences , val context: Activity) : RecyclerView.Adapter<StaffListAdapter.ViewHolder>() {
    private val staffManagementModule = StaffManagementModule(sharedPreferences,context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stafflist_adapter_single_staff, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // bind data to inner RecyclerView views
        holder.staffNameTxt.text = staffList[position].staffName
        holder.editStaffBtn.setOnClickListener {
            val intent = Intent(context, ManageStaffDetails::class.java)
            intent.putExtra("staffName", staffList[position].staffName)
            intent.putExtra("staffEmail", staffList[position].staffEmail)
            intent.putExtra("staffRole", staffList[position].staffRole)
            intent.putExtra("staffID", staffList[position].staffID)
            context.startActivity(intent)
        }
        holder.deleteBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are You sure? Staff Will be Deleted Permanently")
            builder.setPositiveButton("Yes") { dialog, which ->
                staffManagementModule.removeStaff(staffList[position].staffID,staffList[position].staffEmail){
                    if(it){
                        context.recreate()
                        Toast.makeText(context, "Staff Deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            builder.setNegativeButton("No") { dialog, which ->
                //Do nothing
            }
            builder.show()
        }
    }

    override fun getItemCount(): Int {
        return staffList.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val staffNameTxt: TextView = itemView.findViewById(R.id.staffName)
        val editStaffBtn: ImageView = itemView.findViewById(R.id.updateStaffBtn)
        val deleteBtn: ImageView = itemView.findViewById(R.id.deleteStaffBtn)
    }
}
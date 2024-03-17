package com.mikucode.mymezgebcloudposapp.adaptersAndViewModels

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.mikucode.mymezgebcloudposapp.MainActivity
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.classes.Business

class BusinessListAdapter(val context: Context, private val businessList: ArrayList<Business>,private val sharedPreferences: SharedPreferences):RecyclerView.Adapter<BusinessListAdapter.BusinessViewHolder>() {
    private val currentUser = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.business_list_cardview_layout, parent ,false)
        return BusinessViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {

        val currentBusiness = businessList[position]
        holder.txtBusinessName.text = currentBusiness.businessName
        //if there is only one business add it to shared Preference without needing to be clicked
        if ( businessList.size == 1){
            val editor = sharedPreferences.edit()
            editor.putString("businessName", businessList[0].businessName )
            editor.putString("currency", businessList[0].currency)
            editor.putString("businessType", businessList[0].businessType)
            editor.putString("creationTime",businessList[0].creationTime)
            editor.putString("businessID",businessList[0].businessID)
            editor.putString("userPrivilege",businessList[0].userPrivilege)
            editor.putString("creatorID",businessList[0].ownerID)
            editor.apply()
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            val editor = sharedPreferences.edit()
            editor.putString("businessName", currentBusiness.businessName )
            editor.putString("currency" , currentBusiness.currency)
            editor.putString("businessType", currentBusiness.businessType)
            editor.putString("creationTime",currentBusiness.creationTime)
            editor.putString("businessID",currentBusiness.businessID)
            editor.putString("userPrivilege",currentBusiness.userPrivilege)
            editor.putString("creatorID",currentBusiness.ownerID)
            editor.apply()
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return businessList.size
    }

    class BusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtBusinessName: TextView = itemView.findViewById(R.id.txtBusinessName)
    }
}
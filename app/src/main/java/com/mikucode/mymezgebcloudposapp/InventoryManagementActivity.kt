package com.mikucode.mymezgebcloudposapp

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.VPAdapter
import com.mikucode.mymezgebcloudposapp.fragments.AllItemCategories
import com.mikucode.mymezgebcloudposapp.fragments.AllItems

class InventoryManagementActivity : AppCompatActivity() {
    lateinit var tabLayout : TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var VPAdapter : VPAdapter
    lateinit var floatingActionButton: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_mgmt)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager= findViewById(R.id.viewPager2)
        VPAdapter = VPAdapter(supportFragmentManager,lifecycle)
        VPAdapter.addFragment(AllItems())
        VPAdapter.addFragment(AllItemCategories())
        viewPager.adapter = VPAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position

            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {   }
            override fun onTabReselected(tab: TabLayout.Tab?) {   }
        })
        viewPager.registerOnPageChangeCallback( object  : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            tabLayout.getTabAt(position)?.select()
        }
    })

        floatingActionButton = findViewById(R.id.floatingActionButton)

        floatingActionButton.setOnClickListener {
            openBottomDialog()
        }
    }

    private fun openBottomDialog() {
       val dialog = Dialog(this@InventoryManagementActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout)

        val addItem = dialog.findViewById<CardView>(R.id.addItem)
        val addCategory = dialog.findViewById<CardView>(R.id.addCategory)

        addItem.setOnClickListener{
            val intent = Intent(this, ManageItemDetails::class.java)
            startActivity(intent)

        }
        addCategory.setOnClickListener{
            val intent = Intent(this, ManageCategoryDetail::class.java)
            startActivity(intent)

        }


        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }


}
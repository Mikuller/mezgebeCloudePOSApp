package com.mikucode.mymezgebcloudposapp

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.BusinessListAdapter
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.SharedViewModel
import com.mikucode.mymezgebcloudposapp.classes.Business
import com.mikucode.mymezgebcloudposapp.databinding.ActivityMainBinding
import com.mikucode.mymezgebcloudposapp.fragments.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {


    lateinit var bottomNav: ChipNavigationBar
    private var reportFragment = ReportFragment()
    private var todaySalesFragment = TodaySalesFragment()
    private var itemsToSellFragment = ItemsToSellFragment()
    private var counterFragment = CounterFragment()
    private var moreFragment = MoreFragment()

    private lateinit var businessOwnerName: TextView
    private lateinit var businessOwnerEmail: TextView
    private lateinit var businessName: TextView
    private lateinit var manageBusinessBtn: TextView
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var createBusinessBtn: Button
    private lateinit var switchBusiness: Button
    private lateinit var drawerHeaderView: View

    private var businessList: ArrayList<Business> = ArrayList()
    private lateinit var businessListAdapter: BusinessListAdapter
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val currentUser = Firebase.auth.currentUser
    var checkItemExists = true
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var editBusinessBtn: TextView
    private lateinit var userPrivilege: TextView

    private fun initialization(){
        binding = ActivityMainBinding.inflate(layoutInflater)
        bottomNav = binding.navBar.findViewById(R.id.navBar)
        drawerHeaderView = binding.navDrawer.getHeaderView(0)
        createBusinessBtn = drawerHeaderView.findViewById(R.id.createBusiness)
        switchBusiness = drawerHeaderView.findViewById(R.id.switchBusiness)
        sharedPreferences = getSharedPreferences("myPref",Context.MODE_PRIVATE)
        businessListAdapter = BusinessListAdapter(this,businessList,sharedPreferences)
        businessOwnerName = drawerHeaderView.findViewById(R.id.businessOwnerName)
        businessOwnerEmail = drawerHeaderView.findViewById(R.id.emailTxt)
        businessName = drawerHeaderView.findViewById(R.id.businessName)
        editBusinessBtn = drawerHeaderView.findViewById(R.id.editBusiness)
        userPrivilege = drawerHeaderView.findViewById(R.id.userPrivilege)
       sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().replace(R.id.container, counterFragment).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        initialization()
        bottomNav.setItemSelected(R.id.counter)


        //manage navigation Drawer
        manageNavigationDrawer(binding)

        //manage bottom navigation view
        manageBottomNavigation(bottomNav)


        //manage create business and switch Business
        manageCreateAndSwitchBusiness(createBusinessBtn,switchBusiness)

        //Update User info

            businessName.text =  sharedPreferences.getString("businessName", null )
            userPrivilege.text = sharedPreferences.getString("userPrivilege",null)
            businessOwnerEmail.text = currentUser?.email
            businessOwnerName.text = currentUser?.displayName

        editBusinessBtn.setOnClickListener {
            val intent = Intent(this@MainActivity,ManageBusiness::class.java)
            intent.putExtra("toEditBusiness", true)
            startActivity(intent)
        }

    }


    override fun onStart() {
        //if no business is selected prompt user to select one
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser == null){
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }else{

            getBusinessesFromDB{ itemExists ->
                if (!itemExists){//if no business is added
                    val editor = sharedPreferences.edit()
                    editor.putString("businessName", null )
                    editor.apply()
                    val intent = Intent(this@MainActivity, ManageBusiness::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    if(sharedPreferences.getString("businessName",null).isNullOrEmpty())
                    {
                        openBottomDialog() }

                }
            }

        }

        super.onStart()
    }

    private fun manageCreateAndSwitchBusiness(createBusinessBtn: Button, switchBusiness: Button) {
        createBusinessBtn.setOnClickListener {
            val intent = Intent(this, ManageBusiness::class.java)
            startActivity(intent)
        }

        switchBusiness.setOnClickListener {
            getBusinessesFromDB { itemExist->
                if (itemExist){
                    openBottomDialog()
                }
            }

        }
    }

    private fun getBusinessesFromDB(callback: (Boolean) -> Unit) {
        businessList.clear()

        databaseRef.child("${currentUser?.uid}").child("Businesses").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()){
                    checkItemExists = false
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                }
                else{
                    for (postSnapShot in snapshot.children) {
                        val currentBusiness = postSnapShot.getValue(Business::class.java)
                        if (currentBusiness != null) {
                            businessList.add(currentBusiness)
                        }
                    }
                    checkItemExists = true
                    businessListAdapter.notifyItemInserted(businessList.size - 1)
                }
                callback(checkItemExists)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,getString(R.string.error_in_retriving).plus("$error") ,Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })

    }

    private fun openBottomDialog() {
        val dialog = Dialog(this@MainActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.businesses_list_bottomsheet)

        val businessListRV = dialog.findViewById<RecyclerView>(R.id.BusinessListRecyclerView)
        businessListRV.layoutManager = GridLayoutManager(this, 2)
        businessListRV.adapter = businessListAdapter
        dialog.setOnCancelListener  {
            if(sharedPreferences.getString("businessName",null).isNullOrEmpty()){
                openBottomDialog()
            } }
        dialog.show()

        dialog.window?.setLayout(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun manageNavigationDrawer(binding: ActivityMainBinding) {
        //check user privilege and hide some menu like staff management and inventory management
        if(sharedPreferences.getString("userPrivilege",null).equals("CLERK")){
            //clerk User is not able to manage inventory and staff
            binding.navDrawer.menu.findItem(R.id.inventory).isVisible = false
            binding.navDrawer.menu.findItem(R.id.staffManagement).isVisible = false
            editBusinessBtn.visibility = View.INVISIBLE
        }
        setContentView(binding.root)
        binding.apply {
            toggle = ActionBarDrawerToggle(this@MainActivity,drawerLayout,R.string.open,R.string.close)
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowCustomEnabled(true)


            navDrawer.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.inventory -> {
                        val intent = Intent(this@MainActivity, InventoryManagementActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.language -> {
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setMessage("Please select Your language")
                        builder.setPositiveButton("Amharic") { dialog, which ->
                           switchLanguage("am")

                        }
                        builder.setNeutralButton("Afaan Oromo") { dialog, which ->
                            switchLanguage("om")

                        }
                        builder.setNegativeButton("English") { dialog, which ->
                            switchLanguage("US")

                        }
                        builder.setOnDismissListener {
                            recreate()
                        }
                        builder.show()
                    }
                    R.id.staffManagement -> {
                        val intent = Intent(this@MainActivity, StaffManagementActivity::class.java)
                        startActivity(intent)
                    }
//                    R.id.generalSetting -> {
//                        Toast.makeText(
//                            this@MainActivity,
//                            "General setting clicked",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
                    R.id.logout -> {
                        Toast.makeText(this@MainActivity, getString(R.string.user_loging_out), Toast.LENGTH_SHORT)
                            .show()
                        Firebase.auth.signOut()
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        Toast.makeText(this@MainActivity, "nothing clicked", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                true
            }

        }


    }
    private fun switchLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
        //recreate the activity to update the views and resources
        recreate()
    }


    private fun manageBottomNavigation(bottomNav: ChipNavigationBar) {

        bottomNav.setOnItemSelectedListener { item ->
            when (item) {
                R.id.reports -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, reportFragment).commit()
                R.id.today -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, todaySalesFragment).commit()
                R.id.counter -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, counterFragment).commit()
                R.id.items -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, itemsToSellFragment).commit()
                R.id.more -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, moreFragment).commit()
            }
            true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
             true
        }
        return super.onOptionsItemSelected(item)
    }

}

package com.mikucode.mymezgebcloudposapp


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.mikucode.mymezgebcloudposapp.classes.CategoryModule

class ManageCategoryDetail : AppCompatActivity() {

    private lateinit var edtCategoryName: EditText
    lateinit var sharedPreferences: SharedPreferences
    lateinit var categoryModule: CategoryModule
    lateinit var updateBtn: Button
    lateinit var deleteBtn: Button
    lateinit var saveBtn: Button
    private fun validateEditText(editText: EditText): Boolean {
        if (editText.text.toString().isNotEmpty()) {
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        edtCategoryName = findViewById(R.id.categoryName)
        updateBtn = findViewById(R.id.updateBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        saveBtn = findViewById(R.id.saveBtn)
        sharedPreferences = getSharedPreferences("myPref", MODE_PRIVATE)
        categoryModule = CategoryModule(sharedPreferences, this)
        updateBtn.setOnClickListener {
            val categoryName = edtCategoryName.text.toString()
            val categoryID = intent.getStringExtra("categoryID")
            if (validateEditText(edtCategoryName)) {
                if (categoryID != null) {
                    categoryModule.updateCategory(categoryName, categoryID) { updateSuccess ->
                        if (updateSuccess) {
                            Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT)
                                .show()
                               finish()
                        } else {
                            Toast.makeText(this, getString(R.string.update_failed), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            } else {
                Snackbar.make(
                    this,
                    updateBtn,
                    getString(R.string.please_fill_out_all_the_field),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        deleteBtn.setOnClickListener {
            val categoryID = intent.getStringExtra("categoryID")
            if (categoryID != null ) {
                categoryModule.checkIfCategoryHasItem(categoryID){
                    if(!it){
                        categoryModule.deleteCategory(categoryID) { deleteSuccess ->
                            if (deleteSuccess) {
                                Toast.makeText(this, "category Deleted successfully", Toast.LENGTH_SHORT)
                                    .show()
                                finish()
                            } else {
                                Snackbar.make(
                                    this,
                                    deleteBtn,
                                    "Failed!! Category not deleted",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }else{
                        Snackbar.make(
                            this,
                            deleteBtn,
                            getString(R.string.category_cantbe_deleted),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
        saveBtn.setOnClickListener {
            val categoryName = edtCategoryName.text.toString()
            if (validateEditText(edtCategoryName)) {
                // Check for duplicate category name
                categoryModule.getCategoryWithName(categoryName.lowercase().trim()) {
                    if (it.isEmpty()) {
                        // category name is unique, add category data to database
                        categoryModule.addCategoryToDB(
                            categoryName.lowercase().trim()
                        ) { addSuccess ->
                            if (addSuccess) {
                                // category data added successfully, show success message
                                Toast.makeText(this@ManageCategoryDetail, getString(R.string.categ_saved), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, InventoryManagementActivity::class.java)
                                startActivity(intent)
                            } else {
                                // Failed to add category data, show error message
                                Toast.makeText(this@ManageCategoryDetail, getString(R.string.task_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // category with same name already exists, show error message
                        Toast.makeText(
                            this@ManageCategoryDetail,
                            getString(R.string.category_already_exist),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Snackbar.make(this,saveBtn,getString(R.string.please_fill_out_all_the_field),Snackbar.LENGTH_SHORT).show()
            }


        }

    }


    override fun onStart() {
        val intentVal = intent.getStringExtra("categoryName")
        if (intentVal.equals(null)) {
            updateBtn.visibility = View.INVISIBLE
            deleteBtn.visibility = View.INVISIBLE

        } else {
            supportActionBar?.title = intentVal
            saveBtn.visibility = View.INVISIBLE
            edtCategoryName.setText(intentVal)
        }
        super.onStart()
    }
}
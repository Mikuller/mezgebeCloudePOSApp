package com.mikucode.mymezgebcloudposapp.classes


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.itextpdf.text.Document
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SalesModule(val sharedPreferences: SharedPreferences,val context: Activity) {



    private val currentUser = FirebaseAuth.getInstance().currentUser
    val businessID = sharedPreferences.getString("businessID","")
    private val creatorID = sharedPreferences.getString("creatorID","")
    private val pathString = "${creatorID}/Businesses/${businessID}/zSales"
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference(pathString)
    val sales : ArrayList<Sale> = ArrayList()
    // Function to register a sale in the database
    fun registerSale(itemID: String, quantity: Int, price: Float, date: Date, profit: Float, totalTax: Float) {

        val saleId = databaseReference.push().key
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDateTimeString = dateFormat.format(date)
        if (currentUser != null && saleId != null) {
            val sale = Sale(saleId, itemID, quantity, price, date, currentDateTimeString, profit, totalTax)
            databaseReference.child(saleId).setValue(sale)
        }
    }

    // Function to get sales report for a specific day
    fun getSalesReport(day: String, context: Context, callback: (ArrayList<Sale>) -> Unit) {

        if (currentUser != null) {
            databaseReference.orderByChild("creationDate")
                .equalTo(day.trim())
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        sales.clear()
                        for (postSnapshot in snapshot.children) {
                            val sale = postSnapshot.getValue(Sale::class.java)
                            if (sale != null) {
                                sales.add(sale)
                            }
                        }
                        callback(sales)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context,"error $error", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }
    fun getConcurrentSales(day: String, salesDateTime: Date, context: Context, callback: (ArrayList<Sale>) -> Unit) {

        if (currentUser != null) {
            databaseReference.orderByChild("creationDate")
                .equalTo(day.trim())
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        sales.clear()
                        for (postSnapshot in snapshot.children) {
                            val sale = postSnapshot.getValue(Sale::class.java)
                            if (sale != null) {
                                if(sale.date?.equals(salesDateTime) == true){
                                    sales.add(sale)
                                }
                            }
                        }
                        callback(sales)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context,"error $error", Toast.LENGTH_SHORT).show()
                    }

                })
            callback(sales)
        }
    }
    fun generateReceiptPDF(
        salesDateTime: Date,
        concurrentSalesList: List<Sale>?,
        callback: (Boolean) -> Unit
    ) {

                // Permission already granted
                val businessName = sharedPreferences.getString("businessName", "")
                // Create a Document object
                val document = Document()
                // Create the directory path
                val directoryPath = File(
                    Environment.getExternalStorageDirectory(),
                    "/Mezgebe Sales Receipt/${businessName}/"
                )
                // Create the directory if it doesn't exist
                directoryPath.mkdirs()

                // Specify the file path and name
                val fileName = getCurrentDateTime(salesDateTime)
                val filePath = File(directoryPath,"${fileName}.pdf")
                // Create a PdfWriter instance to write the document to a file
        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))
            // Open the document
            document.open()
            // Create a Font object for styling
            val font = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.BOLD)
            // Add content to the document with styling
            if (concurrentSalesList != null) {
                if (concurrentSalesList.isNotEmpty()) {
                    for(sales in concurrentSalesList){
                        val salesReport =
                            "\n\t\t\t\t\t\t  Sales Receipt: \t\t\t" +
                                    "\n\t Sold Item Name : ${sales.itemID}" +
                                    "\n\t Quantity : ${sales.quantity}" +
                                    "\n\t Unit Price : ${sales.price}" +
                                    "\n\t Date Time : ${salesDateTime}" +
                                    "\n\t Total Price : ${sales.price * sales.quantity}" +
                                    "\n\t Profit : ${sales.profit}" +
                                    "\n\t Total Tax : ${sales.totalTax}" +
                                    "\n\t-------------------------------------------------"
                        val paragraph = Paragraph(salesReport, font)
                        document.add(paragraph)
                    }
                    // Close the document
                    document.close()
                    // Close the writer
                    writer.close()

                    callback(true)}}
        }catch (e:Exception){
          println("error $e")
            callback(false)
        }


    }

    fun getAllSales(callback: (ArrayList<Sale>) -> Unit){
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
              sales.clear()
              var sale : Sale?
            for(postSnapShot in snapshot.children){
                sale = postSnapShot.getValue(Sale::class.java)
                if(sale!=null){
                    sales.add(sale)
                }
            }
                callback(sales)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    // Function to get sales report for a range of days
    fun getSalesReport(startDate: Date, endDate: Date): List<Sale> {
        val sales: ArrayList<Sale> = ArrayList()

        if (currentUser != null) {
            databaseReference
                .orderByChild("date")
                .startAt(startDate.time.toDouble())
                .endAt(endDate.time.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (saleSnapshot in snapshot.children) {
                            val sale = saleSnapshot.getValue(Sale::class.java)
                            if (sale != null) {
                                sales.add(sale)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            return sales
        }

        return sales
    }

    private fun getCurrentDateTime(salesDateTime: Date): String {
        val dateFormat = SimpleDateFormat("dd_MM_yyyy  hh_mm_ss_aa", Locale.getDefault())
        return dateFormat.format(salesDateTime)
    }


}



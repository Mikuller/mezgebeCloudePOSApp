package com.mikucode.mymezgebcloudposapp.adaptersAndViewModels

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.classes.Sale
import java.util.Date
import java.util.concurrent.TimeUnit

class TodaySalesAdapter(val context: Context, private val salesTimeList: ArrayList<Date>, val concurrentSales: Map<Date?, List<Sale>>): RecyclerView.Adapter<TodaySalesAdapter.TodaySalesHolder>() {

    private var viewIsGone: Boolean = true
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodaySalesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sales_today_rv_layout, parent, false)
        return TodaySalesHolder(view)
    }

    override fun onBindViewHolder(holder: TodaySalesHolder, position: Int) {
        var grandTotal = 0.0F

        val concurrentSale = concurrentSales[salesTimeList[position]]
        if (concurrentSale != null) {
            for(item in concurrentSale){
                grandTotal += (item.price * item.quantity)
            }
        }
        holder.totalSale.text = grandTotal.toString()
        holder.numberOfItems.text = concurrentSale?.size.toString().plus(" items")
        //manage time passed since item is sold
        holder.timePassed.text = getTimePassed(concurrentSale?.get(0)?.date)

        //manage inner adapter here
        val innerAdapter = InnerAdapter(concurrentSale, context)
        holder.innerRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = innerAdapter }

        //hide and unHide itemList when a sale is clicked
        if(viewIsGone){
            holder.saleListView.removeAllViews()
        }
        holder.itemView.setOnClickListener {
            holder.saleListView.removeAllViews()
            viewIsGone = if (viewIsGone){
                holder.saleListView.addView(holder.innerRecyclerView)
                false
            } else{
                holder.saleListView.removeAllViews()
                true
            }
        }

    }

    override fun getItemCount(): Int {
       return salesTimeList.size
    }

    class TodaySalesHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var totalSale = itemView.findViewById<TextView>(R.id.TotalItemSellingPrice)
        var numberOfItems = itemView.findViewById<TextView>(R.id.numberOfItemsTxt)
        var timePassed = itemView.findViewById<TextView>(R.id.unitPrice)
        var innerRecyclerView = itemView.findViewById<RecyclerView>(R.id.totalItemsSoldRV)
        var saleListView = itemView.findViewById<RelativeLayout>(R.id.itemsSoldList)
    }

    private fun getTimePassed(date: Date?): String {
        if (date == null) {
            return "Invalid date"
        }
        val now = Date()
        val diff = now.time - date.time
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        return when {
            hours > 0 -> "$hours hours and $minutes minutes ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "just now"
        }
    }
}
class InnerAdapter(private val concurrentSale: List<Sale>?, val context: Context) : RecyclerView.Adapter<InnerAdapter.InnerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.inner_sold_item_layout, parent, false)
        return InnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        // bind data to inner RecyclerView views
        holder.itemNameTxt.text = concurrentSale?.get(position)?.itemID
        holder.itemCount.text = "${concurrentSale?.get(position)?.price} X ".plus(concurrentSale?.get(position)?.quantity.toString())
    }



    override fun getItemCount(): Int {
        return concurrentSale?.size ?: 0
    }
    class InnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTxt: TextView = itemView.findViewById(R.id.innerItemNameTxt)
        val itemCount: TextView = itemView.findViewById(R.id.itemCount)
    }
}

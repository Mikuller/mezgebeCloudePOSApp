package com.mikucode.mymezgebcloudposapp.adaptersAndViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mikucode.mymezgebcloudposapp.classes.Item

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val _items = MutableLiveData<ArrayList<Item>>(ArrayList())
    val items: LiveData<ArrayList<Item>> = _items

    fun addItem(item: Item) {
        val itemList = _items.value ?: ArrayList()
        itemList.add(item)
        _items.value = itemList
    }
    fun clearValues(){
        _items.value = ArrayList()
    }

    private val _clickedItemCounter = MutableLiveData<HashMap<String,Int>>(hashMapOf())
    val clickedItemCounter: LiveData<HashMap<String,Int>> = _clickedItemCounter

    fun countClick(itemID: String){
        if( _clickedItemCounter.value?.get(itemID) == null){
                 _clickedItemCounter.value?.set(itemID, 1) }
         else{
             _clickedItemCounter.value?.set(itemID, (1 + _clickedItemCounter.value!![itemID]!!)) }
     }

    fun resetClickCount(){
        _clickedItemCounter.value?.clear()
    }

}
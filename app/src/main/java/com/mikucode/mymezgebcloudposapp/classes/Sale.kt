package com.mikucode.mymezgebcloudposapp.classes

import java.util.*

class Sale{
    var id: String = ""
    var itemID: String = ""
    var quantity: Int = 0
    var price: Float = 0.0F
    var date: Date? = null
    var creationDate: String=""
    var profit: Float = 0.0F
    var totalTax: Float = 0.0F
    constructor()
    constructor(id: String, itemID: String, quantity: Int = 0, price: Float = 0.0F, date: Date?, creationDate: String, profit: Float, totalTax: Float){
        this.id =  id
        this.itemID = itemID
        this.quantity = quantity
        this.price = price
        this.date = date
        this.creationDate = creationDate
        this.profit = profit
        this.totalTax = totalTax
    }
    constructor( itemID: String, quantity: Int = 0, price: Float = 0.0F, date: Date? , profit: Float, totalTax: Float){
        this.itemID = itemID
        this.quantity = quantity
        this.price = price
        this.date = date
        this.creationDate = creationDate
        this.profit = profit
        this.totalTax = totalTax
    }
}
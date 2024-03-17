package com.mikucode.mymezgebcloudposapp.classes

class Item {
    var itemName: String = ""
    var variantName: String = ""
    var category: String = ""
    var sellByMtd: String = ""
    var sellingPrice: Float = 0.0F
    var costPrice: Float = 0.0F
    var stockValue: Float = 0.0F
    var expiryDate: String = ""
    var expiryReminderDate: Int = 1
    var taxPercentage: Float = 0.0F
    var creationTime: String = ""
    var itemID: String = ""
    var barCode: String = ""
    constructor()
    constructor(itemName: String,variantName: String, category: String, sellByMtd: String, sellingPrice: Float, costPrice: Float, stockValue: Float, expiryDate: String, expiryReminderDate: Int, taxPercentage: Float, creationTime: String, uID: String, bardCode: String) {
        this.itemName = itemName
        this.variantName = variantName
        this.category = category
        this.sellByMtd = sellByMtd
        this.sellingPrice = sellingPrice
        this.costPrice = costPrice
        this.stockValue = stockValue
        this.expiryDate = expiryDate
        this.expiryReminderDate = expiryReminderDate
        this.taxPercentage = taxPercentage
        this.creationTime = creationTime
        this.itemID = uID
        this.barCode = bardCode
    }
    constructor(itemName: String, category: String, sellByMtd: String, sellingPrice: Float, costPrice: Float, stockValue: Float, creationTime: String, itemID: String) {
        this.itemName = itemName
        this.category = category
        this.sellByMtd = sellByMtd
        this.sellingPrice = sellingPrice
        this.costPrice = costPrice
        this.stockValue = stockValue
        this.creationTime = creationTime
        this.itemID = itemID
    }

}
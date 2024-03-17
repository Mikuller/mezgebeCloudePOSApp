package com.mikucode.mymezgebcloudposapp.classes

class Category {
    var categoryID: String = ""
    var categoryName: String = ""
    constructor()
    constructor(categoryName: String, categoryID: String){
        this.categoryID = categoryID
        this.categoryName = categoryName
    }
}
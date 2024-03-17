package com.mikucode.mymezgebcloudposapp.classes

class Business {
    var businessID: String = ""
    var businessName: String = ""
    var location: String = ""
    var businessType: String = ""
    var currency: String = ""
    var creationTime: String = ""
    var ownerID: String = ""
    var userPrivilege: String = ""
    constructor()
    constructor(
        BusinessID: String,
        BusinessName: String,
        Location: String,
        BusinessType: String,
        Currency: String,
        CreationTime: String,
        ownerID: String,
        userPrivilege: String
    ) {
        this.businessID = BusinessID
        this.businessName = BusinessName
        this.location = Location
        this.businessType = BusinessType
        this.currency = Currency
        this.creationTime = CreationTime
        this.ownerID = ownerID
        this.userPrivilege = userPrivilege
    }
}


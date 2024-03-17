package com.mikucode.mymezgebcloudposapp.classes

class Staff {
    var staffEmail: String = ""
    var staffName: String = ""
    var staffRole: String = ""
    var staffID: String = ""
    constructor()
    constructor(staffID: String ,staffName: String, staffEmail: String, staffRole: String){
        this.staffID = staffID
        this.staffName = staffName
        this.staffEmail = staffEmail
        this.staffRole = staffRole
    }
}
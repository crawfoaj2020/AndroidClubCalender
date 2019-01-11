package edu.rosehulman.crawfoaj.clubcalender

data class EventModelObject (
    var name:String = "",
    var description:String ="",
    var location:String = "",
    var club:String ="",
    var hour:Int = 0,
    var min:Int = 0,
    var repeatsWeekly:Boolean = true)
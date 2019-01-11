package edu.rosehulman.crawfoaj.clubcalender

data class EventModelObject (
    var name:String = "",
    var description:String ="",
    var location:String = "",
    var club:String ="",
    var time:String = "",
    var date:String = "",
    var repeatsWeekly:Boolean = true) 
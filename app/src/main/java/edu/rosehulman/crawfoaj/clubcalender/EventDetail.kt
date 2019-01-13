package edu.rosehulman.crawfoaj.clubcalender

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.content_event_detail.*

class EventDetail : AppCompatActivity() {
    var event = EventModelObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        setSupportActionBar(toolbar)
        setValues()
    }

    private fun setValues(){
        ValueName.text = event.name
        ValueDescription.text = event.description
        ValueLocation.text = event.location
        ValueTime.text = "${event.hour}:${event.min} PM" //TODO figure out AM vs PM
        ValueDate.text = event.date
        ValueClub.text = event.club
    }

}

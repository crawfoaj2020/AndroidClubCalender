package edu.rosehulman.crawfoaj.clubcalender

import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.content_event_detail.*

class EventDetail : AppCompatActivity() {
    var event = EventModelObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        setSupportActionBar(toolbar)
        event = intent.getParcelableExtra(EventModelObject.KEY)
        setValues()
    }

    private fun setValues(){
        ValueName.text = event.name
        ValueDescription.text = event.description
        ValueLocation.text = event.location
        ValueTime.text = event.getTimeFormatted() //TODO figure out AM vs PM
        val oneIndexedMonth = event.month +1
        ValueDate.text = "${oneIndexedMonth}/${event.day}/${event.year}"
        ValueClub.text = event.club
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_edit_event, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_edit_event -> {
                println("AAAAAAAAAA clicked edit event")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

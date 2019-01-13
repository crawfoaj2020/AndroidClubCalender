package edu.rosehulman.crawfoaj.clubcalender

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.app.TimePickerDialog
import android.content.Intent
import android.widget.TimePicker

import kotlinx.android.synthetic.main.activity_event_summary.*
import kotlinx.android.synthetic.main.content_event_detail.*
import kotlinx.android.synthetic.main.content_event_summary.*

class EventSummary : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_summary)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            val newFragment = TimePickerDialog(this, null, 3,0, false)
            newFragment.show()
        }
        fakeButton.setOnClickListener{ view ->
            val fakeEvent = EventModelObject("Test event", "Testing passing info",
                "Myers", "Android", 37, 5, "1/1/1", false)
            val intent = Intent(this, EventDetail::class.java)
            startActivity(intent)
        }
    }

    //TODO move to create event and implement
    override fun onTimeSet(view: TimePicker?, hour : Int, minute: Int) {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_event_summary, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

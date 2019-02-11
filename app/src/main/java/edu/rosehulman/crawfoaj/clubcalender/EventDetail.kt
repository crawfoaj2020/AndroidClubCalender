package edu.rosehulman.crawfoaj.clubcalender

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.rosehulman.rosefire.Rosefire

import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.content_event_detail.*
import java.util.*

class EventDetail : AppCompatActivity() {
    var event = EventModelObject()
    var day:Int? =null
    var month:Int?=null
    var year:Int?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        setSupportActionBar(toolbar)
        event = intent.getParcelableExtra(EventModelObject.KEY)
        day = intent.getIntExtra(EventModelObject.DAY, 0)
        month = intent.getIntExtra(EventModelObject.MONTH, 0)
        year = intent.getIntExtra(EventModelObject.YEAR, 0)
        setValues()
    }

    private fun setValues(){
        ValueName.text = event.name
        ValueDescription.text = event.description
        ValueLocation.text = event.location

        ValueTime.text = event.getTimeFormatted(false) //TODO figure out AM vs PM
        ValueTimeEnd.text = event.getTimeFormatted(true)
        if(day!=null && month != null && year != null){
            val oneIndexedMonth = month!! +1
            ValueDate.text = getString(R.string.Dateformat, oneIndexedMonth, day, year)
        }else{
            val oneIndexedMonth = event.month +1
            ValueDate.text = getString(R.string.Dateformat, oneIndexedMonth, event.day, event.year)
        }


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
                //TODO add edit permission limitation
                val intent = Intent(this,CreateEvent::class.java)
                intent.putExtra(EventModelObject.KEY, event)
                startActivityForResult(intent,EDIT_EVENT_REQUEST_CODE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val allEventsRef = FirebaseFirestore
            .getInstance().collection(Constants.EVENTS_COLLECTION)


        if (requestCode == EDIT_EVENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val newEvent = data!!.getParcelableExtra<EventModelObject>(CreateEvent.KEY_NEW_EVENT)
            allEventsRef.document(event.id).set(newEvent)
            val intent = Intent(this@EventDetail, EventSummary::class.java)
            startActivity(intent)
        }
    }

    companion object {
        val EDIT_EVENT_REQUEST_CODE = 2
    }

}

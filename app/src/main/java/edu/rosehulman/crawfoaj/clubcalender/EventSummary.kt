package edu.rosehulman.crawfoaj.clubcalender

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.widget.TimePicker
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent

import kotlinx.android.synthetic.main.activity_event_summary.*
import kotlinx.android.synthetic.main.content_event_detail.*
import kotlinx.android.synthetic.main.content_event_summary.*
import java.util.*

class EventSummary : AppCompatActivity() {

    var events = arrayListOf<EventModelObject>()
    val CREATE_EVENT_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_summary)
        setSupportActionBar(toolbar)


        val listener = weekViewListeners()
        var mWeekView = findViewById<WeekView>(R.id.weekView)
        mWeekView.setOnEventClickListener(listener)
        mWeekView.monthChangeListener = listener
        mWeekView.eventLongPressListener = listener



        fab.setOnClickListener { view ->
            val intent = Intent(this,CreateEvent::class.java)
            startActivityForResult(intent,CREATE_EVENT_REQUEST_CODE)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val newEvent = data!!.getParcelableExtra<EventModelObject>(CreateEvent.KEY_NEW_EVENT)
            events.add(newEvent)
            //TODO fix ids
            newEvent.id = (events.size-1).toLong()
            var mWeekView = findViewById<WeekView>(R.id.weekView)
            mWeekView.notifyDatasetChanged()

        }
    }

    inner class weekViewListeners():WeekView.EventClickListener,
        MonthLoader.MonthChangeListener,  WeekView.EventLongPressListener{
        override fun onEventLongPress(event: WeekViewEvent?, eventRect: RectF?) {
            //No reaction
        }

        override fun onMonthChange(newYear: Int, newMonth: Int): MutableList<out WeekViewEvent> {
            var weekEvents = arrayListOf<WeekViewEvent>()
            for(e in events){
                if(e.month == newMonth && e.year == newYear){
                    weekEvents.add(e.toWeekEvent())
                }
            }

//            val startTime = Calendar.getInstance()
//            startTime.set(Calendar.HOUR_OF_DAY, 3)
//            startTime.set(Calendar.MINUTE, 0)
//            startTime.set(Calendar.MONTH, newMonth - 1)
//            startTime.set(Calendar.YEAR, newYear)
//            val endTime = startTime.clone() as Calendar
//            endTime.add(Calendar.HOUR, 1)
//            endTime.set(Calendar.MONTH, newMonth - 1)
//            val event = WeekViewEvent(1, "Just show Somerthing", startTime, endTime)
//            event.color = Color.parseColor("#000000")
//            weekEvents.add(event)
            return weekEvents

        }

        override fun onEventClick(event: WeekViewEvent?, eventRect: RectF?) {
            val intent = Intent(this@EventSummary, EventDetail::class.java)
            if(event == null){
                return
            }
            var id = event.id.toInt()
            intent.putExtra(EventModelObject.KEY, events[id])
            startActivity(intent)

        }

    }
}



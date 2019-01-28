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
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

import kotlinx.android.synthetic.main.activity_event_summary.*
import kotlinx.android.synthetic.main.content_event_detail.*
import kotlinx.android.synthetic.main.content_event_summary.*
import java.util.*

class EventSummary : AppCompatActivity() {

    var events = arrayListOf<EventModelObject>()
    private val allEventsRef = FirebaseFirestore
        .getInstance().collection(Constants.EVENTS_COLLECTION)
    val CREATE_EVENT_REQUEST_CODE = 1
    lateinit var mWeekView: WeekView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_summary)
        setSupportActionBar(toolbar)



        val listener = weekViewListeners()
        mWeekView = findViewById<WeekView>(R.id.weekView)

        addSnapshotListener()
        println("AAAAAAAAAAAAApast snapshot listener")

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

    private fun addSnapshotListener() {
        allEventsRef
            .addSnapshotListener { snapshot, firebaseException ->
                if (firebaseException != null) {
                    return@addSnapshotListener
                }
                println("AAAAAAAAAAA snapshot listener triggered")
                processSnapshotDiffs(snapshot!!)
            }
    }

    private fun processSnapshotDiffs(snapshot: QuerySnapshot) {
        for (documentChange in snapshot.documentChanges) {
            val curEvent = EventModelObject.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    println("AAAAAAAA adding an event ${curEvent.name}")
                    events.add(curEvent)
                    mWeekView.notifyDatasetChanged()
                }
                DocumentChange.Type.REMOVED -> {

                    val index = events.indexOfFirst { curEvent.id == it.id }
                    events.removeAt(index)
                    mWeekView.notifyDatasetChanged()

                }
                DocumentChange.Type.MODIFIED -> {
                    for ((index, mq) in events.withIndex()) {
                        if (mq.id == curEvent.id) {
                            events[index] = curEvent
                            mWeekView.notifyDatasetChanged()
                            break
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val newEvent = data!!.getParcelableExtra<EventModelObject>(CreateEvent.KEY_NEW_EVENT)
//            events.add(newEvent)
//            //TODO fix ids
            allEventsRef.add(newEvent)
//            events.add(newEvent)
//            newEvent.id = (events.size-1).toLong()
//            mWeekView.notifyDatasetChanged()

        }
    }

    inner class weekViewListeners():WeekView.EventClickListener,
        MonthLoader.MonthChangeListener,  WeekView.EventLongPressListener{
        override fun onEventLongPress(event: WeekViewEvent?, eventRect: RectF?) {
            //No reaction yet, eventually delete
        }

        override fun onMonthChange(newYear: Int, newMonth: Int): MutableList<out WeekViewEvent> {
            println("AAAAAAAAAAAAA in on month change $newMonth")
            var weekEvents = arrayListOf<WeekViewEvent>()
            allEventsRef.whereEqualTo("month", newMonth)
                .get().addOnSuccessListener{document: QuerySnapshot ->
                    var returnVal = document.toObjects(EventModelObject::class.java)
                    for(nextEvent in returnVal){
                        println("AAAAAAAAAA found event ${nextEvent.name}")
                        weekEvents.add(nextEvent.toWeekEvent())
                }
//
            }

//            for(e in events){
//                if(e.month == newMonth && e.year == newYear){
//                    weekEvents.add(e.toWeekEvent())
//                }
//            }

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

        override fun onEventClick(weekEvent: WeekViewEvent?, eventRect: RectF?) {
            val intent = Intent(this@EventSummary, EventDetail::class.java)
            if(weekEvent == null){
                return
            }
            var id = weekEvent.id
            for(event in events){
                if(event.key == id){
                    intent.putExtra(EventModelObject.KEY, event)
                    break
                }
            }
            startActivity(intent)

        }

    }
}



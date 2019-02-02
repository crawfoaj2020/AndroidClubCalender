package edu.rosehulman.crawfoaj.clubcalender

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.graphics.RectF
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import edu.rosehulman.rosefire.Rosefire

import kotlinx.android.synthetic.main.activity_event_summary.*

class EventSummary : AppCompatActivity() {


    var events = arrayListOf<EventModelObject>()
    private val allEventsRef = FirebaseFirestore
        .getInstance().collection(Constants.EVENTS_COLLECTION)
    val CREATE_EVENT_REQUEST_CODE = 1
    lateinit var mWeekView: WeekView
    private val REGISTRY_TOKEN = "9dbad222-43fa-40bd-8354-6b1eb67c647b"
    private val ROSEFIRE_LOGIN_REQUEST_CODE = 2
    lateinit var authListener: FirebaseAuth.AuthStateListener
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_summary)
        setSupportActionBar(toolbar)

        val listener = weekViewListeners()
        mWeekView = findViewById(R.id.weekView)

        addSnapshotListener()
        addAuthStateListener()
//        println("AAAAAAAAAAAAApast snapshot listener")

        mWeekView.setOnEventClickListener(listener)
        mWeekView.monthChangeListener = listener
        mWeekView.eventLongPressListener = listener

        fab.setOnClickListener { view ->
            val intent = Intent(this,CreateEvent::class.java)
            startActivityForResult(intent,CREATE_EVENT_REQUEST_CODE)
        }

    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener);
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener);
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
            R.id.action_logout -> {
                auth.signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addSnapshotListener() {
        allEventsRef
            .addSnapshotListener { snapshot, firebaseException ->
                if (firebaseException != null) {
                    return@addSnapshotListener
                }
//                println("AAAAAAAAAAA snapshot listener triggered")
                processSnapshotDiffs(snapshot!!)
            }
    }

    private fun addAuthStateListener() {
        authListener = FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
            val user = auth.currentUser
            if (user != null){
                Log.d("Rose","Log in succeeded")
                if (auth.uid != "liur5"){
                    fab.hide()
                }else{
                    fab.show()
                }
            }else{
                val signInIntent = Rosefire.getSignInIntent(this, REGISTRY_TOKEN)
                Log.d("Rose","Starting Login activity")
                startActivityForResult(signInIntent, ROSEFIRE_LOGIN_REQUEST_CODE)
            }
        }
    }

    private fun processSnapshotDiffs(snapshot: QuerySnapshot) {
        for (documentChange in snapshot.documentChanges) {
            val curEvent = EventModelObject.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
//                    println("AAAAAAAA adding an event ${curEvent.name}")
//                    print("AAAAAAAAAA print 3 (once per event)")
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
//        println("AAAAAAA $events")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val newEvent = data!!.getParcelableExtra<EventModelObject>(CreateEvent.KEY_NEW_EVENT)
            allEventsRef.add(newEvent)
        }else if (requestCode == ROSEFIRE_LOGIN_REQUEST_CODE){
            val result = Rosefire.getSignInResultFromIntent(data)
            if (!result.isSuccessful) {
                // The user cancelled the login
            }else{
                FirebaseAuth.getInstance().signInWithCustomToken(result.token)
                    .addOnCompleteListener(this) { task ->
                        Log.d("Rosefire", "signInWithCustomToken:onComplete:" + task.isSuccessful)

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // you should use an AuthStateListener to handle the logic for
                        // signed in user and a signed out user.
                        if (!task.isSuccessful) {
                            Log.w("Rosefire", "signInWithCustomToken", task.exception)
                            Toast.makeText(
                                this@EventSummary, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else{
                            Log.d("Rose","Calling authListener")
                            Log.d("Rose","User id = ${auth.uid}")
                            authListener.onAuthStateChanged(auth)
                        }
                    }
            }
        }
    }


    inner class weekViewListeners():WeekView.EventClickListener,
        MonthLoader.MonthChangeListener,  WeekView.EventLongPressListener{

        override fun onEventLongPress(weekEvent: WeekViewEvent?, eventRect: RectF?) {
            println("AAAAAAAA long press")
            if(weekEvent == null){
                return
            }

            val builder = AlertDialog.Builder(this@EventSummary)
            builder.setTitle("Are you sure you would like to delete this event?")
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                var id = weekEvent.id
                for(event in events){
                    if(event.key == id){
                        allEventsRef.document(event.id).delete()
                        break
                    }
                }
            }
            builder.setNeutralButton(android.R.string.cancel, null)
            builder.create().show()

        }

        override fun onMonthChange(newYear: Int, newMonth: Int): MutableList<out WeekViewEvent> {
//            println("AAAAAAAAAAAA print 4 month: $newMonth")
            var weekEvents = arrayListOf<WeekViewEvent>()
//
            for(e in events){
                if(e.month == newMonth && e.year == newYear){
                    weekEvents.add(e.toWeekEvent())
                }
            }

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



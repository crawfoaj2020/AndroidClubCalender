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
import java.util.*

class EventSummary : AppCompatActivity() {


    var events = arrayListOf<EventModelObject>()
    private val allEventsRef = FirebaseFirestore
        .getInstance().collection(Constants.EVENTS_COLLECTION)
    private val userRef = FirebaseFirestore.getInstance().collection(Constants.USER_COLLECTION)
    private var loggedIn = false

    val CREATE_EVENT_REQUEST_CODE = 1
    lateinit var mWeekView: WeekView
    private val REGISTRY_TOKEN = "9dbad222-43fa-40bd-8354-6b1eb67c647b"
    private val ROSEFIRE_LOGIN_REQUEST_CODE = 2
    lateinit var authListener: FirebaseAuth.AuthStateListener
    val auth = FirebaseAuth.getInstance()
    var curUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_summary)
        setSupportActionBar(toolbar)

        val listener = weekViewListeners()
        mWeekView = findViewById(R.id.weekView)

        addAuthStateListener()
        updateUserBasedInfo()
        addSnapshotListener()

        mWeekView.setOnEventClickListener(listener)
        mWeekView.monthChangeListener = listener
        mWeekView.eventLongPressListener = listener

        fab.setOnClickListener { view ->
            val intent = Intent(this,CreateEvent::class.java)
            intent.putExtra(User.KEY, curUser!!.managedClubs)
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
            R.id.action_club_list -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.dialog_title_update_club_list)

                val allClubs = resources.getStringArray(R.array.all_clubs)
                val checkedItems = booleanArrayOf(false, false, false, false, false)
                for ((pos,club) in allClubs.withIndex()){
                    if (club in curUser!!.interestedClubs){
                        checkedItems[pos] = true
                    }
                }
                builder.setMultiChoiceItems(allClubs,checkedItems){dialog,which,isChecked ->
                    checkedItems[which] = isChecked
                }

                builder.setPositiveButton(android.R.string.ok){_,_ ->
                    val newClubList = arrayListOf<String>()
                    for ((pos,bool) in checkedItems.withIndex()){
                        if (bool){
                            newClubList.add(allClubs[pos])
                        }
                    }
                    curUser?.interestedClubs = newClubList
                    userRef.document(curUser!!.id).update("interestedClubs",newClubList)
                    mWeekView.notifyDatasetChanged()
                }

                builder.create().show()
                true
            }
            R.id.action_logout -> {
                loggedIn = false
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

        userRef
            .addSnapshotListener { snapshot, firestoreException ->
                if (firestoreException != null) {
                    return@addSnapshotListener
                }
//                println("AAAAAAAAAAA snapshot listener triggered")
                processUserSnapshotDiffs(snapshot!!)
            }
    }


    private fun addAuthStateListener() {
        authListener = FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
            val user = auth.currentUser
            println("AAAAAAAAA in auth state list")
            if (user != null){
//                Log.d("Rose","Log in succeeded")
                println("AAAAAAAAAA Log in succeedded")
                updateUserBasedInfo()
                mWeekView.notifyDatasetChanged()


            }else{
                if (!loggedIn) {
                    val signInIntent = Rosefire.getSignInIntent(this, REGISTRY_TOKEN)
                    Log.d("Rose","Starting Login activity")
                    loggedIn = true
                    startActivityForResult(signInIntent, ROSEFIRE_LOGIN_REQUEST_CODE)
                }
            }
        }
    }

    private fun updateUserBasedInfo() {
        val user = auth.currentUser
        if(user == null){
            println("AAAAAAAAAAAA User was null, bailed on updateUserInfo")
            return
        }
        //Make sure have a user
        userRef.whereEqualTo("username", user.uid).get().addOnSuccessListener {
            println("AAAAAAAA in on success")
            if(it.isEmpty){
                val emptyList = arrayListOf<String>()
                if(user.uid == "liur5"){
                    val clubList = arrayListOf<String>(
                        "Board Game Club",
                        "Volleyball Club",
                        "Anime Club"
                        )
                    val interestedList = arrayListOf<String>(
                        "Volleyball Club",
                        "Anime Club"
                    )
                    val user = User(user.uid, interestedList, clubList)
                    userRef.add(user)

                }else if (user.uid == "crawfoaj"){
                    println("AAAAAAAA adding ALyssa")
                    val clubList = arrayListOf<String>(
                        "MakerLab",
                        "D&D"
                    )
                    val interestedList = arrayListOf<String>(
                        "D&D"
                    )
                    val user = User(user.uid, interestedList, clubList)
                    userRef.add(user)
                }
            }
            mWeekView.notifyDatasetChanged()
        }

        userRef.whereEqualTo("username", user.uid).get().addOnSuccessListener {
            println("AAAAAAAAAAA updating user")
            for(i in it){
                curUser = User.fromSnapshot(i)
                println("AAAAAAAA set currentUser to $curUser")
                println("AAAAAAA ${curUser!!.managedClubs}")
                if(curUser!!.managedClubs.isEmpty()){
                    updateFab(false)
                }else{
                    updateFab(true)
                }
            }

        }

    }

    private fun updateFab(showFab:Boolean){
        if(showFab){
            fab.show()
        }else{
            println("AAAAAA here fab should hide")
            fab.hide()
        }
    }

    private fun processSnapshotDiffs(snapshot: QuerySnapshot) {
        for (documentChange in snapshot.documentChanges) {
            val curEvent = EventModelObject.fromSnapshot(documentChange.document)
            val id = auth.currentUser?.uid

            //if (curEvent.club in curUser.interestedClubs){
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
            //}
        }
//        println("AAAAAAA $events")
    }

    private fun processUserSnapshotDiffs(snapshot: QuerySnapshot) {

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
                            loggedIn = false
                        }else{
                            Log.d("Rose","Calling authListener")
                            Log.d("Rose","User id = ${auth.uid}")
                            loggedIn = true
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
            if (weekEvent == null) {
                return
            }

            var id = weekEvent.id
            for (event in events) {
                if (event.key == id && event.club in curUser!!.managedClubs) {
                    val builder = AlertDialog.Builder(this@EventSummary)
                    builder.setTitle("Are you sure you would like to delete this event?")
                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        allEventsRef.document(event.id).delete()

                    }
                    builder.setNeutralButton(android.R.string.cancel, null)
                    builder.create().show()
                    return

                }
            }


        }

        override fun onMonthChange(newYear: Int, newMonth: Int): MutableList<out WeekViewEvent> {
//            println("AAAAAAAAAAAA print 4 month: $newMonth")
            var weekEvents = arrayListOf<WeekViewEvent>()
//
            for(e in events){
                if (curUser != null && e.club in curUser!!.interestedClubs) {
                    weekEvents.addAll(e.getAllOccurrences(newMonth-1,newYear))
                }
            }
            weekEvents.map { Log.d("weekEvent","event name: ${it.name}, event date: ${it.startTime.time}"
            + ", where it should be year $newYear month $newMonth") }
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
                    intent.putExtra(EventModelObject.DAY, weekEvent.startTime.get(Calendar.DAY_OF_MONTH))
                    intent.putExtra(EventModelObject.MONTH, weekEvent.startTime.get(Calendar.MONTH))
                    intent.putExtra(EventModelObject.YEAR, weekEvent.startTime.get(Calendar.YEAR))
                    intent.putExtra(User.KEY, curUser!!.managedClubs)
//                    intent.putInt(EventModelObject.CALENDER_KEY, weekEvent)
                    break
                }
            }
            startActivity(intent)

        }

    }
}



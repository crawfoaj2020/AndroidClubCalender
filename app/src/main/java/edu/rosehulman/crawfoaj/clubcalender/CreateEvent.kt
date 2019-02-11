package edu.rosehulman.crawfoaj.clubcalender

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker

import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.content_create_event.*
import java.util.*

class CreateEvent : AppCompatActivity() {
    var event = EventModelObject()
    var wasNewEvent = true
    var managedClubs: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        setSupportActionBar(toolbar)

        //If true actually editing not creating
        if(intent.getParcelableExtra<EventModelObject>(EventModelObject.KEY) != null) {
            var event: EventModelObject = intent.getParcelableExtra(EventModelObject.KEY)
            name_field.setText(event.name)
            description_field.setText(event.description)
            date_field.text =  "${event.month+1}/${event.day}/${event.year}"
            location_field.setText(event.location)
            if(event.repeatsWeekly){
                repeat_field.isChecked = true
            }
            time_field.text =event.getTimeFormatted()
            club_field.text = event.club
            this.event = event
            println("AAAAAAA on Create id is ${this.event.id}")
            wasNewEvent = false
        }

        if(intent.getStringArrayListExtra(User.KEY) != null){
            managedClubs = intent.getStringArrayListExtra(User.KEY)
            println("AAAAAAA got list $managedClubs")
        }
    }

    fun showTimePickerDialog(v: View) {
        val bundle = Bundle()
        bundle.putParcelable(KEY_NEW_EVENT,event)
        val timePicker = MyTimePicker()
        timePicker.arguments = bundle
        timePicker.show(supportFragmentManager, "timePicker")
    }

    fun showDatePickerDialog(v: View){
        val bundle = Bundle()
        bundle.putParcelable(KEY_NEW_EVENT,event)
        val datePicker = MyDatePicker()
        datePicker.arguments = bundle
        datePicker.show(supportFragmentManager,"datePicker")
    }

    fun showClubPickerDialog(v: View){
        if(managedClubs == null){
            //Was going to say could not edit, but does not seem to be working
//            println("ASDF in if ")
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("Sorry, can't update the club of an existing event")
//            builder.setPositiveButton(android.R.string.ok) { _, _ ->
//            }
//            builder.create().show()

        }else {

            val builder = AlertDialog.Builder(this)
            val managedClubsArr = managedClubs!!.toTypedArray() as Array<CharSequence>
            println("AAAAAAAA ${managedClubsArr.javaClass}")
            builder.setTitle(getString(R.string.CreateEventClubDialogTittle))
            builder.setItems(managedClubsArr, { _, position ->
                updateClubName(managedClubs!![position])
            })
            builder.create().show()
        }
    }

    fun updateClubName(name: String){
        club_field.text = name
    }

    fun saveEvent(v:View){
        val intent = Intent()
        event.name = name_field.text.toString()
        event.description = description_field.text.toString()
        event.location = location_field.text.toString()
        event.repeatsWeekly = repeat_field.isChecked
        event.club = club_field.text.toString()
        intent.putExtra(KEY_NEW_EVENT,event)
        intent.putExtra(KEY_WAS_NEW_EVENT, wasNewEvent)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_event -> {
                val intent = Intent(this,EventSummary::class.java)
                event.name = name_field.text.toString()
                event.description = description_field.text.toString()
                event.location = location_field.text.toString()
                event.repeatsWeekly = repeat_field.isChecked
                event.club = club_field.text.toString()
                intent.putExtra(KEY_NEW_EVENT,event)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        val KEY_NEW_EVENT = "new event"
        val KEY_WAS_NEW_EVENT = "boolIfEventNewOrUpdated"

        class MyTimePicker() : DialogFragment(),TimePickerDialog.OnTimeSetListener {

            lateinit var event:EventModelObject

            /**
             * Called when the user is done setting a new time and the dialog has
             * closed.
             *
             * @param view the view associated with this listener
             * @param hourOfDay the hour that was set
             * @param minute the minute that was set
             */
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                event.hour = hourOfDay
                event.min = minute
                activity!!.findViewById<TextView>(R.id.time_field).text = event.getTimeFormatted()
            }

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                if (arguments != null){
                    event = arguments!!.getParcelable<EventModelObject>(KEY_NEW_EVENT)
                }
                val c = Calendar.getInstance()
                val hour = c.get(Calendar.HOUR_OF_DAY)
                val minute = c.get(Calendar.MINUTE)
                return TimePickerDialog(activity,this,hour,minute,false)
            }
        }

        class MyDatePicker():DialogFragment(),DatePickerDialog.OnDateSetListener{
            lateinit var event:EventModelObject
            /**
             * @param view the picker associated with the dialog
             * @param year the selected year
             * @param month the selected month (0-11 for compatibility with
             * [Calendar.MONTH])
             * @param dayOfMonth th selected day of the month (1-31, depending on
             * month)
             */
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                activity!!.findViewById<TextView>(R.id.date_field).text = "${month+1}/$dayOfMonth/$year"
                event.day = dayOfMonth
                event.month = month
                event.year = year
            }

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                if (arguments != null){
                    event = arguments!!.getParcelable<EventModelObject>(KEY_NEW_EVENT)
                }
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                // Create a new instance of DatePickerDialog and return it
                return DatePickerDialog(activity, this, year, month, day)
            }

        }
    }
}

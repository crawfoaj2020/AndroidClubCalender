package edu.rosehulman.crawfoaj.clubcalender

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker

import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.content_create_event.*
import java.util.*

class CreateEvent : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        setSupportActionBar(toolbar)
    }

    fun showTimePickerDialog(v: View) {
        MyTimePicker().show(supportFragmentManager, "timePicker")
    }

    fun showDatePickerDialog(v: View){
        MyDatePicker().show(supportFragmentManager,"datePicker")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_event -> {
                val intent = Intent(this,EventSummary::class.java)
                val name = name_field.text.toString()
                val descpt = description_field.text.toString()
                val location = location_field.text.toString()
                //TODO time and date
                val repeat = repeat_field.isChecked
                val club = club_field.text.toString()
                intent.putExtra(KEY_NEW_EVENT,EventModelObject(
                    name,descpt,location,club,0,0,2019,1,1,repeat
                ))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        val KEY_NEW_EVENT = "new event"

        class MyTimePicker() : DialogFragment(),TimePickerDialog.OnTimeSetListener {

            /**
             * Called when the user is done setting a new time and the dialog has
             * closed.
             *
             * @param view the view associated with this listener
             * @param hourOfDay the hour that was set
             * @param minute the minute that was set
             */
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                activity!!.findViewById<TextView>(R.id.time_field).text = "$hourOfDay:$minute"
            }

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                val c = Calendar.getInstance()
                val hour = c.get(Calendar.HOUR_OF_DAY)
                val minute = c.get(Calendar.MINUTE)

                return TimePickerDialog(activity,this,hour,minute,false)
            }
        }

        class MyDatePicker():DialogFragment(),DatePickerDialog.OnDateSetListener{
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
            }

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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

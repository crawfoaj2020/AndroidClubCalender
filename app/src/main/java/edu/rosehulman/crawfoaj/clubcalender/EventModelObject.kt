package edu.rosehulman.crawfoaj.clubcalender

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import com.alamkanak.weekview.WeekViewEvent
import java.text.SimpleDateFormat
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

data class EventModelObject (
    var name:String = "",
    var description:String ="",
    var location:String = "",
    var club:String ="",
    var hour:Int = 0,
    var min:Int = 0,
    var year:Int = 2019,
    var month:Int = 1,
    var day:Int = 1,
    var repeatsWeekly:Boolean = true,
    var key:Long = EventModelObject.getUnusedId()): Parcelable {

    var id = ""


    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    fun toWeekEvent(): WeekViewEvent{
        var startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, hour)
        startTime.set(Calendar.MINUTE, min)

        startTime.set(year, month, day)

        var endTime = Calendar.getInstance()
        endTime.set(Calendar.HOUR_OF_DAY, hour +1)
        endTime.set(Calendar.MINUTE, min)

        endTime.set(year, month, day)
        var weekEvent = WeekViewEvent(key, name, location, startTime, endTime)
        weekEvent.color = Color.parseColor("#AAAAAA")
        return weekEvent
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(location)
        parcel.writeString(club)
        parcel.writeInt(hour)
        parcel.writeInt(min)
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(day)
        parcel.writeByte((if (repeatsWeekly) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getTimeFormatted():String{
        val format = SimpleDateFormat("hh:mm a")
        val time = Calendar.getInstance()
        time.set(year,month,day,hour,min)
        return format.format(time.time)
    }

    companion object CREATOR : Parcelable.Creator<EventModelObject> {
        val KEY = "GetEvent"
        var avalibleKeys: MutableList<Long>? = null
        private fun setupKeys(){
            avalibleKeys = mutableListOf()

            for(i in 0..1000){
                avalibleKeys!!.add(i.toLong())
            }
        }

        fun getUnusedId():Long{
            if(avalibleKeys == null){
                setupKeys()
            }
            return avalibleKeys!!.removeAt(0)
        }

        //Cal when delete an event
        fun makeIdAvalible(id:Long){
            avalibleKeys!!.add(id)
        }

        override fun createFromParcel(parcel: Parcel): EventModelObject {
            return EventModelObject(parcel)
        }

        fun fromSnapshot(snapshot: DocumentSnapshot): EventModelObject{
            val event = snapshot.toObject(EventModelObject::class.java)!!
            event.id = snapshot.id
            return event
        }

        override fun newArray(size: Int): Array<EventModelObject?> {
            return arrayOfNulls(size)
        }
    }

}
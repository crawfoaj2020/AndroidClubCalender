package edu.rosehulman.crawfoaj.clubcalender

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.alamkanak.weekview.WeekViewEvent
import java.text.SimpleDateFormat
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*
import kotlin.collections.HashMap

data class EventModelObject (
    var name:String = "",
    var description:String ="",
    var location:String = "",
    var club:String ="",

    var hour:Int = 0,
    var min:Int = 0,
    var endHour: Int = 0,
    var endMin: Int = 0,
    var year:Int = 2019,
    var month:Int = 1,
    var day:Int = 1,
    var repeatsWeekly:Boolean = true
    ): Parcelable {

    var id = ""
    var key:Long = -1


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
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    fun setIdAndKey(newId: String){
        id = newId
        key = newId.hashCode().toLong()
    }

    fun toWeekEvent(): WeekViewEvent{
        var startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, hour)
        startTime.set(Calendar.MINUTE, min)

        startTime.set(year, month, day)

        var endTime = Calendar.getInstance()
        endTime.set(Calendar.HOUR_OF_DAY, endHour)
        endTime.set(Calendar.MINUTE, endMin)

        endTime.set(year, month, day)
        var weekEvent = WeekViewEvent(key, name, location, startTime, endTime)
//        weekEvent.color =
        weekEvent.color = Color.parseColor(EventModelObject.colorMap[club])
        return weekEvent
    }

    private fun toWeekEvent(start: Calendar?):WeekViewEvent {
        start!!.set(Calendar.HOUR_OF_DAY,hour)
        start!!.set(Calendar.MINUTE,min)
        val end = start!!.clone() as Calendar
        end.set(Calendar.HOUR_OF_DAY,endHour)
        end.set(Calendar.MINUTE,endMin)
        val weekEvent = WeekViewEvent(key,name,location,start,end)
        weekEvent.color = Color.parseColor(EventModelObject.colorMap[club])
//        weekEvent.color = Color.parseColor("#777777")
        return weekEvent
    }

    fun getAllOccurrences(targetMonth: Int, targetYear: Int) :List<WeekViewEvent> {
        val occurrences =  arrayListOf<WeekViewEvent>()
        if (!repeatsWeekly){
            if (this.month == targetMonth && this.year == targetYear){
                occurrences.add(this.toWeekEvent())
                occurrences.map { Log.d("weekEvent","the actual events that are returned: ${it.startTime.time}, ${it.endTime.time}") }
            }
        }else{
            if (name == "Do&D2"){
                Log.d("weekEvent","event year: $year,event month $month")
            }
            if (this.year > targetYear || (this.year == targetYear && this.month > targetMonth))
                return emptyList()
            val calendar = Calendar.getInstance()
            calendar.set(year,month,day)
            val target = Calendar.getInstance()
            target.set(targetYear,targetMonth,0)
            Log.d("weekEvent","calendar: ${calendar.time}, target: $targetYear - $targetMonth")
            while (calendar.before(target)){
                calendar.add(Calendar.WEEK_OF_YEAR,1)
            }
            Log.d("weekEvent","event date in parsing: ${calendar.time}")
            while (calendar.get(Calendar.MONTH) == targetMonth){
                val cloned = calendar.clone() as Calendar
                occurrences.add(toWeekEvent(cloned))
                calendar.add(Calendar.WEEK_OF_YEAR,1)
                Log.d("weekEvent","event date in increasing: ${calendar.time}")
            }
            occurrences.map { Log.d("weekEvent","the actual events that are returned: ${it.startTime.time}, ${it.endTime.time}") }
        }

        return occurrences
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(location)
        parcel.writeString(club)
        parcel.writeInt(hour)
        parcel.writeInt(min)
        parcel.writeInt(endHour)
        parcel.writeInt(endMin)
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(day)
        parcel.writeByte((if (repeatsWeekly) 1 else 0).toByte())

        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getTimeFormatted(wantEndTime: Boolean):String{
        val format = SimpleDateFormat("hh:mm a")
        val time = Calendar.getInstance()
        time.set(year,month,day,hour,min)
        if(wantEndTime){
            time.set(year, month, day, endHour, endMin)
        }
        return format.format(time.time)
    }

    fun computeEndTime(duration: Int) {
        var time = Calendar.getInstance()
        time.set(Calendar.HOUR_OF_DAY, hour)
        time.set(Calendar.MINUTE, min)
        time.set(year, month, day)
        time.add(Calendar.MINUTE,duration)
        endHour = time.get(Calendar.HOUR_OF_DAY)
        endMin = time.get(Calendar.MINUTE)
        println("TTTTTTTTTT $endHour")
    }

    fun getDuration(): Int {
        return (endHour-hour)*60 + endMin - min

    }

    companion object CREATOR : Parcelable.Creator<EventModelObject> {
        val KEY = "GetEvent"
        val DAY = "calender_day"
        val MONTH = "calender_month"
        val YEAR = "calender_year"
        val colorMap = mapOf<String, String>(
            "Board Game Club" to "#AAAAAA",
            "Volleyball Club" to "#C979d3",
            "Anime Club" to "#C3dcea",
            "MakerLab" to "#FEF7b8",
            "D&D" to "#FBB49b"

            )

        override fun createFromParcel(parcel: Parcel): EventModelObject {
            val newEvent = EventModelObject(parcel)
            newEvent.setIdAndKey(parcel.readString())
            return newEvent
        }

        fun fromSnapshot(snapshot: DocumentSnapshot): EventModelObject{
            val event = snapshot.toObject(EventModelObject::class.java)!!
            event.setIdAndKey(snapshot.id)
            return event
        }

        override fun newArray(size: Int): Array<EventModelObject?> {
            return arrayOfNulls(size)
        }
    }

}
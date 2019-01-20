package edu.rosehulman.crawfoaj.clubcalender

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import com.alamkanak.weekview.WeekViewEvent
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
    var repeatsWeekly:Boolean = true): Parcelable {

    var id:Long = -1

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
        //Might need a minus 1
        startTime.set(Calendar.MONTH, month - 1)
        startTime.set(Calendar.YEAR, year)

        val endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR, 1)
        endTime.set(Calendar.MONTH, month - 1)
        println("AAAAAAAAAAAA made a week event $startTime")
        var weekEvent = WeekViewEvent(id, name, location, startTime, endTime)
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

    companion object CREATOR : Parcelable.Creator<EventModelObject> {
        val KEY = "GetEvent"
        override fun createFromParcel(parcel: Parcel): EventModelObject {
            return EventModelObject(parcel)
        }

        override fun newArray(size: Int): Array<EventModelObject?> {
            return arrayOfNulls(size)
        }
    }

}
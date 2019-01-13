package edu.rosehulman.crawfoaj.clubcalender

import android.os.Parcel
import android.os.Parcelable

data class EventModelObject (
    var name:String = "",
    var description:String ="",
    var location:String = "",
    var club:String ="",
    var hour:Int = 0,
    var min:Int = 0,
    var date:String = "",
    var repeatsWeekly:Boolean = true): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(location)
        parcel.writeString(club)
        parcel.writeInt(hour)
        parcel.writeInt(min)
        parcel.writeString(date)
        parcel.writeByte((if (repeatsWeekly) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EventModelObject> {
        override fun createFromParcel(parcel: Parcel): EventModelObject {
            return EventModelObject(parcel)
        }

        override fun newArray(size: Int): Array<EventModelObject?> {
            return arrayOfNulls(size)
        }
    }
}
package fr.afpa.servicespublics.metier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//The object returned by the http request
@Parcelize
data class CityJsonObject(val code: String, val nom: String, val codeDepartement: String): Parcelable{

}
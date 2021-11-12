package fr.afpa.servicespublics.metier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//The object returned by the http request
@Parcelize
data class ServiceJsonObject(val features:List<Service>): Parcelable{

}

@Parcelize
data class Service(val properties: Properties): Parcelable{

}

@Parcelize
data class Properties(val nom: String, val adresses: List<Adresse>): Parcelable{

}

@Parcelize
data class Adresse(val lignes: List<String>, val codePostal: String,
                   val commune: String, val coordonnees:List<Float>): Parcelable{

}
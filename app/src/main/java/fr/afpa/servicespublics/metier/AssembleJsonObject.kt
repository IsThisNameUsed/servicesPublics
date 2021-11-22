package fr.afpa.servicespublics.metier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AssemblyJsonObject(var deputes: List<DeputeContainer>): Parcelable {}

@Parcelize
data class DeputeContainer(var depute: Depute): Parcelable {}

@Parcelize
data class Depute(var nom: String, var nom_circo: String, var groupe_sigle: String): Parcelable {}
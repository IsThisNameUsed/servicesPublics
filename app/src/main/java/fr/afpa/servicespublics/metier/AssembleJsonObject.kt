package fr.afpa.servicespublics.metier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class assemblyDeputiesListJson(var deputes: List<deputyContainer>): Parcelable {}

@Parcelize
data class deputyContainer(var depute: lightDeputy): Parcelable {}

@Parcelize
data class lightDeputy(var nom: String, var nom_de_famille: String, var slug: String): Parcelable {}

@Parcelize
data class deputyDetailsContainer(var depute: deputyDetails): Parcelable {}
@Parcelize
data class deputyDetails(var nom: String, var nom_de_famille: String, var nom_circo: String, var num_deptmt: String,
                  var groupe_sigle: String, var parti_ratt_financier: String, var slug: String,
                  var sites_web: List<site>, var emails: List<email>,
                    var profession:String): Parcelable {}

@Parcelize
data class site(var site: String):Parcelable{}

/*@Parcelize
data class adresse(var adresse: String):Parcelable{}*/

@Parcelize
data class email(var email: String):Parcelable{}

@Parcelize
data class deputyVoteList(var votes: List<voteContainer>):Parcelable{}

@Parcelize
data class voteContainer(var vote: vote):Parcelable{}

@Parcelize
data class vote(var scrutin: scrutin, var position: String, var parlementaire_slug: String):Parcelable{}

@Parcelize
data class scrutin(var sort: String, var date: String, var titre: String, var numero: String,
                   var nombre_votants: String, var nombre_pours: String, var nombre_contres: String,
                    var nombre_abstentions: String ):Parcelable{}

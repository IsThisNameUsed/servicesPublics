package fr.afpa.servicespublics.api

import fr.afpa.servicespublics.metier.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IFindCityApi {

    companion object {
        val ENDPOINT="https://geo.api.gouv.fr/"
    }

    @GET("communes?")
    fun getCityCodeWithName(@Query("nom")nom: String): Call<List<CityJsonObject>>

    @GET("communes?")
    fun getCityCodeWithPostalCode(@Query("codePostal")codePostal: String): Call<List<CityJsonObject>>

    @GET("communes/{codeCommune}/{typeService}")
    fun getServiceInCity(@Path("codeCommune")codePostal: String, @Path("typeService")typeService: String): Call<ServiceJsonObject>
}

interface IFindServiceApi {

    companion object {
        val ENDPOINT="https://etablissements-publics.api.gouv.fr/v3/"
    }

    @GET("communes/{codeCommune}/{typeService}")
    fun getServiceInCity(@Path("codeCommune")codePostal: String, @Path("typeService")typeService: String): Call<ServiceJsonObject>
}

interface IAssembleApi {

    companion object {
        val ENDPOINT="https://www.nosdeputes.fr/"
    }

    @GET("deputes/enmandat/json?textplain=true")
    fun getDeputyList(): Call<assemblyDeputiesListJson>

    @GET("{name}/votes/json?textplain=true")
    fun getDeputyVoteList(@Path("name")name: String): Call<deputyVoteList>

    @GET("{name}/json?textplain=true")
    fun getDeputyDetails(@Path("name")name: String): Call<deputyDetailsContainer>

}
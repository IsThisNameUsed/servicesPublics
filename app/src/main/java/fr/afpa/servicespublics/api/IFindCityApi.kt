package fr.afpa.servicespublics.api

import fr.afpa.servicespublics.metier.CityJsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IFindCityApi {

    companion object {
        val ENDPOINT="https://geo.api.gouv.fr/"
    }

    @GET("communes?nom")
    fun getCityCode(@Query("nom")nom: String): Call<List<CityJsonObject>>
}
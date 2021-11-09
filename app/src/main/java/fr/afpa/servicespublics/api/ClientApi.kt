package fr.afpa.servicespublics.api

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ClientApi {

    private val retrofit: Retrofit = if(android.os.Build.VERSION.SDK_INT >= 26) {
        Log.d("OKHTTTP", "safe Okhttp")
        Retrofit.Builder().baseUrl(IFindCityApi.ENDPOINT).addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
    else {
        Log.d("OKHTTP3", "Unsafe Okhttp")
        Retrofit.Builder().baseUrl(IFindCityApi.ENDPOINT).client(UnsafeOkHttpClient.unsafeOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create()).build()
    }

    val service = retrofit.create(IFindCityApi::class.java)
}
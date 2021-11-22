package fr.afpa.servicespublics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import fr.afpa.servicespublics.api.ClientAssembleApi
import fr.afpa.servicespublics.metier.AssemblyJsonObject
import fr.afpa.servicespublics.metier.CityJsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchByAssemblyMemberScreen1_Activity: AppCompatActivity() {

    var clientAssembleApi = ClientAssembleApi()
    lateinit var testText : TextView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_by_assemblymember_screen1)

        //Setup buttons
        val home_button = findViewById<Button>(R.id.home_button)
        val return_button = findViewById<Button>(R.id.return_button)
        val test_details_button = findViewById<Button>(R.id.testButton_toDetailsScreen)

        home_button.setOnClickListener {
            startActivity(Intent(this, NationalAssembly_Menu_Activity::class.java))
        }

        return_button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        test_details_button.setOnClickListener {
            //startActivity(Intent(this, SearchByAssemblyMemberInfoVote_Activity::class.java))
            getDeputyList()
        }

        testText = findViewById(R.id.testText)
    }

    fun getDeputyList(){
        clientAssembleApi.service.getDeputyList().enqueue(object : Callback<AssemblyJsonObject> {
            override fun onResponse(call: Call<AssemblyJsonObject>, response: Response<AssemblyJsonObject>) {
                var assemblyJsonObject = response.body()

                if(assemblyJsonObject == null)
                    Log.d("API", "error, response.body is empty")

                else {
                    assemblyJsonObject?.let {
                        var deputy = assemblyJsonObject.deputes[0].depute
                        testText.text = deputy.nom.toString() + " / " + deputy.nom_circo + " / " + deputy.groupe_sigle

                        //debug
                        Log.d("", "SUCCESS")
                    }
                }
            }
            override fun onFailure(call: Call<AssemblyJsonObject>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}
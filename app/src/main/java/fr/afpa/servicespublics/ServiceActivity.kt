package fr.afpa.servicespublics

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner

class ServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        /* Récupère les données des villes et services par le biais du fichier xml */
        val villes = resources.getStringArray(R.array.villes)
        val services = resources.getStringArray(R.array.services)
        val entites = resources.getStringArray(R.array.entites)

        /* Créé les ArrayAdapters qui vont servir à voir les données sous forme de Spinner */
        val adapterVilles = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, villes)
        val adapterServices = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, services)
        val adapterEntites = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, entites)

        /* Récupère les Spinners existants et les lie aux ArrayAdapters adéquates */
        val spinner_villes = findViewById<Spinner>(R.id.spinner_villes)
        spinner_villes.adapter = adapterVilles
        val spinner_services = findViewById<Spinner>(R.id.spinner_services)
        spinner_services.adapter = adapterServices
        val spinner_entites = findViewById<Spinner>(R.id.spinner_entites)
        spinner_entites.adapter = adapterEntites
    }
}
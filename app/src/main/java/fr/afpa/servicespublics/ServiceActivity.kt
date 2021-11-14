package fr.afpa.servicespublics

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import fr.afpa.servicespublics.api.ClientGeoApi
import fr.afpa.servicespublics.metier.CityJsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import fr.afpa.servicespublics.api.ClientServicesApi
import fr.afpa.servicespublics.metier.ServiceJsonObject
import android.text.Editable
import android.widget.AutoCompleteTextView
import android.text.TextWatcher
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_service.*


class ServiceActivity : AppCompatActivity() {

    //region init
    var clientGeoAPI = ClientGeoApi()
    var clientServiceAPI = ClientServicesApi()
    lateinit var input_cityName : TextView
    lateinit var input_cityCode : TextView
    lateinit var selectedCity : TextView
    var typeServicesList = listOf("apec", "pole_emploi", "gendarmerie", "commissariat_police", "mairie", "prefecture", "sous-pref")

    lateinit var adapterVilles :  ArrayAdapter<String>
    lateinit var adapterServices :  ArrayAdapter<String>
    lateinit var adapterEntites :  ArrayAdapter<String>

    /* Récupère les Spinners existants  */
    lateinit var spinner_villes : Spinner
    lateinit var spinner_services : Spinner
    lateinit var spinner_entites : Spinner

    var citiesList: List<CityJsonObject>? = null
    var citiesCodeList = arrayListOf<String>()
    var citiesNameList = arrayListOf<String>()

    var selectedCity_code: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        /* Récupère les données des villes et services par le biais du fichier xml */
        val villes = resources.getStringArray(R.array.villes)
        val services = resources.getStringArray(R.array.services)
        val entites = resources.getStringArray(R.array.entites)


        /* Récupère les Spinners existants  */
        spinner_villes = findViewById<Spinner>(R.id.spinner_villes)
        spinner_services = findViewById<Spinner>(R.id.spinner_services)
        spinner_entites = findViewById<Spinner>(R.id.spinner_entites)

        /* Créé les ArrayAdapters qui vont servir à voir les données sous forme de Spinner */
        adapterVilles = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item)
        adapterServices = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeServicesList)
        adapterServices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_services.setAdapter(adapterServices)
        adapterEntites = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, entites)

        //lie les spinners aux ArrayAdapters adéquats
        spinner_villes.adapter = adapterVilles
        //spinner_services.adapter = adapterServices
        spinner_entites.adapter = adapterEntites

        selectedCity = findViewById(R.id.label_ville_code)

        input_cityName = findViewById<View>(R.id.city_inputText) as AutoCompleteTextView
        val search_button = findViewById<Button>(R.id.bouton_recherche)
        search_button.setOnClickListener {
            launchResearch()
        }

        //Installe un TextWatcher pour obtenir des callbacks dès que le texte est modifié dans le champs de saisie
        input_cityName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(input: CharSequence, start: Int, before: Int, count: Int) {
                GetCitiesListByName(input.toString());
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        //Détection d'un clique sur la liste de proposition de ville
        city_inputText.setOnItemClickListener{ parent, position, view, id ->
            Log.d("DebugLog", "CLICK")
            selectCity()
        }

    }

    //endregion init

    fun DisplayCitiesPickList(citiesList: List<CityJsonObject>?) {

        if (citiesList == null || citiesList.size == 0)
            return
        citiesNameList.clear()
        citiesCodeList.clear()

        for (city in citiesList) {
            citiesNameList.add(city.nom)
            citiesCodeList.add(city.code)
        }

        var test: Array<String> = citiesNameList.toTypedArray()

        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, test).also { adapter ->
            city_inputText.setAdapter(adapter)
        }
    }



    //region appel API's

    fun GetCitiesListByName(input: String){
        clientGeoAPI.service.getCityCodeWithName(input).enqueue(object : Callback<List<CityJsonObject>> {
            override fun onResponse(call: Call<List<CityJsonObject>>, response: Response<List<CityJsonObject>>) {
                citiesList = response.body()

                if(citiesList == null)
                    Log.d("API", "error, response.body is empty")

                if(citiesList?.size == 0)
                    displayNoCityAvailable()

                citiesList?.let {
                    DisplayCitiesPickList(citiesList)
                    //debug
                    Log.d("", "SUCCESS")
                }
            }
            override fun onFailure(call: Call<List<CityJsonObject>>, t: Throwable) {
                Log.e("REG", "Error : $t")
            }
        })
    }

    fun getServiceDetails(cityCode:String, typeService:String) {
        //On accède ici par le choix d'une ville ET d'un type de service dans les spinners appropriés
        ClientServicesApi().service.getServiceInCity(cityCode, typeService).enqueue(object : Callback<ServiceJsonObject> {

            override fun onResponse(call: Call<ServiceJsonObject>, response: Response<ServiceJsonObject>) {
                val servicesList = response.body()

                servicesList?.let {
                    Log.d("", "SUCCESS")

                    var infoTextView = findViewById<TextView>(R.id.info_entite)
                    var adresse = servicesList.features[0].properties.adresses[0]
                    infoTextView.text = adresse.lignes[0] +" " + adresse.codePostal + " " + adresse.commune
                    var layout_entite = findViewById<TextView>(R.id.info_entite)
                    layout_entite.visibility = View.VISIBLE
                }
            }
            override fun onFailure(call: Call<ServiceJsonObject>, t: Throwable) {
                Log.e("REG", "Error : $t")
            }
        })
    }

    //endregion

    //region select city and service

    fun selectCity(){
        selectedCity_code = getCityCodeByName()
    }

    fun launchResearch() {
        val cityName = input_cityName.text.toString()
        if(cityName.isNotEmpty()){
            var cityCode = getCityCodeByName()
            if(cityCode=="") {
                //Afficher: la ville n'existe pas
            }
            else {
                getServiceDetails(cityCode, spinner_services.selectedItem.toString())
            }
        }
    }

    fun getCityCodeByName(): String {
        var cityNameInput = city_inputText.text.toString()
        var cityCode: String = ""
        for(i in 0..citiesNameList.size-1)
        {
            if(citiesNameList[i].equals(cityNameInput))
                cityCode=citiesCodeList[i]
        }
        selectedCity.text = cityNameInput +" "+ cityCode
        selectedCity.visibility = View.VISIBLE
        return cityCode
    }

    //On verifie si le code est de la bonne longueur et est composé de caractères numériques
    fun checkCityPostalCodeValidity(code: String):Boolean {
        if(code.length!=5)
            return false

        for(char in code)
        {
            if(char.code <48 || char.code>57)
                return false
        }
        return true
    }

    //endregion select city and service

    //region displaying
    fun displayServiceDetails()
    {

    }

    fun displayNoCityAvailable()
    {
        //Afficher un message comme quoi la recherche n'a pas aboutie avec les données renseignées
    }

    //endregion displaying

    //region TEST et DEBUG
    fun displayPostalCode(jsonObject: CityJsonObject?){
        input_cityCode.text = jsonObject?.code
    }

    fun displayCityname(jsonObject: CityJsonObject){
        input_cityName.text = jsonObject?.nom
    }

    //endregion TEST et DEBUG
}

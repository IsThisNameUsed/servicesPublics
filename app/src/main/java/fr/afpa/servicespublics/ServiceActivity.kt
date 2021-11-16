package fr.afpa.servicespublics

import android.content.Context
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
import fr.afpa.servicespublics.metier.Service
import kotlinx.android.synthetic.main.activity_service.*


class ServiceActivity : AppCompatActivity() {

    //region init
    var clientGeoAPI = ClientGeoApi()
    var clientServiceAPI = ClientServicesApi()
    lateinit var input_cityName : AutoCompleteTextView
    lateinit var input_cityCode : TextView
    lateinit var selectedCity : TextView
    lateinit var service_details : TextView
    lateinit var scroll_view_info : ScrollView

    var typeServicesList = listOf("apec", "pole_emploi", "gendarmerie", "commissariat_police", "mairie", "prefecture", "sous-pref")
    var typeServicesParisList = listOf("apec", "pole_emploi", "gendarmerie", "commissariat_police",
        "paris_mairie", "paris_mairie_arrondissement", "paris_ppp", "prefecture", "sous-pref")
    lateinit var adapterServices :  ArrayAdapter<String>
    lateinit var adapterEntites :  ArrayAdapter<String>
    lateinit var arrayAdapterCity : ArrayAdapter<String>

    /* Récupère les Spinners existants  */
    lateinit var spinner_services : Spinner
    lateinit var spinner_entites : Spinner

    var citiesList: List<CityJsonObject>? = null
    var citiesCodeList = arrayListOf<String>()
    var citiesNameList = arrayListOf<String>()

    var selectedCity_code: String =""
    var servicesList : ServiceJsonObject? = null

    fun SetAdapterServicesToParis(){
        adapterServices = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeServicesParisList)
        adapterServices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_services.setAdapter(adapterServices)
    }

    fun SetAdapterSericesToDefault(){
        adapterServices = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeServicesList)
        adapterServices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_services.setAdapter(adapterServices)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        /* Récupère les Spinners existants  */
        spinner_services = findViewById<Spinner>(R.id.spinner_services)
        spinner_entites = findViewById<Spinner>(R.id.spinner_entites)

        /* Créé les ArrayAdapters qui vont servir à voir les données sous forme de Spinner */
        adapterEntites = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item)
        adapterServices = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeServicesList)
        adapterServices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_services.setAdapter(adapterServices)

        //spinner_services.adapter = adapterServices
        spinner_entites.adapter = adapterEntites

        selectedCity = findViewById(R.id.label_ville_code)
        service_details = findViewById(R.id.info_entite)
        input_cityName = findViewById(R.id.city_inputText)
        input_cityName.threshold = 1
        scroll_view_info = findViewById(R.id.scroll_view_info)

        input_cityName.setOnClickListener {
            GetCitiesListByName(input_cityName.text.toString())
        }

        //Installe un TextWatcher pour obtenir des callbacks dès que le texte est modifié dans le champs de saisie
        input_cityName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(input: CharSequence, start: Int, before: Int, count: Int) {
                GetCitiesListByName(input.toString());
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        //Détection d'un clique sur la liste de proposition de ville
        input_cityName.setOnItemClickListener{ parent, position, view, id ->
            selectCity()
            launchResearch()
        }

        spinner_services.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getServiceDetails(selectedCity_code, spinner_services.selectedItem.toString())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        /* TODO A finir si possible Pierre, bug subsistant
         * servicesList!!.features[spinner_entites.selectedItemPosition] cette ligne n'est pas safe, ça ne récupère pas à 100% le bon service
         * j'ai passé servicesList en variable plutôt qu'en constante
         * il est 4h33, je suis au bout de ma vie, je te laisse t'en charger
         * Très peu de chance que je sois là pour la classe virtuelle
         */
        spinner_entites.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                displayServiceDetails(servicesList!!.features[spinner_entites.selectedItemPosition])
                adapterEntites.notifyDataSetChanged()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
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

        /* Créer un ArrayAdapter avec la liste des villes, ajoute l'adapter créé à l'input d'affichage de la ville et notifie des changements de données */
        arrayAdapterCity = ArrayAdapter(this, android.R.layout.simple_list_item_1, citiesNameList.toTypedArray())
        input_cityName.setAdapter(arrayAdapterCity)
        arrayAdapterCity.notifyDataSetChanged()
    }



    //region appel API's

    fun GetCitiesListByName(input: String){
        clientGeoAPI.service.getCityCodeWithName(input).enqueue(object : Callback<List<CityJsonObject>> {
            override fun onResponse(call: Call<List<CityJsonObject>>, response: Response<List<CityJsonObject>>) {
                citiesList = response.body()

                if(citiesList == null)
                    Log.d("API", "error, response.body is empty")
                else if(citiesList?.size == 0)
                    displayNoCityAvailable()
                else {
                    citiesList?.let {
                        DisplayCitiesPickList(citiesList)
                        //debug
                        Log.d("", "SUCCESS")
                    }
                }
            }
            override fun onFailure(call: Call<List<CityJsonObject>>, t: Throwable) {
                Log.e("REG", "Error : $t")
            }
        })
    }

    fun getServiceDetails(cityCode:String, typeService:String) {
        cleanServiceDetails()
        ClientServicesApi().service.getServiceInCity(cityCode, typeService).enqueue(object : Callback<ServiceJsonObject> {
            override fun onResponse(call: Call<ServiceJsonObject>, response: Response<ServiceJsonObject>) {
                servicesList = response.body()

                servicesList?.let {
                    Log.d("", "SUCCESS")
                    if(servicesList!!.features.size==0) {
                        //TODO afficher pas de services de ce type
                    }
                    else if(servicesList!!.features.size==1) {
                        cleanEntitiesSpinner()
                        displayServiceDetails(servicesList!!.features[0])
                    }
                    else{
                        //TODO afficher les différents services dans le spinner entite
                        fillEntitiesSpinner(servicesList!!)
                    }
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
        if(selectedCity_code=="75056")
            SetAdapterServicesToParis()
        else SetAdapterSericesToDefault()
    }

    fun fillEntitiesSpinner(serviceList: ServiceJsonObject){
        var entitiesList = arrayListOf<String>()
        for(service in serviceList.features){
            entitiesList.add(service.properties.nom)
        }
        adapterEntites = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, entitiesList)
        adapterEntites.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_entites.setAdapter(adapterEntites)
        adapterEntites.notifyDataSetChanged()
        spinner_entites.visibility = View.VISIBLE
    }

    fun cleanEntitiesSpinner(){
        adapterEntites.clear()
        spinner_entites.visibility = View.INVISIBLE
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
    fun displayServiceDetails(service: Service)
    {
        var infoTextView = findViewById<TextView>(R.id.info_entite)
        var adresse = service.properties.adresses[0]
        var nom = service.properties.nom
        var url = service.properties.url
        val phone = service.properties.telephone

        infoTextView.text = nom + "\n"+ adresse.lignes[0] +" " + adresse.codePostal + " " + adresse.commune + "\n"+ "site web: " + url + "\n"+ "téléphone: " + phone + "\n"

        findViewById<View>(R.id.layout_entite).visibility = View.VISIBLE
    }

    fun cleanServiceDetails(){
        var infoTextView = findViewById<TextView>(R.id.info_entite)
        infoTextView.text = ""
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
        //input_cityName.text = jsonObject?.nom
    }

    //endregion TEST et DEBUG
}

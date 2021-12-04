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
import fr.afpa.servicespublics.metier.Service
import kotlinx.android.synthetic.main.activity_service.*


class ServiceActivity : AppCompatActivity() {

    //region init
    var clientGeoAPI = ClientGeoApi()
    var clientServiceAPI = ClientServicesApi()
    lateinit var inputCityName : AutoCompleteTextView
    lateinit var selectedCity : TextView
    lateinit var serviceDetails : TextView
    lateinit var scrollViewInfo : ScrollView

    var typeServicesList = listOf("apec", "pole_emploi", "gendarmerie", "commissariat_police", "mairie", "prefecture", "sous-pref")
    var typeServicesParisList = listOf("apec", "pole_emploi", "gendarmerie", "commissariat_police",
        "paris_mairie", "paris_mairie_arrondissement", "paris_ppp", "prefecture", "sous-pref")
    lateinit var adapterServices :  ArrayAdapter<String>
    lateinit var adapterEntites :  ArrayAdapter<String>
    lateinit var arrayAdapterCity : ArrayAdapter<String>

    /* Récupère les Spinners existants  */
    lateinit var spinnerServices : Spinner
    lateinit var spinnerEntites : Spinner

    var citiesList: List<CityJsonObject>? = null
    var citiesCodeList = arrayListOf<String>()
    var citiesNameList = arrayListOf<String>()

    var selectedCityCode: String = ""
    var servicesList : ServiceJsonObject? = null

    fun setAdapterServicesToParis(){
        adapterServices = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeServicesParisList)
        spinner_services.adapter = adapterServices
    }

    fun setAdapterServicesToDefault(){
        adapterServices = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeServicesList)
        spinner_services.adapter = adapterServices
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        /* Récupère les Spinners existants  */
        spinnerServices = findViewById(R.id.spinner_services)
        spinnerEntites = findViewById(R.id.spinner_entites)

        /* Créé les ArrayAdapters qui vont servir à voir les données sous forme de Spinner */
        adapterEntites = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item)
        adapterServices = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeServicesList)
        spinnerServices.adapter = adapterServices

        //spinner_services.adapter = adapterServices
        spinnerEntites.adapter = adapterEntites

        selectedCity = findViewById(R.id.label_ville_code)
        serviceDetails = findViewById(R.id.info_entite)
        inputCityName = findViewById(R.id.city_inputText)
        inputCityName.threshold = 1
        scrollViewInfo = findViewById(R.id.scroll_view_info)

        inputCityName.setOnClickListener {
            if (inputCityName.text.isNotEmpty() && inputCityName.text.matches("[0-9]{5}".toRegex()))
                getCitiesListByCode(inputCityName.text.toString())
            else if (inputCityName.text.isNotEmpty())
                getCitiesListByName(inputCityName.text.toString())
        }

        //Installe un TextWatcher pour obtenir des callbacks dès que le texte est modifié dans le champs de saisie
        inputCityName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(input: CharSequence, start: Int, before: Int, count: Int) {
                if (input.isNotEmpty() && input.matches("[0-9]{5}".toRegex()))
                    getCitiesListByCode(input.toString())
                else if (input.isNotEmpty())
                    getCitiesListByName(input.toString())
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        //Détection d'un clique sur la liste de proposition de ville
        inputCityName.setOnItemClickListener{ parent, position, view, id ->
            selectCity()
            launchResearch()
        }

        spinnerServices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getServiceDetails(selectedCityCode, spinnerServices.selectedItem.toString())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spinnerEntites.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                displayServiceDetails(servicesList!!.features[spinnerEntites.selectedItemPosition])
                adapterEntites.notifyDataSetChanged()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    //endregion init

    fun displayCitiesPickList(citiesList: List<CityJsonObject>?) {
        if (citiesList != null && citiesList.isNotEmpty()) {
            citiesNameList.clear()
            citiesCodeList.clear()
            for (city in citiesList) {
                citiesNameList.add(city.nom)
                citiesCodeList.add(city.code)
            }
            /* Créer un ArrayAdapter avec la liste des villes, ajoute l'adapter créé à l'input d'affichage de la ville et notifie des changements de données */
            arrayAdapterCity = ArrayAdapter(this, android.R.layout.simple_list_item_1, citiesNameList.toTypedArray())
            inputCityName.setAdapter(arrayAdapterCity)
            arrayAdapterCity.notifyDataSetChanged()
        }
    }

    //region appel API's

    fun getCitiesListByName(input: String){
        clientGeoAPI.service.getCityCodeWithName(input).enqueue(object : Callback<List<CityJsonObject>> {
            override fun onResponse(call: Call<List<CityJsonObject>>, response: Response<List<CityJsonObject>>) {
                citiesList = response.body()

                if(citiesList == null)
                    Log.d("API", "error, response.body is empty")
                else if(citiesList?.size == 0)
                    displayNoCityAvailable()
                else {
                    citiesList?.let {
                        displayCitiesPickList(citiesList)
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

    fun getCitiesListByCode(input: String){
        clientGeoAPI.service.getCityCodeWithPostalCode(input).enqueue(object : Callback<List<CityJsonObject>> {
            override fun onResponse(call: Call<List<CityJsonObject>>, response: Response<List<CityJsonObject>>) {
                citiesList = response.body()

                if(citiesList == null)
                    Log.d("API", "error, response.body is empty")
                else if(citiesList?.size == 0)
                    displayNoCityAvailable()
                else {
                    citiesList?.let {
                        displayCitiesPickList(citiesList)
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
        //On accède ici par le choix d'une ville ET d'un type de service
        cleanServiceDetails()
        clientServiceAPI.service.getServiceInCity(cityCode, typeService).enqueue(object : Callback<ServiceJsonObject> {
            override fun onResponse(call: Call<ServiceJsonObject>, response: Response<ServiceJsonObject>) {
                servicesList = response.body()
                servicesList?.let {
                    Log.d("", "SUCCESS")
                    if (servicesList!!.features.isEmpty()) {
                        //TODO afficher pas de services de ce type
                    }
                    else if (servicesList!!.features.size == 1) {
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
    fun selectCity() {
        selectedCityCode = getCityCodeByName()
        if (selectedCityCode == "75056")
            setAdapterServicesToParis()
        else
            setAdapterServicesToDefault()
    }

    fun fillEntitiesSpinner(serviceList: ServiceJsonObject) {
        var entitiesList = arrayListOf<String>()
        for (service in serviceList.features)
            entitiesList.add(service.properties.nom)
        adapterEntites = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, entitiesList)
        spinnerEntites.adapter = adapterEntites
        adapterEntites.notifyDataSetChanged()
        spinner_entites.visibility = View.VISIBLE
    }

    fun cleanEntitiesSpinner() {
        adapterEntites.clear()
        spinner_entites.visibility = View.INVISIBLE
    }

    fun launchResearch() {
        val cityName = inputCityName.text.toString()
        if (cityName.isNotEmpty()) {
            var cityCode = getCityCodeByName()
            if (cityCode.isNotEmpty()) {
                getServiceDetails(cityCode, spinnerServices.selectedItem.toString())
            }
        }
    }

    fun getCityCodeByName(): String {
        var cityName = city_inputText.text.toString()
        var cityCode: String = ""
        for (i in 0..citiesNameList.size-1)
        {
            if(citiesNameList[i].equals(cityName))
                cityCode=citiesCodeList[i]
        }
        selectedCity.text = cityName + " " + cityCode
        selectedCity.visibility = View.VISIBLE
        return cityCode
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
        TODO("Afficher un message comme quoi la recherche n'a pas aboutie avec les données renseignées")
    }

    //endregion displaying
}

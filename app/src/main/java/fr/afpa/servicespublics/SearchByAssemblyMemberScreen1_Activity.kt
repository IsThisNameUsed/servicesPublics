package fr.afpa.servicespublics

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import fr.afpa.servicespublics.api.ClientAssembleApi
import fr.afpa.servicespublics.metier.*
import kotlinx.android.synthetic.main.search_by_assemblymember_screen1.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchByAssemblyMemberScreen1_Activity: AppCompatActivity() {

    var clientAssembleApi = ClientAssembleApi()

    var deputyNameMap = hashMapOf<String, Int>()
    var autoCompletionNameList = arrayListOf<String>()
    var deputiesIdNameList = arrayListOf<String>()
    var deputyTenLastVoteList = arrayListOf<voteContainer>()

    //A button used through different screens
    lateinit var multiTaskButton: Button
    //Search screen
    lateinit var textInput : AutoCompleteTextView
    lateinit var arrayAdapterName :  ArrayAdapter<String>
    lateinit var resultsTable : TableLayout
    lateinit var searchLayout: RelativeLayout
    //Deputy details screen
    lateinit var deputyDetailsLayout : RelativeLayout
    lateinit var deputyNameTextView: TextView
    lateinit var deputyJobTextView: TextView
    lateinit var deputyGroupeNameTextView: TextView
    lateinit var circonscriptionDetails: TextView
    lateinit var deputyMailTextView: TextView
    lateinit var deputyWebSiteTextView : TextView
    //vote details screen
    lateinit var voteDetailsLayout: RelativeLayout
    lateinit var voteTitleTextView: TextView
    lateinit var nbVoteForTextView: TextView
    lateinit var nbVoteAgainstTextView: TextView
    lateinit var nbVotersTextView: TextView
    lateinit var nbAbstentionTextView: TextView

    var searchByDeputyMode = true
    var selectedDeputyIdName= ""

    enum class InterfaceMode{deputyDetails, voteDetails, searchScreen}
    var interfaceMode = InterfaceMode.searchScreen

    //region init
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_by_assemblymember_screen1)
        //Setup buttons
        val home_button = findViewById<Button>(R.id.home_button)
        home_button.setOnClickListener {
            startActivity(Intent(this, NationalAssembly_Menu_Activity::class.java))
        }

        multiTaskButton = findViewById<Button>(R.id.multiTaskButton)
        multiTaskButton.visibility = View.INVISIBLE
        multiTaskButton.setOnClickListener {
            if(interfaceMode==InterfaceMode.searchScreen && searchByDeputyMode==true)
                callGetDeputyDetails()
            else if(interfaceMode==InterfaceMode.deputyDetails || interfaceMode==InterfaceMode.voteDetails)
                displaySearchScreenInterface()
        }

        //Get views
        deputyDetailsLayout = findViewById(R.id.deputyDetails)
        textInput = findViewById(R.id.name_inputField)
        resultsTable = findViewById(R.id.details_vote_table)
        deputyNameTextView = findViewById(R.id.deputyName)
        deputyGroupeNameTextView = findViewById(R.id.deputyGroupeName)
        deputyJobTextView = findViewById(R.id.deputyJob)
        deputyGroupeNameTextView = findViewById(R.id.deputyGroupeName)
        circonscriptionDetails = findViewById(R.id.circonscriptionDetails)
        deputyMailTextView = findViewById(R.id.deputyMail)
        deputyWebSiteTextView = findViewById(R.id.deputyWebSite)
        searchLayout = findViewById(R.id.searchLayout)
        voteDetailsLayout = findViewById(R.id.voteDetails)
        voteTitleTextView = findViewById(R.id.voteTitle)
        nbVoteForTextView = findViewById(R.id.nbVoteFor)
        nbVoteAgainstTextView = findViewById(R.id.nbVoteAgainst)
        nbVotersTextView = findViewById(R.id.nbVoters)
        nbAbstentionTextView = findViewById(R.id.nbAbstention)

        //Installe un TextWatcher pour obtenir des callbacks dès que le texte est modifié dans le champs de saisie du nom député
        textInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(input: CharSequence, start: Int, before: Int, count: Int) {
                //fillSortedDeputiesNameList(input)
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        //Détection d'un clique sur la liste de proposition de ville
        textInput.setOnItemClickListener{ parent, position, view, id ->
            val name = arrayAdapterName.getItem(id.toInt())
            selectDeputy(name)
        }

        getDeputyList()
    }

    //endregion init

    //region api
    fun getDeputyDetails(){
        clientAssembleApi.service.getDeputyDetails(selectedDeputyIdName).enqueue(object : Callback<deputyDetailsContainer> {
            override fun onResponse(call: Call<deputyDetailsContainer>, response: Response<deputyDetailsContainer>) {
                var deputyJsonObject = response.body()
                Log.d("API", "getDeputyDetails")
                if(deputyJsonObject == null)
                    Log.d("API", "error, response.body is empty")
                else {
                    deputyJsonObject?.let {
                        val deputyDetails = deputyJsonObject?.depute
                        displayDeputyDetails(deputyDetails)
                        Log.d("API", deputyJsonObject?.depute?.nom)
                        Log.d("API", deputyJsonObject?.depute?.profession)
                        Log.d("API", deputyJsonObject?.depute?.emails[0]?.email)
                        //debug
                        Log.d("API", "SUCCESS")
                    }
                }
            }
            override fun onFailure(call: Call<deputyDetailsContainer>, t: Throwable) {
                TODO("Not yet implemented")
                Log.d("API", "Failure")
            }
        })
    }

    fun getDeputyList(){
        clientAssembleApi.service.getDeputyList().enqueue(object : Callback<assemblyDeputiesListJson> {
            override fun onResponse(call: Call<assemblyDeputiesListJson>, response: Response<assemblyDeputiesListJson>) {
                var assemblyJsonObject = response.body()

                if(assemblyJsonObject == null)
                    Log.d("API", "error, response.body is empty")

                else {
                    assemblyJsonObject?.let {
                        fillDeputiesNameList(assemblyJsonObject)
                        //debug
                        Log.d("", "SUCCESS")
                    }
                }
            }
            override fun onFailure(call: Call<assemblyDeputiesListJson>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getDeputyVoteList(name: String){
        clientAssembleApi.service.getDeputyVoteList(name).enqueue(object : Callback<deputyVoteList> {
            override fun onResponse(call: Call<deputyVoteList>, response: Response<deputyVoteList>) {
                var voteList = response.body()

                if(voteList == null)
                    Log.d("API", "error, response.body is empty")
                else {
                    voteList?.let {
                        fillDeputyTenLastVoteList(voteList)
                        fillDeputyLastVoteTable(voteList)
                        Log.d("", "SUCCESS")
                    }
                }
            }
            override fun onFailure(call: Call<deputyVoteList>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    //endregion api

    //region research and get results
    fun callGetDeputyDetails(){
        displayDeputyDetailsInterface()
        Log.d("DEBUG","selectedDeputyIdName "+ selectedDeputyIdName)
        if(selectedDeputyIdName!="")
            getDeputyDetails()
        else{
            //Afficher un message?
        }
    }

    fun displayDeputyDetails(deputyJsonObject: deputyDetails){
        deputyNameTextView.text = deputyJsonObject?.nom
        deputyJobTextView.text = deputyJsonObject?.profession
        deputyWebSiteTextView.text = deputyJsonObject?.sites_web[0].site
        deputyMailTextView.text = deputyJsonObject?.emails[0]?.email
        deputyGroupeNameTextView.text = deputyJsonObject?.groupe_sigle +" " +  deputyJsonObject?.parti_ratt_financier
        circonscriptionDetails.text = deputyJsonObject?.nom_circo + " " + deputyJsonObject?.num_deptmt
    }

    fun fillDeputyTenLastVoteList(voteList:deputyVoteList)
    {
        var endIndex = voteList.votes.size - 1
        var startIndex = endIndex - 10
        if(startIndex<0)
            startIndex = 0

        for(i in startIndex..endIndex) {
            deputyTenLastVoteList.add(voteList.votes[i])
        }

    }
    fun fillDeputyLastVoteTable(voteList:deputyVoteList){

        var endIndex = voteList.votes.size - 1
        var startIndex = endIndex - 10
        if(startIndex<0)
            startIndex = 0

        val li = LayoutInflater.from(applicationContext)
        val row = li.inflate(R.layout.raw_assembly_member2, null)

        for(i in startIndex..endIndex){
            val row = li.inflate(R.layout.raw_assembly_member2, null)
            row.findViewById<TextView>(R.id.titreVoteColumn).text = voteList.votes[i].vote.scrutin.titre
            row.findViewById<TextView>(R.id.voteColumn).text = voteList.votes[i].vote.position
            row.findViewById<TextView>(R.id.idColumn).text = voteList.votes[i].vote.scrutin.numero
            resultsTable.addView(row)

            row.setOnClickListener(View.OnClickListener { row ->
                var test = row.findViewById<TextView>(R.id.voteColumn).text
                displayVoteDetails(row.findViewById<TextView>(R.id.idColumn).text.toString())
            })
        }
    }

    fun displayVoteDetails(id: String){

        var voteFinded = false
        for(voteContainer in deputyTenLastVoteList){
            if(id==voteContainer.vote.scrutin.numero)
            {
                voteFinded = true
                var scrutin = voteContainer.vote.scrutin
                displayVoteDetailsInterface()
                voteTitleTextView.text = scrutin.titre
                nbVoteForTextView.text = scrutin.nombre_pours
                nbVoteAgainstTextView.text = scrutin.nombre_contres
                nbVotersTextView.text = scrutin.nombre_votants
                nbAbstentionTextView .text = scrutin.nombre_votants
            }
        }

        if(voteFinded==false)
        {
            //Display error vote not existing
        }
    }

    //Rempli la liste de tous les députés de l'assemblé qui sert de référence
    fun fillDeputiesNameList(deputiesList:assemblyDeputiesListJson) {
        for(i in 0..deputiesList.deputes.size-1){
            deputyNameMap.put(deputiesList.deputes[i].depute.nom, i)
            deputiesIdNameList.add(deputiesList.deputes[i].depute.slug)
        }

        for((key,value) in deputyNameMap){
                autoCompletionNameList.add(key)
        }

        arrayAdapterName = ArrayAdapter(this, android.R.layout.simple_list_item_1, autoCompletionNameList.toTypedArray())
        textInput.setAdapter(arrayAdapterName)
        arrayAdapterName.notifyDataSetChanged()
    }

    fun cleanResultsTable(){
        resultsTable.removeAllViews()
    }

    fun selectDeputy(name:String?) {
        if(name == null || name.isEmpty())
            return

        selectedDeputyIdName = getDeputyIdName(name)

        if(selectedDeputyIdName=="")
        {
            //Display an error msg
        }
        else
        {
            getDeputyVoteList(selectedDeputyIdName)
            multiTaskButton.visibility = View.VISIBLE
            multiTaskButton.text = "Détails député"
        }
    }

    //IdName is named slug on the API, it is the reference ID for a call on a specific deputy
    fun getDeputyIdName(name:String): String{
        var indice = deputyNameMap.get(name)

        if(indice==null)
            return ""
        return deputiesIdNameList[indice]
    }
    //endregion research and get results

    //region interface
    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.searchDeputyRadio ->
                    if (checked) {
                        searchByDeputyMode = true
                        textInput.hint = "nom député"
                    }
                R.id.searchVoteRadio ->
                    if (checked) {
                        searchByDeputyMode = false
                        textInput.hint = "nom vote"
                    }
            }
        }
    }

    fun displayVoteDetailsInterface(){
        interfaceMode = InterfaceMode.voteDetails
        resultsTable.visibility = View.INVISIBLE
        deputyDetailsLayout.visibility = View.INVISIBLE
        searchLayout.visibility = View.INVISIBLE
        voteDetailsLayout.visibility = View.VISIBLE
        multiTaskButton.visibility = View.VISIBLE
        multiTaskButton.text = "return"


    }

    fun displaySearchScreenInterface() {
        interfaceMode=InterfaceMode.searchScreen
        resultsTable.visibility = View.VISIBLE
        deputyDetailsLayout.visibility = View.INVISIBLE
        searchLayout.visibility = View.VISIBLE
        voteDetailsLayout.visibility = View.INVISIBLE
        multiTaskButton.text = "détails député"

    }

    fun displayDeputyDetailsInterface() {
        interfaceMode= InterfaceMode.deputyDetails
        resultsTable.visibility = View.INVISIBLE
        deputyDetailsLayout.visibility = View.VISIBLE
        searchLayout.visibility = View.INVISIBLE
        voteDetailsLayout.visibility = View.INVISIBLE
        multiTaskButton.visibility = View.VISIBLE
        multiTaskButton.text = "return"

    }
    //endregion interface
}
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchByAssemblyMemberScreen1Activity: AppCompatActivity() {

    private var clientAssembleApi = ClientAssembleApi()

    private var deputyNameMap = hashMapOf<String, Int>()
    private var autoCompletionNameList = arrayListOf<String>()
    private var deputiesIdNameList = arrayListOf<String>()
    private var deputyTenLastVoteList = arrayListOf<voteContainer>()

    private lateinit var loadBar: ProgressBar
    //A button used through different screens
    private lateinit var multiTaskButton: Button
    //Search screen
    private lateinit var textInput : AutoCompleteTextView
    private lateinit var arrayAdapterName :  ArrayAdapter<String>
    private lateinit var resultsTable : TableLayout
    private lateinit var searchLayout: RelativeLayout
    //Deputy details screen
    private lateinit var deputyDetailsLayout : RelativeLayout
    private lateinit var deputyNameTextView: TextView
    private lateinit var deputyJobTextView: TextView
    private lateinit var deputyGroupNameTextView: TextView
    private lateinit var circonscriptionDetails: TextView
    private lateinit var deputyMailTextView: TextView
    private lateinit var deputyWebSiteTextView : TextView
    //vote details screen
    private lateinit var voteDetailsLayout: RelativeLayout
    private lateinit var voteTitleTextView: TextView
    private lateinit var nbVoteForTextView: TextView
    private lateinit var nbVoteAgainstTextView: TextView
    private lateinit var nbVotersTextView: TextView
    private lateinit var nbAbstentionTextView: TextView

    private var searchByDeputyMode = true
    private var selectedDeputyIdName= ""

    enum class InterfaceMode{DeputyDetails, VoteDetails, SearchScreen}
    private var interfaceMode = InterfaceMode.SearchScreen

    //region init
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_by_assemblymember_screen1)
        //Setup buttons
        val homeButton = findViewById<Button>(R.id.home_button)
        homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        multiTaskButton = findViewById(R.id.multiTaskButton)
        multiTaskButton.visibility = View.INVISIBLE
        multiTaskButton.setOnClickListener {
            if(interfaceMode==InterfaceMode.SearchScreen && searchByDeputyMode)
                callGetDeputyDetails()
            else if(interfaceMode==InterfaceMode.DeputyDetails || interfaceMode==InterfaceMode.VoteDetails)
                displaySearchScreenInterface()
        }

        //Get views
        deputyDetailsLayout = findViewById(R.id.deputyDetails)
        textInput = findViewById(R.id.name_inputField)
        resultsTable = findViewById(R.id.details_vote_table)
        deputyNameTextView = findViewById(R.id.deputyName)
        deputyGroupNameTextView = findViewById(R.id.deputyGroupeName)
        deputyJobTextView = findViewById(R.id.deputyJob)
        deputyGroupNameTextView = findViewById(R.id.deputyGroupeName)
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
        loadBar = findViewById(R.id.loadDeputyListProgressBar)

        //Installe un TextWatcher pour obtenir des callbacks dès que le texte est modifié dans le champs de saisie du nom du député
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
    private fun getDeputyDetails(){
        clientAssembleApi.service.getDeputyDetails(selectedDeputyIdName).enqueue(object : Callback<deputyDetailsContainer> {
            override fun onResponse(call: Call<deputyDetailsContainer>, response: Response<deputyDetailsContainer>) {
                val deputyJsonObject = response.body()
                Log.d("API", "getDeputyDetails")
                if(deputyJsonObject == null)
                    Log.d("API", "error, response.body is empty")
                else {
                    deputyJsonObject.let {
                        val deputyDetails = deputyJsonObject.depute
                        displayDeputyDetails(deputyDetails)
                        Log.d("API", deputyJsonObject.depute.nom)
                        Log.d("API", deputyJsonObject.depute.profession)
                        Log.d("API", deputyJsonObject.depute.emails[0].email)
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

    private fun getDeputyList(){
        clientAssembleApi.service.getDeputyList().enqueue(object : Callback<assemblyDeputiesListJson> {
            override fun onResponse(call: Call<assemblyDeputiesListJson>, response: Response<assemblyDeputiesListJson>) {
                val assemblyJsonObject = response.body()

                if(assemblyJsonObject == null)
                    Log.d("API", "error, response.body is empty")

                else {
                    assemblyJsonObject.let {
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

    private fun getDeputyVoteList(name: String){
        clientAssembleApi.service.getDeputyVoteList(name).enqueue(object : Callback<deputyVoteList> {
            override fun onResponse(call: Call<deputyVoteList>, response: Response<deputyVoteList>) {
                val voteList = response.body()

                if(voteList == null)
                    Log.d("API", "error, response.body is empty")
                else {
                    voteList.let {
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
    private fun callGetDeputyDetails(){
        displayDeputyDetailsInterface()
        if(selectedDeputyIdName!="")
            getDeputyDetails()
        else{
            TODO("Afficher un message (une erreur est survenue...")
        }
    }

    fun displayDeputyDetails(deputyJsonObject: deputyDetails){
        deputyNameTextView.text = deputyJsonObject.nom
        deputyJobTextView.text = deputyJsonObject.profession
        deputyWebSiteTextView.text = deputyJsonObject.sites_web[0].site
        deputyMailTextView.text = deputyJsonObject.emails[0].email
        deputyGroupNameTextView.text = deputyJsonObject.groupe_sigle +getString(R.string.space) +  deputyJsonObject.parti_ratt_financier
        circonscriptionDetails.text = deputyJsonObject.nom_circo + getString(R.string.space) + deputyJsonObject.num_deptmt
    }

    fun fillDeputyTenLastVoteList(voteList:deputyVoteList)
    {
        val endIndex = voteList.votes.size - 1
        var startIndex = endIndex - 10
        if(startIndex<0)
            startIndex = 0

        for(i in startIndex..endIndex) {
            deputyTenLastVoteList.add(voteList.votes[i])
        }
    }

    fun fillDeputyLastVoteTable(voteList:deputyVoteList){

        val endIndex = voteList.votes.size - 1
        var startIndex = endIndex - 10
        if(startIndex<0)
            startIndex = 0

        val li = LayoutInflater.from(applicationContext)

        for(i in startIndex..endIndex){
            val row = li.inflate(R.layout.raw_assembly_member2, null)
            row.findViewById<TextView>(R.id.titreVoteColumn).text = voteList.votes[i].vote.scrutin.titre
            row.findViewById<TextView>(R.id.voteColumn).text = voteList.votes[i].vote.position
            row.findViewById<TextView>(R.id.idColumn).text = voteList.votes[i].vote.scrutin.numero
            resultsTable.addView(row)
            if(resultsTable.indexOfChild(row)%2==0)
                row.setBackgroundColor(getColor(R.color.raw_Darkgrey))
            else row.setBackgroundColor(getColor(R.color.raw_grey))
            row.setOnClickListener { row ->
                displayVoteDetails(row.findViewById<TextView>(R.id.idColumn).text.toString())
            }
        }
        loadBar.visibility = View.INVISIBLE
        multiTaskButton.visibility = View.VISIBLE
        multiTaskButton.text = getString(R.string.deputyDetailButtonText)
    }

    private fun displayVoteDetails(id: String){

        var voteFinded = false
        for(voteContainer in deputyTenLastVoteList){
            if(id==voteContainer.vote.scrutin.numero)
            {
                voteFinded = true
                val scrutin = voteContainer.vote.scrutin
                displayVoteDetailsInterface()
                voteTitleTextView.text = scrutin.titre
                nbVoteForTextView.text = scrutin.nombre_pours
                nbVoteAgainstTextView.text = scrutin.nombre_contres
                nbVotersTextView.text = scrutin.nombre_votants
                nbAbstentionTextView .text = scrutin.nombre_votants
            }
        }

        if(!voteFinded)
        {
            TODO("Display error vote not existing")
        }
    }

    //Rempli la liste de tous les députés qui sert de référence
    fun fillDeputiesNameList(deputiesList:assemblyDeputiesListJson) {
        for(i in 0 until deputiesList.deputes.size-1){
            deputyNameMap[deputiesList.deputes[i].depute.nom] = i
            deputiesIdNameList.add(deputiesList.deputes[i].depute.slug)
        }

        for((key,value) in deputyNameMap){
                autoCompletionNameList.add(key)
        }

        arrayAdapterName = ArrayAdapter(this, android.R.layout.simple_list_item_1, autoCompletionNameList.toTypedArray())
        textInput.setAdapter(arrayAdapterName)
        arrayAdapterName.notifyDataSetChanged()
        loadBar.visibility = View.INVISIBLE
        searchLayout.visibility = View.VISIBLE
    }

    fun cleanResultsTable(){
        resultsTable.removeAllViews()
    }

    private fun selectDeputy(name:String?) {
        if(name == null || name.isEmpty())
            return

        selectedDeputyIdName = getDeputyIdName(name)

        if(selectedDeputyIdName=="")
        {
            //Display an error msg
        }
        else
        {
            loadBar.visibility = View.VISIBLE
            getDeputyVoteList(selectedDeputyIdName)
        }
    }

    //IdName is named slug on the API, it is the reference ID for a call on a specific deputy
    private fun getDeputyIdName(name:String): String{
        val indice = deputyNameMap[name] ?: return ""
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

    private fun displayVoteDetailsInterface(){
        interfaceMode = InterfaceMode.VoteDetails
        resultsTable.visibility = View.INVISIBLE
        deputyDetailsLayout.visibility = View.INVISIBLE
        searchLayout.visibility = View.INVISIBLE
        voteDetailsLayout.visibility = View.VISIBLE
        multiTaskButton.visibility = View.VISIBLE
        multiTaskButton.text = getString(R.string.mutliTaskButtonReturnText)


    }

    private fun displaySearchScreenInterface() {
        interfaceMode=InterfaceMode.SearchScreen
        resultsTable.visibility = View.VISIBLE
        deputyDetailsLayout.visibility = View.INVISIBLE
        searchLayout.visibility = View.VISIBLE
        voteDetailsLayout.visibility = View.INVISIBLE
        multiTaskButton.text = getString(R.string.deputyDetailButtonText)

    }

    private fun displayDeputyDetailsInterface() {
        interfaceMode= InterfaceMode.DeputyDetails
        resultsTable.visibility = View.INVISIBLE
        deputyDetailsLayout.visibility = View.VISIBLE
        searchLayout.visibility = View.INVISIBLE
        voteDetailsLayout.visibility = View.INVISIBLE
        multiTaskButton.visibility = View.VISIBLE
        multiTaskButton.text = getString(R.string.mutliTaskButtonReturnText)

    }
    //endregion interface
}
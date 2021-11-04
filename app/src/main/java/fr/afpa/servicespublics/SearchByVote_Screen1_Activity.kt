package fr.afpa.servicespublics

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SearchByVote_Screen1_Activity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_by_vote_screen1)

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
            startActivity(Intent(this, SearchByVote_DetailsScreen_Activity::class.java))
        }

        //Get information about ten last votes et fill table rows with them (more votes? paging?)
        var tableLayout = findViewById<TableLayout>(R.id.vote_table)
        val li = LayoutInflater.from(applicationContext)
        val row = li.inflate(R.layout.raw_vote, null)

        for(i in 0..10){
            val row = li.inflate(R.layout.raw_vote, null)
            row.findViewById<Button>(R.id.column0).text = "nom vote"
            //Bind button to link to the details_screen_activity, check performances
            row.findViewById<TextView>(R.id.column1).text = "145"
            row.findViewById<TextView>(R.id.column2).text = "230"
            row.findViewById<TextView>(R.id.column3).text = "52"
            tableLayout.addView(row)
        }
    }
}
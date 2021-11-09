package fr.afpa.servicespublics

import android.content.Intent
import android.os.Bundle
import android.widget.TableLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView


class SearchByVote_DetailsScreen_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_by_vote_details_screen)

        //Setup buttons
        val home_button = findViewById<Button>(R.id.home_button)
        val return_button = findViewById<Button>(R.id.return_button)

        home_button.setOnClickListener {
            startActivity(Intent(this, SearchByVote_Screen1_Activity::class.java))
        }

        return_button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Get informations with the fr.afpa.servicespublics.api about the vote of all members of the assembly and fill rows with them
        var tableLayout = findViewById<TableLayout>(R.id.details_vote_table)
        val li = LayoutInflater.from(applicationContext)
        val row = li.inflate(R.layout.raw_assembly_member1, null)

        for(i in 0..60){
            val row = li.inflate(R.layout.raw_vote, null)
            row.findViewById<TextView>(R.id.column0).text = "depute" + i
            row.findViewById<TextView>(R.id.column1).text = "parti" + i
            row.findViewById<TextView>(R.id.column2).text = "vote" + i
            tableLayout.addView(row)
        }
    }
}
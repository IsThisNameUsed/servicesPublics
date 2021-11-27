package fr.afpa.servicespublics

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NationalAssembly_Menu_Activity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.national_assembly_menu)
        val searchByVote_button = findViewById<Button>(R.id.button_search_by_vote)
        val searchByMemberName_button = findViewById<Button>(R.id.button_search_by_assemblyMemberName)
        val home_button = findViewById<Button>(R.id.home_button)
        val return_button = findViewById<Button>(R.id.multiTaskButton)

        home_button?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        return_button?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        searchByVote_button?.setOnClickListener {
            startActivity(Intent(this, SearchByVote_Screen1_Activity::class.java))
        }

        searchByMemberName_button?.setOnClickListener {
            startActivity(Intent(this,SearchByAssemblyMemberScreen1_Activity::class.java))
        }
    }
}
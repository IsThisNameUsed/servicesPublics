package fr.afpa.servicespublics

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SearchByAssemblyMemberInfoVote_Activity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_by_assemblymember_infovote)

        //Setup buttons
        val home_button = findViewById<Button>(R.id.home_button)
        val return_button = findViewById<Button>(R.id.multiTaskButton)

        home_button.setOnClickListener {
            startActivity(Intent(this, SearchByAssemblyMemberScreen1Activity::class.java))
        }

        return_button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
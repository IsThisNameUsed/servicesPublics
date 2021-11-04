package fr.afpa.servicespublics

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import fr.afpa.servicespublics.R
import fr.afpa.servicespublics.ServiceActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val boutonService = findViewById<Button>(R.id.bouton_services)
        boutonService.setOnClickListener {
            startActivity(Intent(this, ServiceActivity::class.java))
        }
    }
}
package com.juanjoselopera.proy_prog_mobile.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.juanjoselopera.proy_prog_mobile.R

class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvWelcomeUser = findViewById<TextView>(R.id.tvWelcomeUser)
        val usuario = intent.getStringExtra("usuario") ?: "Juan"
        tvWelcomeUser.text = "Hola, $usuario 👋"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val subjectsCard = findViewById<CardView>(R.id.SubjectsCard)
        val notesCard = findViewById<CardView>(R.id.notesCard)
        val transcriptionsCard = findViewById<CardView>(R.id.TranscriptionsCard)
        val keyIdeasCard = findViewById<CardView>(R.id.KeyIdeasCard)
        val cardApuntes = findViewById<CardView>(R.id.CardApuntes)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_subjects -> {
                    startActivity(Intent(this, MateriasActivity::class.java))
                    true
                }
                R.id.nav_notes -> {
                    startActivity(Intent(this, ApuntesActivity::class.java))
                    true
                }
                R.id.nav_ai -> {
                    startActivity(Intent(this, AIActivity::class.java))
                    true
                }
                else -> false
            }
        }

        subjectsCard.setOnClickListener {
            startActivity(Intent(this, MateriasActivity::class.java))
        }

        notesCard.setOnClickListener {
            startActivity(Intent(this, ApuntesActivity::class.java))
        }

        transcriptionsCard.setOnClickListener {
            // Asumiendo que va a Apuntes o una actividad similar si no existe una específica
            startActivity(Intent(this, ApuntesActivity::class.java))
        }

        keyIdeasCard.setOnClickListener {
             startActivity(Intent(this, AIActivity::class.java))
        }

        cardApuntes.setOnClickListener {
            startActivity(Intent(this, ApuntesActivity::class.java))
        }
    }
}

package com.juanjoselopera.proy_prog_mobile.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.juanjoselopera.proy_prog_mobile.R
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsuario = findViewById<TextInputEditText>(R.id.useremail)
        val etPassword = findViewById<TextInputEditText>(R.id.userpassword)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val tvMensaje = findViewById<TextView>(R.id.tvMensaje)

        btnIngresar.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()
            when {
                usuario.isEmpty() || password.isEmpty() -> {
                    // Caso: campos vacíos
                    tvMensaje.text = "Completá todos los campos antes de continuar."
                    tvMensaje.setTextColor(Color.parseColor("#B91C1C"))
                    tvMensaje.visibility = View.VISIBLE
                }
                usuario == "admin" && password == "1234" -> {
                    val intent = Intent(this, LandingActivity::class.java)
                    intent.putExtra("usuario", usuario) // Enviar el nombre de usuario
                    startActivity(intent)
                }
                else -> {
                    // Caso: credenciales incorrectas
                    tvMensaje.text = "Usuario o contraseña incorrectos. Revisá los datos."
                    tvMensaje.setTextColor(Color.parseColor("#B91C1C"))
                    tvMensaje.visibility = View.VISIBLE
                    // etPassword.text.clear() // Limpiar el campo de contraseña
                }
            }


        }
    }
}

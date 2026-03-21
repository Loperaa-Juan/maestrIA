package com.juanjoselopera.proy_prog_mobile.app.ui.signup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.juanjoselopera.proy_prog_mobile.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Reutilizamos el contenedor de MainActivity o crea uno nuevo

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content_container, SignUpFragment())
                .commit()
        }
    }
}
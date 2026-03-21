package com.juanjoselopera.proy_prog_mobile.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.ui.landing.LandingFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.login.LoginFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.signup.SignUpFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val viewToOpen = intent.getStringExtra("view")
            when (viewToOpen) {
                "login" -> {
                    // Login sí lleva header y menú según tu solicitud
                    setNavComponentsVisibility(true)
                    replaceMainFragment(LoginFragment(), false)
                }
                "signup" -> {
                    // Signup no lleva header ni menú
                    setNavComponentsVisibility(false)
                    replaceMainFragment(SignUpFragment(), false)
                }
                else -> {
                    setNavComponentsVisibility(true)
                    replaceMainFragment(LandingFragment(), false)
                }
            }
        }
    }

    fun setNavComponentsVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        findViewById<View>(R.id.header_container)?.visibility = visibility
        findViewById<View>(R.id.menu_container)?.visibility = visibility
    }

    fun replaceMainFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            .replace(R.id.main_content_container, fragment)
        
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }
}

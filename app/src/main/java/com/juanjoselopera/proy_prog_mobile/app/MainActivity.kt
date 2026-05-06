package com.juanjoselopera.proy_prog_mobile.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.ui.landing.LandingFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.login.LoginFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.signup.SignUpFragment
import com.juanjoselopera.proy_prog_mobile.app.util.SessionManager
import com.juanjoselopera.proy_prog_mobile.app.util.applySlideInTransition
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySlideInTransition()
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val viewToOpen = intent.getStringExtra("view")

            if (!sessionManager.isLoggedIn && viewToOpen !in listOf("login", "signup")) {
                startActivity(
                    Intent(this, PresentationActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                return
            }

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

package com.juanjoselopera.proy_prog_mobile.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.ui.auth.AuthFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.landing.LandingFragment
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
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (sessionManager.isLoggedIn && firebaseUser != null) {
                setNavComponentsVisibility(true)
                replaceMainFragment(LandingFragment(), false)
            } else {
                // Si SharedPreferences decía que estaba logueado pero Firebase no, limpiamos
                if (sessionManager.isLoggedIn) {
                    sessionManager.clearSession()
                }

                val tab = when (intent.getStringExtra("view")) {
                    "signup" -> AuthFragment.TAB_SIGNUP
                    else -> AuthFragment.TAB_LOGIN
                }
                setNavComponentsVisibility(false)
                replaceMainFragment(AuthFragment.newInstance(tab), false)
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

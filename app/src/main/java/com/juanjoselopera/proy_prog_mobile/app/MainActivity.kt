package com.juanjoselopera.proy_prog_mobile.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.core.connectivity.ConnectivityObserver
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.AIFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.ApuntesFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.auth.AuthFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.header.HeaderFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.landing.LandingFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.materias.MateriasFragment
import com.juanjoselopera.proy_prog_mobile.app.util.SessionManager
import com.juanjoselopera.proy_prog_mobile.app.util.applySlideInTransition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySlideInTransition()
        setContentView(R.layout.activity_main)

        // Mantiene el tab del bottom nav en sincronía al usar el botón atrás
        // (el pop del back stack no pasa por replaceMainFragment).
        supportFragmentManager.addOnBackStackChangedListener {
            supportFragmentManager.findFragmentById(R.id.main_content_container)
                ?.let { syncNavSelection(it) }
        }

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
        observeConnectivity()
    }

    fun setNavComponentsVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        findViewById<View>(R.id.header_container)?.visibility = visibility
        findViewById<View>(R.id.menu_container)?.visibility = visibility

        // Al entrar al área logueada, refresca el avatar del header: su onResume
        // ya corrió sin sesión, así que hay que pedirle que cargue la foto.
        if (visible) {
            (supportFragmentManager.findFragmentById(R.id.header_container) as? HeaderFragment)
                ?.refreshAvatar()
        }
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

        syncNavSelection(fragment)
    }

    /**
     * Mantiene el ítem activo del bottom nav en sincronía con el fragment mostrado,
     * sin importar desde dónde se navegue (cards de Inicio, etc.). Usa isChecked para
     * actualizar la selección visual sin disparar el listener (evita re-navegar).
     */
    private fun syncNavSelection(fragment: Fragment) {
        val itemId = when (fragment) {
            is LandingFragment -> R.id.nav_home
            is MateriasFragment -> R.id.nav_subjects
            is ApuntesFragment -> R.id.nav_notes
            is AIFragment -> R.id.nav_ai
            else -> return // pantallas que no son de nivel superior: deja el tab actual
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav) ?: return
        if (bottomNav.selectedItemId != itemId) {
            bottomNav.menu.findItem(itemId)?.isChecked = true
        }
    }

    private fun observeConnectivity() {
        val banner = findViewById<TextView>(R.id.offline_banner)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                connectivityObserver.observe().collect { online ->
                    banner.visibility = if (online) View.GONE else View.VISIBLE
                }
            }
        }
    }
}

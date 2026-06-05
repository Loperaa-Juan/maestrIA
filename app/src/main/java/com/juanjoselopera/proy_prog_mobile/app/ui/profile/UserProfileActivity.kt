package com.juanjoselopera.proy_prog_mobile.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.util.PreferencesManager
import com.juanjoselopera.proy_prog_mobile.app.util.SessionManager
import com.juanjoselopera.proy_prog_mobile.app.util.applySlideInTransition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {

    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var sessionManager: SessionManager

    private val viewModel: UserProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySlideInTransition()
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userProfileRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Toolbar>(R.id.toolbarProfile).setNavigationOnClickListener { finish() }

        bindUser()
        bindDarkModeSwitch()
        bindStats()

        findViewById<View>(R.id.btnEditAvatar).setOnClickListener {
            Toast.makeText(this, "Pronto podrás cambiar tu foto", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            firebaseAuth.signOut()
            sessionManager.clearSession()
            val intent = Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    private fun bindStats() {
        val tvStatNotes = findViewById<TextView>(R.id.tvStatNotes)
        val tvStatSubjects = findViewById<TextView>(R.id.tvStatSubjects)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noteCount.collect { tvStatNotes.text = it.toString() }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subjectCount.collect { tvStatSubjects.text = it.toString() }
            }
        }
    }

    private fun bindDarkModeSwitch() {
        val switch = findViewById<SwitchMaterial>(R.id.switchDarkMode)
        switch.isChecked = preferencesManager.isDarkMode
        switch.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.isDarkMode = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun bindUser() {
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvVerifiedBadge = findViewById<TextView>(R.id.tvVerifiedBadge)
        val ivAvatar = findViewById<ImageView>(R.id.ivAvatar)
        val tvFieldName = findViewById<TextView>(R.id.tvFieldName)
        val tvFieldEmail = findViewById<TextView>(R.id.tvFieldEmail)
        val tvFieldUid = findViewById<TextView>(R.id.tvFieldUid)
        val tvFieldMemberSince = findViewById<TextView>(R.id.tvFieldMemberSince)
        val tvFieldLastSignIn = findViewById<TextView>(R.id.tvFieldLastSignIn)

        val user = firebaseAuth.currentUser
        if (user == null) {
            tvName.text = "Sin sesión"
            tvEmail.text = "Inicia sesión para ver tu perfil"
            tvVerifiedBadge.text = "Sin sesión"
            return
        }

        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore('@')?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            ?: "Usuario MaestrIA"

        tvName.text = displayName
        tvEmail.text = user.email ?: "Sin email"
        tvFieldName.text = displayName
        tvFieldEmail.text = user.email ?: "Sin email"
        tvFieldUid.text = user.uid

        tvVerifiedBadge.text =
            if (user.isEmailVerified) "Cuenta verificada" else "Cuenta sin verificar"

        val dateFormat = SimpleDateFormat("d 'de' MMMM, yyyy", Locale.forLanguageTag("es-ES"))
        tvFieldMemberSince.text = user.metadata?.creationTimestamp
            ?.let { dateFormat.format(Date(it)) } ?: "—"
        tvFieldLastSignIn.text = user.metadata?.lastSignInTimestamp
            ?.let { SimpleDateFormat("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-ES")).format(Date(it)) }
            ?: "—"

        user.photoUrl?.let { photoUri ->
            Glide.with(this)
                .load(photoUri)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(ivAvatar)
        }
    }
}

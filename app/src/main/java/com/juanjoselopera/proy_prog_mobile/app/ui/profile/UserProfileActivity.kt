package com.juanjoselopera.proy_prog_mobile.app.ui.profile

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.util.PreferencesManager
import com.juanjoselopera.proy_prog_mobile.app.util.ProfileImageStore
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import com.juanjoselopera.proy_prog_mobile.app.util.SessionManager
import com.juanjoselopera.proy_prog_mobile.app.util.applySlideInTransition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
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

    private lateinit var ivAvatar: ImageView
    private lateinit var avatarProgress: ProgressBar
    private lateinit var btnEditAvatar: View
    private lateinit var tvName: TextView
    private lateinit var tvFieldName: TextView

    private var pendingCameraUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { onImagePicked(it) }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) pendingCameraUri?.let { onImagePicked(it) }
        }

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

        ivAvatar = findViewById(R.id.ivAvatar)
        avatarProgress = findViewById(R.id.avatarProgress)
        btnEditAvatar = findViewById(R.id.btnEditAvatar)
        tvName = findViewById(R.id.tvProfileName)
        tvFieldName = findViewById(R.id.tvFieldName)

        bindUser()
        bindDarkModeSwitch()
        bindStats()
        observeProfileState()

        btnEditAvatar.setOnClickListener { showPhotoSourceDialog() }
        findViewById<View>(R.id.btnEditName).setOnClickListener { showEditNameDialog() }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            firebaseAuth.signOut()
            sessionManager.clearSession()
            preferencesManager.profileName = null
            preferencesManager.profilePhotoPath = null
            ProfileImageStore.clear(this)
            val intent = Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    // ── Foto de perfil ─────────────────────────────────────────────────────────

    private fun showPhotoSourceDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Foto de perfil")
            .setItems(arrayOf("Elegir de galería", "Tomar foto")) { _, which ->
                if (which == 0) {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    openCamera()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openCamera() {
        val dir = File(cacheDir, "camera").also { it.mkdirs() }
        val file = File(dir, "profile_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            this,
            "com.juanjoselopera.proy_prog_mobile.fileprovider",
            file
        )
        pendingCameraUri = uri
        takePicture.launch(uri)
    }

    private fun onImagePicked(uri: Uri) {
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null || bytes.isEmpty()) {
                Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
                return
            }
            viewModel.savePhoto(bytes)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAvatar() {
        val local = ProfileImageStore.existing(this)
        when {
            local != null -> {
                ivAvatar.setPadding(0, 0, 0, 0)
                ivAvatar.imageTintList = null
                Glide.with(this)
                    .load(local)
                    .signature(ObjectKey(local.lastModified()))
                    .circleCrop()
                    .into(ivAvatar)
            }
            firebaseAuth.currentUser?.photoUrl != null -> {
                ivAvatar.setPadding(0, 0, 0, 0)
                ivAvatar.imageTintList = null
                Glide.with(this)
                    .load(firebaseAuth.currentUser?.photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivAvatar)
            }
            else -> {
                val pad = (22 * resources.displayMetrics.density).toInt()
                ivAvatar.setImageResource(R.drawable.ic_person)
                ivAvatar.imageTintList =
                    ColorStateList.valueOf(getColor(R.color.colorAccentPrimary))
                ivAvatar.setPadding(pad, pad, pad, pad)
            }
        }
    }

    // ── Editar nombre ──────────────────────────────────────────────────────────

    private fun showEditNameDialog() {
        val current = firebaseAuth.currentUser?.displayName ?: tvName.text?.toString().orEmpty()
        val input = EditText(this).apply {
            setText(current)
            setSelection(text.length)
            hint = "Tu nombre"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            maxLines = 1
        }
        val pad = (20 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this).apply {
            setPadding(pad, (8 * resources.displayMetrics.density).toInt(), pad, 0)
            addView(input)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar nombre")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.saveName(name)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Estados ────────────────────────────────────────────────────────────────

    private fun observeProfileState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.photoState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            avatarProgress.visibility = View.VISIBLE
                            btnEditAvatar.isEnabled = false
                        }
                        is Resource.Success -> {
                            avatarProgress.visibility = View.GONE
                            btnEditAvatar.isEnabled = true
                            loadAvatar()
                            Toast.makeText(this@UserProfileActivity, "Foto actualizada", Toast.LENGTH_SHORT).show()
                            viewModel.clearPhotoState()
                        }
                        is Resource.Error -> {
                            avatarProgress.visibility = View.GONE
                            btnEditAvatar.isEnabled = true
                            Toast.makeText(this@UserProfileActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.clearPhotoState()
                        }
                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nameState.collect { state ->
                    when (state) {
                        is Resource.Success -> {
                            val name = firebaseAuth.currentUser?.displayName
                            if (!name.isNullOrBlank()) {
                                tvName.text = name
                                tvFieldName.text = name
                            }
                            Toast.makeText(this@UserProfileActivity, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                            viewModel.clearNameState()
                        }
                        is Resource.Error -> {
                            Toast.makeText(this@UserProfileActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.clearNameState()
                        }
                        else -> Unit
                    }
                }
            }
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
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvVerifiedBadge = findViewById<TextView>(R.id.tvVerifiedBadge)
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

        loadAvatar()
    }
}

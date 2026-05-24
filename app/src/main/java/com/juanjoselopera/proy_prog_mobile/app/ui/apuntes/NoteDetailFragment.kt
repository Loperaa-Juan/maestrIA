package com.juanjoselopera.proy_prog_mobile.app.ui.apuntes

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private val viewModel: ApuntesViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("es"))

    private var note: Note? = null
    private var isEditMode = false
    private var pendingPhotoUri: Uri? = null

    private lateinit var markwon: Markwon
    private lateinit var toolbar: Toolbar
    private lateinit var tvTitle: TextView
    private lateinit var tilTitle: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var tvContent: TextView
    private lateinit var tilContent: TextInputLayout
    private lateinit var etContent: TextInputEditText
    private lateinit var noteImage: ImageView
    private lateinit var btnChangePhoto: MaterialButton
    private lateinit var tvDate: TextView
    private lateinit var tvSubject: TextView
    private lateinit var fabEditSave: FloatingActionButton

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingPhotoUri ?: return@registerForActivityResult
        if (success) {
            note = note?.copy(imageUri = uri.toString())
            refreshImage()
        } else {
            // Clean up the temp file so it doesn't accumulate in cache
            try { File(uri.path ?: return@registerForActivityResult).delete() } catch (_: Exception) {}
        }
    }

    companion object {
        private const val ARG_NOTE = "note"
        private const val ARG_START_EDIT = "start_edit"
        private const val STATE_PENDING_URI = "pending_photo_uri"
        private const val STATE_IS_EDIT_MODE = "is_edit_mode"

        fun newInstance(note: Note, startInEditMode: Boolean = false): NoteDetailFragment =
            NoteDetailFragment().apply {
                arguments = Bundle().also {
                    it.putParcelable(ARG_NOTE, note)
                    it.putBoolean(ARG_START_EDIT, startInEditMode)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_note_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        note = arguments?.getParcelable(ARG_NOTE)
        val startEdit = arguments?.getBoolean(ARG_START_EDIT) ?: false

        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val prism4j = Prism4j(GrammarLocatorDef())
        val prism4jTheme = if (isDark) Prism4jThemeDarkula.create() else Prism4jThemeDefault.create()
        val textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics
        )
        markwon = Markwon.builder(requireContext())
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, prism4jTheme))
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create(textSizePx) { it.inlinesEnabled(true) })
            .build()

        toolbar = view.findViewById(R.id.toolbar)
        tvTitle = view.findViewById(R.id.tvNoteTitle)
        tilTitle = view.findViewById(R.id.tilNoteTitle)
        etTitle = view.findViewById(R.id.etNoteTitle)
        tvContent = view.findViewById(R.id.tvNoteContent)
        tilContent = view.findViewById(R.id.tilNoteContent)
        etContent = view.findViewById(R.id.etNoteContent)
        noteImage = view.findViewById(R.id.noteImage)
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto)
        tvDate = view.findViewById(R.id.tvNoteDate)
        tvSubject = view.findViewById(R.id.tvNoteSubject)
        fabEditSave = view.findViewById(R.id.fabEditSave)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        toolbar.inflateMenu(R.menu.menu_note_detail)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete) { confirmDelete(); true } else false
        }

        renderNote()

        if (savedInstanceState != null) {
            savedInstanceState.getString(STATE_PENDING_URI)?.let { pendingPhotoUri = Uri.parse(it) }
            if (savedInstanceState.getBoolean(STATE_IS_EDIT_MODE)) switchToEditMode()
        } else if (startEdit) {
            switchToEditMode()
        }

        fabEditSave.setOnClickListener {
            if (isEditMode) saveNote() else switchToEditMode()
        }

        btnChangePhoto.setOnClickListener { openCamera() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pendingPhotoUri?.let { outState.putString(STATE_PENDING_URI, it.toString()) }
        outState.putBoolean(STATE_IS_EDIT_MODE, isEditMode)
    }

    private fun renderNote() {
        val n = note ?: return
        toolbar.title = ""
        tvTitle.text = n.title
        markwon.setMarkdown(tvContent, n.content.ifBlank { "*Sin contenido aún*" })
        tvDate.text = dateFormat.format(Date(n.createdAt))
        tvSubject.text = n.subjectName
        refreshImage()
    }

    private fun refreshImage() {
        val uri = note?.imageUri
        if (!uri.isNullOrBlank()) {
            noteImage.visibility = View.VISIBLE
            Glide.with(this).load(Uri.parse(uri)).centerCrop().into(noteImage)
        } else {
            noteImage.visibility = View.GONE
        }
    }

    private fun switchToEditMode() {
        isEditMode = true
        tvTitle.visibility = View.GONE
        tilTitle.visibility = View.VISIBLE
        etTitle.setText(note?.title)
        tvContent.visibility = View.GONE
        tilContent.visibility = View.VISIBLE
        etContent.setText(note?.content)
        btnChangePhoto.visibility = View.VISIBLE
        fabEditSave.setImageResource(R.drawable.ic_check)
        etTitle.requestFocus()
    }

    private fun switchToReadMode() {
        isEditMode = false
        tvTitle.visibility = View.VISIBLE
        tilTitle.visibility = View.GONE
        tvContent.visibility = View.VISIBLE
        tilContent.visibility = View.GONE
        btnChangePhoto.visibility = View.GONE
        fabEditSave.setImageResource(android.R.drawable.ic_menu_edit)
        renderNote()
    }

    private fun saveNote() {
        val title = etTitle.text?.toString()?.trim().orEmpty()
        val content = etContent.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "El apunte necesita un título", Toast.LENGTH_SHORT).show()
            etTitle.requestFocus()
            return
        }
        note = note?.copy(title = title, content = content)
        val n = note ?: return
        viewModel.saveNote(n)
        if (n.id.isEmpty()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        } else {
            switchToReadMode()
        }
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar apunte")
            .setMessage("¿Seguro que quieres eliminar \"${note?.title}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                note?.let { viewModel.deleteNote(it.id) }
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openCamera() {
        val context = requireContext()
        val cameraDir = File(context.cacheDir, "camera").also { it.mkdirs() }
        val photoFile = File(cameraDir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "com.juanjoselopera.proy_prog_mobile.fileprovider",
            photoFile
        )
        pendingPhotoUri = uri
        cameraLauncher.launch(uri)
    }
}

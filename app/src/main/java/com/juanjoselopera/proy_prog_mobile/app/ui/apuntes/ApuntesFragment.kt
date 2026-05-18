package com.juanjoselopera.proy_prog_mobile.app.ui.apuntes

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ApuntesFragment : Fragment() {

    private val viewModel: ApuntesViewModel by viewModels()
    private var subjectSnapshot: List<Subject> = emptyList()
    private val dateFormat = SimpleDateFormat("d MMM", Locale("es"))

    companion object {
        private const val ARG_SUBJECT_ID = "subject_id"
        private const val ARG_SUBJECT_NAME = "subject_name"

        fun newInstance(subjectId: String, subjectName: String): ApuntesFragment {
            return ApuntesFragment().apply {
                arguments = android.os.Bundle().also {
                    it.putString(ARG_SUBJECT_ID, subjectId)
                    it.putString(ARG_SUBJECT_NAME, subjectName)
                }
            }
        }

        private val accentColors = listOf(
            Color.parseColor("#1565C0"),
            Color.parseColor("#2E7D32"),
            Color.parseColor("#C2185B"),
            Color.parseColor("#EF6C00"),
            Color.parseColor("#6A1B9A"),
            Color.parseColor("#00838F")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_apuntes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<LinearLayout>(R.id.apuntesList)
        val tvFilter = view.findViewById<TextView>(R.id.tvSubjectFilter)
        val tvTitle = view.findViewById<TextView>(R.id.tvApuntesTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvApuntesSubtitle)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddNote)

        val argSubjectId = arguments?.getString(ARG_SUBJECT_ID)
        val argSubjectName = arguments?.getString(ARG_SUBJECT_NAME)

        if (argSubjectId != null) viewModel.filterBySubject(argSubjectId)
        if (argSubjectName != null) tvTitle.text = "Apuntes de $argSubjectName"

        // Style filter as a pill chip
        tvFilter.background = GradientDrawable().apply {
            setColor(Color.parseColor("#EDE9FE"))
            cornerRadius = dp(24f)
        }
        tvFilter.setPadding(dp(14).toInt(), dp(8).toInt(), dp(14).toInt(), dp(8).toInt())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subjects.collect { subjects -> subjectSnapshot = subjects }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedSubjectId.collect { selectedId ->
                    val name = if (selectedId == null) "Todas las materias"
                    else subjectSnapshot.find { it.id == selectedId }?.name ?: "Todas las materias"
                    tvFilter.text = "$name ▾"
                }
            }
        }

        var lastCount = -1
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notes.collect { notes ->
                    tvSubtitle.text = when (notes.size) {
                        0 -> "Sin apuntes aún"
                        1 -> "1 apunte"
                        else -> "${notes.size} apuntes"
                    }
                    list.removeAllViews()
                    notes.forEach { note -> addNoteCard(list, note) }
                    if (notes.size > lastCount && lastCount >= 0) {
                        animateNewCard(list.getChildAt(0))
                    }
                    lastCount = notes.size
                }
            }
        }

        tvFilter.setOnClickListener { showSubjectFilterDialog(tvFilter) }
        fab.setOnClickListener { showCreateNoteDialog() }
    }

    private fun addNoteCard(container: LinearLayout, note: Note) {
        val context = container.context

        val subjectAccent = subjectSnapshot.find { it.id == note.subjectId }
            ?.let { accentColors.getOrElse(it.colorIndex) { accentColors[0] } }
            ?: accentColors[4]

        val cardBg = context.getColor(R.color.colorCardBackground)
        val textPrimary = context.getColor(R.color.colorTextPrimary)
        val textSecondary = context.getColor(R.color.colorTextSecondary)

        val card = CardView(context).apply {
            radius = dp(16f)
            cardElevation = dp(3f)
            setCardBackgroundColor(cardBg)
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = dp(12).toInt()
            layoutParams = lp
        }

        // Horizontal root: color bar | content
        val outer = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        val colorBar = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(4).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(subjectAccent)
        }

        val inner = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14).toInt(), dp(14).toInt(), dp(14).toInt(), dp(12).toInt())
        }

        // Row 1: title + soft delete icon
        val row1 = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleView = TextView(context).apply {
            text = note.title
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val deleteBtn = ImageView(context).apply {
            val lp = LinearLayout.LayoutParams(dp(20).toInt(), dp(20).toInt())
            lp.leftMargin = dp(8).toInt()
            layoutParams = lp
            setImageResource(android.R.drawable.ic_menu_delete)
            imageTintList = ColorStateList.valueOf(Color.parseColor("#D1D5DB"))
        }
        deleteBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar apunte")
                .setMessage("¿Seguro que quieres eliminar \"${note.title}\"?")
                .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteNote(note.id) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        row1.addView(titleView)
        row1.addView(deleteBtn)

        // Content preview (shown right below title)
        val contentView = if (note.content.isNotBlank()) {
            TextView(context).apply {
                text = note.content
                textSize = 13f
                setTextColor(textSecondary)
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = dp(4).toInt()
                layoutParams = lp
            }
        } else null

        // Row 2: subject chip (subject color) + spacer + date
        val row2 = LinearLayout(context).apply {
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(10).toInt()
            layoutParams = lp
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        if (note.subjectName.isNotBlank()) {
            val chip = TextView(context).apply {
                text = note.subjectName
                textSize = 11f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(subjectAccent)
                background = GradientDrawable().apply {
                    setColor(Color.argb(22,
                        Color.red(subjectAccent),
                        Color.green(subjectAccent),
                        Color.blue(subjectAccent)))
                    cornerRadius = dp(20f)
                }
                setPadding(dp(8).toInt(), dp(3).toInt(), dp(8).toInt(), dp(3).toInt())
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lp.rightMargin = dp(6).toInt()
                layoutParams = lp
            }
            row2.addView(chip)
        }
        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }
        val dateView = TextView(context).apply {
            text = dateFormat.format(Date(note.createdAt))
            textSize = 11f
            setTextColor(textSecondary)
        }
        row2.addView(spacer)
        row2.addView(dateView)

        inner.addView(row1)
        if (contentView != null) inner.addView(contentView)
        inner.addView(row2)

        outer.addView(colorBar)
        outer.addView(inner)
        card.addView(outer)
        container.addView(card, 0)
    }

    private fun showSubjectFilterDialog(tvFilter: TextView) {
        val subjects = subjectSnapshot
        val items = arrayOf("Todas las materias") + subjects.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar por materia")
            .setItems(items) { _, which ->
                if (which == 0) {
                    viewModel.filterBySubject(null)
                    tvFilter.text = "Todas las materias ▾"
                } else {
                    val selected = subjects[which - 1]
                    viewModel.filterBySubject(selected.id)
                    tvFilter.text = "${selected.name} ▾"
                }
            }
            .show()
    }

    private fun showCreateNoteDialog() {
        val subjects = subjectSnapshot
        if (subjects.isEmpty()) {
            Toast.makeText(requireContext(), "Primero crea una materia", Toast.LENGTH_SHORT).show()
            return
        }

        val context = requireContext()
        val dialog = BottomSheetDialog(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_note, null)
        dialog.setContentView(dialogView)

        var selectedSubjectIdx = 0
        val titleInput = dialogView.findViewById<EditText>(R.id.etNoteTitle)
        val contentInput = dialogView.findViewById<EditText>(R.id.etNoteContent)
        val confirmBtn = dialogView.findViewById<MaterialButton>(R.id.btnConfirmNote)
        val subjectPicker = dialogView.findViewById<LinearLayout>(R.id.subjectPicker)

        fun buildSubjectChip(label: String, accent: Int, selected: Boolean): TextView {
            return TextView(context).apply {
                text = label
                textSize = 13f
                setTypeface(typeface, if (selected) Typeface.BOLD else Typeface.NORMAL)
                setTextColor(if (selected) Color.WHITE else accent)
                background = subjectChipBackground(accent, selected)
                setPadding(dp(14).toInt(), dp(8).toInt(), dp(14).toInt(), dp(8).toInt())
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lp.rightMargin = dp(10).toInt()
                layoutParams = lp
            }
        }

        fun refreshChips() {
            subjectPicker.removeAllViews()
            subjects.forEachIndexed { index, subject ->
                val accent = accentColors.getOrElse(subject.colorIndex) { accentColors[0] }
                val chip = buildSubjectChip(subject.name, accent, index == selectedSubjectIdx)
                chip.setOnClickListener {
                    selectedSubjectIdx = index
                    refreshChips()
                }
                subjectPicker.addView(chip)
            }
        }
        refreshChips()

        confirmBtn.setOnClickListener {
            val title = titleInput.text?.toString()?.trim().orEmpty()
            val content = contentInput.text?.toString()?.trim().orEmpty()
            if (title.isEmpty()) {
                Toast.makeText(context, "El apunte necesita un título", Toast.LENGTH_SHORT).show()
                titleInput.requestFocus()
                return@setOnClickListener
            }
            val subject = subjects[selectedSubjectIdx]
            viewModel.addNote(title, content, subject.id, subject.name)
            dialog.dismiss()
        }

        titleInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                confirmBtn.isEnabled = !s.isNullOrBlank()
            }
        })
        confirmBtn.isEnabled = false

        dialog.show()
    }

    private fun subjectChipBackground(@ColorInt accent: Int, selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            setColor(if (selected) accent else Color.parseColor("#F3F4F6"))
            cornerRadius = dp(20f)
            if (!selected) setStroke(dp(1).toInt(), accent)
        }
    }

    private fun animateNewCard(view: View?) {
        view ?: return
        view.alpha = 0f
        view.translationY = -dp(24f)
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setInterpolator(OvershootInterpolator(1.2f))
            .setDuration(300)
            .start()
    }

    private fun dp(value: Int): Float = dp(value.toFloat())
    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}

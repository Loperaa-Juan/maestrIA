package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.juanjoselopera.proy_prog_mobile.R
import kotlinx.coroutines.flow.combine
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.data.local.AIModelPrefs
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.result.ConceptsResultFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.result.QuestionsResultFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.result.SummaryResultFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.ApuntesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotePickerFragment : Fragment() {

    private val viewModel: ApuntesViewModel by viewModels()

    companion object {
        private const val ARG_TOOL = "tool_type"

        private val accentColors = listOf(
            Color.parseColor("#1565C0"),
            Color.parseColor("#2E7D32"),
            Color.parseColor("#C2185B"),
            Color.parseColor("#EF6C00"),
            Color.parseColor("#6A1B9A"),
            Color.parseColor("#00838F")
        )

        fun newInstance(tool: ToolType) = NotePickerFragment().apply {
            arguments = Bundle().apply { putString(ARG_TOOL, tool.name) }
        }
    }

    private val toolType: ToolType by lazy {
        ToolType.valueOf(arguments?.getString(ARG_TOOL) ?: ToolType.QUESTIONS.name)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_note_picker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val tvSubtitle = view.findViewById<TextView>(R.id.tvPickerSubtitle)
        tvSubtitle.text = when (toolType) {
            ToolType.QUESTIONS -> "Elige la nota para generar preguntas"
            ToolType.CONCEPTS -> "Elige la nota para extraer conceptos"
            ToolType.SUMMARY -> "Elige la nota para resumir"
        }

        val notesList = view.findViewById<LinearLayout>(R.id.notesList)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.subjects, viewModel.notes) { subjects, notes ->
                    Pair(subjects, notes)
                }.collect { (subjects, notes) ->
                    notesList.removeAllViews()
                    if (notes.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        tvEmpty.visibility = View.GONE
                        renderGroupedNotes(notesList, subjects, notes)
                    }
                }
            }
        }

        viewModel.filterBySubject(null)
    }

    private fun renderGroupedNotes(container: LinearLayout, subjects: List<Subject>, notes: List<Note>) {
        val grouped = notes.groupBy { it.subjectId }

        subjects.forEach { subject ->
            val subjectNotes = grouped[subject.id] ?: return@forEach
            if (subjectNotes.isEmpty()) return@forEach

            val accent = accentColors.getOrElse(subject.colorIndex) { accentColors[0] }

            val sectionHeader = TextView(requireContext()).apply {
                text = subject.name
                textSize = 13f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(accent)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = dp(16)
                lp.bottomMargin = dp(8)
                layoutParams = lp
            }
            container.addView(sectionHeader)

            subjectNotes.forEach { note ->
                container.addView(buildNoteCard(note, accent))
            }
        }
    }

    private fun buildNoteCard(note: Note, accent: Int): CardView {
        val context = requireContext()
        val card = CardView(context).apply {
            radius = dp(14f)
            cardElevation = dp(2f)
            setCardBackgroundColor(context.getColor(R.color.colorCardBackground))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = dp(10)
            layoutParams = lp
        }

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val colorBar = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(4), LinearLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(accent)
        }

        val inner = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }

        val tvTitle = TextView(context).apply {
            text = note.title
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColor(R.color.colorTextPrimary))
        }
        inner.addView(tvTitle)

        if (note.content.isNotBlank()) {
            val tvPreview = TextView(context).apply {
                text = note.content
                textSize = 13f
                setTextColor(context.getColor(R.color.colorTextSecondary))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = dp(4)
                layoutParams = lp
            }
            inner.addView(tvPreview)
        }

        row.addView(colorBar)
        row.addView(inner)
        card.addView(row)

        card.setOnClickListener { onNoteSelected(note) }
        return card
    }

    private fun onNoteSelected(note: Note) {
        val model = AIModelPrefs.getSelectedId(requireContext())
        if (model.isNullOrBlank()) {
            Snackbar.make(requireView(), "Selecciona un modelo de IA primero", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (note.content.isBlank()) {
            Snackbar.make(requireView(), "Esta nota no tiene contenido para analizar", Snackbar.LENGTH_SHORT).show()
            return
        }

        val resultFragment = when (toolType) {
            ToolType.QUESTIONS -> QuestionsResultFragment.newInstance(note.content, model)
            ToolType.CONCEPTS -> ConceptsResultFragment.newInstance(note.content, model)
            ToolType.SUMMARY -> SummaryResultFragment.newInstance(note.content, model)
        }
        (requireActivity() as MainActivity).replaceMainFragment(resultFragment)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}

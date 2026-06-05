package com.juanjoselopera.proy_prog_mobile.app.ui.apuntes

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.LoadingDotsAnimator
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.MarkwonProvider
import com.juanjoselopera.proy_prog_mobile.databinding.FragmentExtractTextBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExtractTextFragment : Fragment() {

    private var _binding: FragmentExtractTextBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExtractTextViewModel by viewModels()
    private var subjectsSnapshot: List<Subject> = emptyList()

    companion object {
        private const val ARG_IMAGE = "image_bytes"
        private const val ARG_MIME = "mime_type"
        private const val ARG_MODEL = "model"
        private const val ARG_SUBJECT_ID = "subject_id"
        private const val ARG_SUBJECT_NAME = "subject_name"

        fun newInstance(
            imageBytes: ByteArray,
            mimeType: String,
            model: String,
            subjectId: String,
            subjectName: String
        ) = ExtractTextFragment().apply {
            arguments = Bundle().apply {
                putByteArray(ARG_IMAGE, imageBytes)
                putString(ARG_MIME, mimeType)
                putString(ARG_MODEL, model)
                putString(ARG_SUBJECT_ID, subjectId)
                putString(ARG_SUBJECT_NAME, subjectName)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtractTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageBytes = arguments?.getByteArray(ARG_IMAGE) ?: byteArrayOf()
        val mimeType = arguments?.getString(ARG_MIME) ?: "image/jpeg"
        val model = arguments?.getString(ARG_MODEL) ?: ""
        val defaultSubjectId = arguments?.getString(ARG_SUBJECT_ID) ?: ""
        val defaultSubjectName = arguments?.getString(ARG_SUBJECT_NAME) ?: ""

        val markwon = MarkwonProvider.create(requireContext())
        val dotsAnimator = LoadingDotsAnimator(binding.tvLoadingLabel, "Extrayendo texto")

        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.extract(imageBytes, mimeType, model)
        }

        binding.btnSaveNote.setOnClickListener {
            showSaveDialog(defaultSubjectId, defaultSubjectName)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subjects.collect { subjects -> subjectsSnapshot = subjects }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loadingState.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    if (state.isLoading) dotsAnimator.start() else dotsAnimator.stop()
                    binding.errorState.visibility = if (state.error != null) View.VISIBLE else View.GONE
                    binding.resultState.visibility = if (state.text != null) View.VISIBLE else View.GONE

                    state.error?.let {
                        binding.tvError.text = it
                    }

                    state.text?.let { text ->
                        markwon.setMarkdown(binding.tvExtractedText, text)
                    }

                    if (state.isSaved) {
                        Toast.makeText(requireContext(), "Apunte guardado", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            }
        }

        if (viewModel.uiState.value.text == null && !viewModel.uiState.value.isLoading) {
            viewModel.extract(imageBytes, mimeType, model)
        }
    }

    private fun showSaveDialog(defaultSubjectId: String, defaultSubjectName: String) {
        val subjects = subjectsSnapshot.ifEmpty {
            if (defaultSubjectId.isNotBlank()) {
                listOf(Subject(id = defaultSubjectId, name = defaultSubjectName))
            } else {
                Toast.makeText(requireContext(), "Primero crea una materia", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val context = requireContext()
        val dialog = BottomSheetDialog(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_note, null)
        dialog.setContentView(dialogView)

        val titleInput = dialogView.findViewById<EditText>(R.id.etNoteTitle)
        val contentInput = dialogView.findViewById<EditText>(R.id.etNoteContent)
        val confirmBtn = dialogView.findViewById<MaterialButton>(R.id.btnConfirmNote)
        val subjectPicker = dialogView.findViewById<LinearLayout>(R.id.subjectPicker)

        contentInput.visibility = View.GONE

        var selectedIdx = subjects.indexOfFirst { it.id == defaultSubjectId }.takeIf { it >= 0 } ?: 0

        val accentColors = resources.obtainTypedArray(R.array.subject_accents).let { ta ->
            val colors = (0 until ta.length()).map { ta.getColor(it, Color.GRAY) }
            ta.recycle()
            colors
        }

        fun dp(v: Float) = v * resources.displayMetrics.density

        fun refreshChips() {
            subjectPicker.removeAllViews()
            subjects.forEachIndexed { index, subject ->
                val accent = accentColors.getOrElse(subject.colorIndex) { accentColors[0] }
                val selected = index == selectedIdx
                val chip = TextView(context).apply {
                    text = subject.name
                    textSize = 13f
                    setTypeface(typeface, if (selected) Typeface.BOLD else Typeface.NORMAL)
                    setTextColor(if (selected) Color.WHITE else accent)
                    background = android.graphics.drawable.GradientDrawable().apply {
                        setColor(if (selected) accent else context.getColor(R.color.colorChipNeutralBackground))
                        cornerRadius = dp(20f)
                        if (!selected) setStroke(dp(1f).toInt(), accent)
                    }
                    setPadding(dp(14f).toInt(), dp(8f).toInt(), dp(14f).toInt(), dp(8f).toInt())
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.rightMargin = dp(10f).toInt()
                    layoutParams = lp
                    setOnClickListener { selectedIdx = index; refreshChips() }
                }
                subjectPicker.addView(chip)
            }
        }
        refreshChips()

        confirmBtn.isEnabled = false
        titleInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                confirmBtn.isEnabled = !s.isNullOrBlank()
            }
        })

        confirmBtn.setOnClickListener {
            val title = titleInput.text?.toString()?.trim().orEmpty()
            if (title.isEmpty()) return@setOnClickListener
            val subject = subjects[selectedIdx]
            viewModel.saveNote(title, subject.id, subject.name)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

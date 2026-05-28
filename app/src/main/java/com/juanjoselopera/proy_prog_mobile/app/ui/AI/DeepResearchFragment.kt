package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.data.local.AIModelPrefs
import com.juanjoselopera.proy_prog_mobile.app.domain.model.ResearchSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class DeepResearchFragment : Fragment() {

    private val viewModel: DeepResearchViewModel by viewModels()

    private var lastTopic: String? = null
    private var lastImageBytes: ByteArray? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            lastImageBytes = stream.toByteArray()
            lastTopic = null
            val model = AIModelPrefs.getSelectedId(requireContext())
            if (model.isNullOrBlank()) {
                Snackbar.make(requireView(), "Selecciona un modelo de IA primero", Snackbar.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            viewModel.research(model, null, lastImageBytes, "image/jpeg")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_deep_research, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formScroll = view.findViewById<ScrollView>(R.id.formScroll)
        val loadingState = view.findViewById<View>(R.id.loadingState)
        val resultScroll = view.findViewById<ScrollView>(R.id.resultScroll)
        val errorState = view.findViewById<View>(R.id.errorState)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val tvResearch = view.findViewById<TextView>(R.id.tvResearch)
        val markwon = MarkwonProvider.create(requireContext())
        val sourcesCard = view.findViewById<MaterialCardView>(R.id.sourcesCard)
        val sourcesList = view.findViewById<LinearLayout>(R.id.sourcesList)
        val btnNewSearch = view.findViewById<MaterialButton>(R.id.btnNewSearch)
        val btnRetry = view.findViewById<MaterialButton>(R.id.btnRetry)
        val inputLayout = view.findViewById<TextInputLayout>(R.id.topicInputLayout)
        val etTopic = view.findViewById<TextInputEditText>(R.id.etTopic)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.btnInvestigate).setOnClickListener {
            val tema = etTopic.text?.toString()?.trim()
            if (tema.isNullOrEmpty()) {
                inputLayout.error = "Por favor escribe un tema"
                return@setOnClickListener
            }
            inputLayout.error = null
            val model = AIModelPrefs.getSelectedId(requireContext())
            if (model.isNullOrBlank()) {
                Snackbar.make(requireView(), "Selecciona un modelo de IA primero", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lastTopic = tema
            lastImageBytes = null
            viewModel.research(model, tema, null, null)
        }

        view.findViewById<MaterialCardView>(R.id.cardCamera).setOnClickListener {
            takePictureLauncher.launch(null)
        }

        btnNewSearch.setOnClickListener {
            viewModel.reset()
            etTopic.text?.clear()
        }

        btnRetry.setOnClickListener {
            val model = AIModelPrefs.getSelectedId(requireContext()) ?: return@setOnClickListener
            viewModel.research(model, lastTopic, lastImageBytes, if (lastImageBytes != null) "image/jpeg" else null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    formScroll.visibility = if (!state.isLoading && state.result == null && state.error == null) View.VISIBLE else View.GONE
                    loadingState.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    resultScroll.visibility = if (state.result != null) View.VISIBLE else View.GONE
                    errorState.visibility = if (state.error != null) View.VISIBLE else View.GONE

                    state.error?.let { tvError.text = it }

                    state.result?.let { result ->
                        markwon.setMarkdown(tvResearch, result.research)
                        sourcesList.removeAllViews()
                        if (result.sources.isNotEmpty()) {
                            sourcesCard.visibility = View.VISIBLE
                            result.sources.forEach { source ->
                                sourcesList.addView(buildSourceRow(source))
                            }
                        } else {
                            sourcesCard.visibility = View.GONE
                        }
                    }
                }
            }
        }

        val content = view.findViewById<View>(R.id.deepResearchContent)
        content.alpha = 0f
        content.translationY = 32f
        content.animate().alpha(1f).translationY(0f).setDuration(350).start()
    }

    private fun buildSourceRow(source: ResearchSource): View {
        val context = requireContext()
        val dp = resources.displayMetrics.density

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (12 * dp).toInt()
            layoutParams = lp
        }

        val tvTitle = TextView(context).apply {
            text = source.title
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#7C3AED"))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.uri)))
            }
        }

        val tvUri = TextView(context).apply {
            text = source.uri
            textSize = 11f
            setTextColor(context.getColor(R.color.colorTextSecondary))
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val divider = View(context).apply {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
            lp.topMargin = (12 * dp).toInt()
            layoutParams = lp
            setBackgroundColor(context.getColor(R.color.colorDivider))
        }

        row.addView(tvTitle)
        row.addView(tvUri)
        row.addView(divider)
        return row
    }
}

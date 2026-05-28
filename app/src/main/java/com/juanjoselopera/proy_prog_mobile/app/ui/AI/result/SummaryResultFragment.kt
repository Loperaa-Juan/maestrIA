package com.juanjoselopera.proy_prog_mobile.app.ui.AI.result

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.GrammarLocatorDef
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SummaryResultFragment : Fragment() {

    private val viewModel: SummaryViewModel by viewModels()

    companion object {
        private const val ARG_NOTE = "note"
        private const val ARG_MODEL = "model"

        fun newInstance(noteContent: String, model: String) = SummaryResultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_NOTE, noteContent)
                putString(ARG_MODEL, model)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_summary_result, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteContent = arguments?.getString(ARG_NOTE) ?: ""
        val model = arguments?.getString(ARG_MODEL) ?: ""

        val loadingState = view.findViewById<View>(R.id.loadingState)
        val errorState = view.findViewById<View>(R.id.errorState)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnRetry = view.findViewById<MaterialButton>(R.id.btnRetry)
        val summaryScroll = view.findViewById<ScrollView>(R.id.summaryScroll)
        val tvSummary = view.findViewById<TextView>(R.id.tvSummary)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val prism4j = Prism4j(GrammarLocatorDef())
        val textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics)
        val markwon = Markwon.builder(requireContext())
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, if (isDark) Prism4jThemeDarkula.create() else Prism4jThemeDefault.create()))
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create(textSizePx) { it.inlinesEnabled(true) })
            .build()

        btnRetry.setOnClickListener { viewModel.load(noteContent, model) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    loadingState.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    errorState.visibility = if (state.error != null) View.VISIBLE else View.GONE
                    summaryScroll.visibility = if (state.summary != null) View.VISIBLE else View.GONE

                    if (state.error != null) tvError.text = state.error
                    if (state.summary != null) markwon.setMarkdown(tvSummary, state.summary)
                }
            }
        }

        if (viewModel.uiState.value.summary == null && !viewModel.uiState.value.isLoading) {
            viewModel.load(noteContent, model)
        }
    }
}

package com.juanjoselopera.proy_prog_mobile.app.ui.AI.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.domain.model.ConceptItem
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.MarkwonProvider
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConceptsResultFragment : Fragment() {

    private val viewModel: ConceptsViewModel by viewModels()

    companion object {
        private const val ARG_NOTE = "note"
        private const val ARG_MODEL = "model"

        fun newInstance(noteContent: String, model: String) = ConceptsResultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_NOTE, noteContent)
                putString(ARG_MODEL, model)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_concepts_result, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteContent = arguments?.getString(ARG_NOTE) ?: ""
        val model = arguments?.getString(ARG_MODEL) ?: ""

        val loadingState = view.findViewById<View>(R.id.loadingState)
        val errorState = view.findViewById<View>(R.id.errorState)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnRetry = view.findViewById<MaterialButton>(R.id.btnRetry)
        val rvConcepts = view.findViewById<RecyclerView>(R.id.rvConcepts)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val markwon = MarkwonProvider.create(requireContext())
        val adapter = ConceptsAdapter(markwon)
        rvConcepts.layoutManager = LinearLayoutManager(requireContext())
        rvConcepts.adapter = adapter

        btnRetry.setOnClickListener { viewModel.load(noteContent, model) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    loadingState.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    errorState.visibility = if (state.error != null) View.VISIBLE else View.GONE
                    rvConcepts.visibility = if (state.concepts.isNotEmpty()) View.VISIBLE else View.GONE

                    if (state.error != null) tvError.text = state.error
                    if (state.concepts.isNotEmpty()) adapter.submitList(state.concepts)
                }
            }
        }

        if (viewModel.uiState.value.concepts.isEmpty() && !viewModel.uiState.value.isLoading) {
            viewModel.load(noteContent, model)
        }
    }
}

class ConceptsAdapter(private val markwon: Markwon) : RecyclerView.Adapter<ConceptsAdapter.VH>() {

    private var items: List<ConceptItem> = emptyList()

    fun submitList(list: List<ConceptItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_concept, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        markwon.setMarkdown(holder.tvTerm, item.term)
        markwon.setMarkdown(holder.tvDefinition, item.definition)
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTerm: TextView = view.findViewById(R.id.tvTerm)
        val tvDefinition: TextView = view.findViewById(R.id.tvDefinition)
    }
}

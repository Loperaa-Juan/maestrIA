package com.juanjoselopera.proy_prog_mobile.app.ui.AI.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.juanjoselopera.proy_prog_mobile.app.domain.model.QAItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionsResultFragment : Fragment() {

    private val viewModel: QuestionsViewModel by viewModels()

    companion object {
        private const val ARG_NOTE = "note"
        private const val ARG_MODEL = "model"

        fun newInstance(noteContent: String, model: String) = QuestionsResultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_NOTE, noteContent)
                putString(ARG_MODEL, model)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_questions_result, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteContent = arguments?.getString(ARG_NOTE) ?: ""
        val model = arguments?.getString(ARG_MODEL) ?: ""

        val loadingState = view.findViewById<View>(R.id.loadingState)
        val errorState = view.findViewById<View>(R.id.errorState)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnRetry = view.findViewById<MaterialButton>(R.id.btnRetry)
        val rvQuestions = view.findViewById<RecyclerView>(R.id.rvQuestions)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val adapter = QuestionsAdapter()
        rvQuestions.layoutManager = LinearLayoutManager(requireContext())
        rvQuestions.adapter = adapter

        btnRetry.setOnClickListener { viewModel.load(noteContent, model) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    loadingState.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    errorState.visibility = if (state.error != null) View.VISIBLE else View.GONE
                    rvQuestions.visibility = if (state.questions.isNotEmpty()) View.VISIBLE else View.GONE

                    if (state.error != null) tvError.text = state.error
                    if (state.questions.isNotEmpty()) adapter.submitList(state.questions)
                }
            }
        }

        if (viewModel.uiState.value.questions.isEmpty() && !viewModel.uiState.value.isLoading) {
            viewModel.load(noteContent, model)
        }
    }
}

class QuestionsAdapter : RecyclerView.Adapter<QuestionsAdapter.VH>() {

    private var items: List<QAItem> = emptyList()
    private val expanded = mutableSetOf<Int>()

    fun submitList(list: List<QAItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvQuestion.text = item.question
        holder.tvAnswer.text = item.answer

        val isExpanded = expanded.contains(position)
        holder.tvAnswer.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.ivExpand.rotation = if (isExpanded) 180f else 0f

        holder.itemView.setOnClickListener {
            if (expanded.contains(position)) expanded.remove(position) else expanded.add(position)
            notifyItemChanged(position)
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val tvAnswer: TextView = view.findViewById(R.id.tvAnswer)
        val ivExpand: ImageView = view.findViewById(R.id.ivExpandIcon)
    }
}

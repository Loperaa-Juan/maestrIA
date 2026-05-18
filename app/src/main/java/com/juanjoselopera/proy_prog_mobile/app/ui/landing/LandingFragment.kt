package com.juanjoselopera.proy_prog_mobile.app.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.AIFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.ApuntesFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.materias.MateriasFragment
import com.juanjoselopera.proy_prog_mobile.app.util.animateChildrenSlideInFromRight
import com.juanjoselopera.proy_prog_mobile.app.util.findFirstViewGroupById
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LandingFragment : Fragment() {

    private val viewModel: LandingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_landing, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvWelcomeUser = view.findViewById<TextView>(R.id.tvWelcomeUser)
        val tvSubjectCount = view.findViewById<TextView>(R.id.tvSubjectCount)
        val tvNotesCount = view.findViewById<TextView>(R.id.tvNotesCount)
        val subjectsCard = view.findViewById<CardView>(R.id.SubjectsCard)
        val notesCard = view.findViewById<CardView>(R.id.notesCard)
        val keyIdeasCard = view.findViewById<CardView>(R.id.KeyIdeasCard)
        val cardApuntes = view.findViewById<CardView>(R.id.CardApuntes)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userName = currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Usuario"
        tvWelcomeUser.text = "Hola, $userName 👋"

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subjectCount.collect { count ->
                    tvSubjectCount.text = count.toString()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noteCount.collect { count ->
                    tvNotesCount.text = count.toString()
                }
            }
        }

        subjectsCard.setOnClickListener {
            (activity as? MainActivity)?.replaceMainFragment(MateriasFragment())
        }

        notesCard.setOnClickListener {
            (activity as? MainActivity)?.replaceMainFragment(ApuntesFragment())
        }

        keyIdeasCard.setOnClickListener {
            (activity as? MainActivity)?.replaceMainFragment(AIFragment())
        }

        cardApuntes.setOnClickListener {
            (activity as? MainActivity)?.replaceMainFragment(ApuntesFragment())
        }

        view.findFirstViewGroupById(R.id.landingContent)?.animateChildrenSlideInFromRight()
    }
}
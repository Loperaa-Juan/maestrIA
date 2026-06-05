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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.ui.AI.AIFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.ApuntesFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.NoteDetailFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.materias.MateriasFragment
import com.juanjoselopera.proy_prog_mobile.app.util.animateChildrenSlideInFromRight
import com.juanjoselopera.proy_prog_mobile.app.util.findFirstViewGroupById
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.juanjoselopera.proy_prog_mobile.app.ui.auth.AuthFragment
import com.juanjoselopera.proy_prog_mobile.app.util.SessionManager
import javax.inject.Inject

@AndroidEntryPoint
class LandingFragment : Fragment() {

    @Inject
    lateinit var sessionManager: SessionManager

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
        val tvVerTodos = view.findViewById<TextView>(R.id.tvVerTodosApuntes)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyRecentNotes)
        val recycler = view.findViewById<RecyclerView>(R.id.recentNotesRecycler)

        val currentUser = FirebaseAuth.getInstance().currentUser
        
        if (currentUser == null) {
            sessionManager.clearSession()
            (activity as? MainActivity)?.let {
                it.setNavComponentsVisibility(false)
                it.replaceMainFragment(AuthFragment.newInstance(AuthFragment.TAB_LOGIN), false)
            }
            return
        }

        val userName = currentUser.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Usuario"
        tvWelcomeUser.text = "Hola, $userName 👋"

        // Configura el RecyclerView con los últimos 5 apuntes
        val recentNoteAdapter = RecentNoteAdapter { note ->
            (activity as? MainActivity)?.replaceMainFragment(
                NoteDetailFragment.newInstance(note)
            )
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = recentNoteAdapter

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

        // Actualiza los colores de los chips cuando cambian las materias
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subjects.collect { subjects ->
                    recentNoteAdapter.updateSubjects(subjects)
                }
            }
        }

        // Muestra los últimos 5 apuntes o el estado vacío si no hay ninguno
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentNotes.collect { notes ->
                    recentNoteAdapter.submitList(notes)
                    tvEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
                    recycler.visibility = if (notes.isEmpty()) View.GONE else View.VISIBLE
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

        tvVerTodos.setOnClickListener {
            (activity as? MainActivity)?.replaceMainFragment(ApuntesFragment())
        }

        view.findFirstViewGroupById(R.id.landingContent)?.animateChildrenSlideInFromRight()
    }

    override fun onResume() {
        super.onResume()
        // Refresca el saludo por si el usuario cambió su nombre en el perfil.
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Usuario"
        view?.findViewById<TextView>(R.id.tvWelcomeUser)?.text = "Hola, $userName 👋"
    }
}
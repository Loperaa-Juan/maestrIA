package com.juanjoselopera.proy_prog_mobile.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.juanjoselopera.proy_prog_mobile.R

class LandingFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val tvWelcomeUser = view.findViewById<TextView>(R.id.tvWelcomeUser)
        val subjectsCard = view.findViewById<CardView>(R.id.SubjectsCard)
        val notesCard = view.findViewById<CardView>(R.id.notesCard)
        val keyIdeasCard = view.findViewById<CardView>(R.id.KeyIdeasCard)
        val cardApuntes = view.findViewById<CardView>(R.id.CardApuntes)


        val usuario = activity?.intent?.getStringExtra("usuario") ?: "Usuario"
        tvWelcomeUser.text = "Hola, $usuario 👋"

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

    }
}
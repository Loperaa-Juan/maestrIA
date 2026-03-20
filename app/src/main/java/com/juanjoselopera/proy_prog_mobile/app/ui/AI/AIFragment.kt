package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juanjoselopera.proy_prog_mobile.R

class AIFragment : Fragment() {

    // 1. Inflamos el layout de la IA
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asegúrate de que el nombre del layout coincida con el tuyo
        return inflater.inflate(R.layout.fragment_ai, container, false)
    }

    // 2. Aquí va la lógica de los "Apuntes Inteligentes" o el Chat de IA
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ejemplo: val inputIA = view.findViewById<EditText>(R.id.etPreguntaIA)
    }
}
package com.juanjoselopera.proy_prog_mobile.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juanjoselopera.proy_prog_mobile.R

class ApuntesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Conectamos con el layout fragment_apuntes.xml
        return inflater.inflate(R.layout.fragment_apuntes, container, false)
    }

    // 2. Aquí va la lógica de tus apuntes (listas, clics, etc.)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ejemplo de uso:
        // val listaApuntes = view.findViewById<RecyclerView>(R.id.rvApuntes)
    }
}
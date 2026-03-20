package com.juanjoselopera.proy_prog_mobile.app.ui.materias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juanjoselopera.proy_prog_mobile.R

class MateriasFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout de materias
        return inflater.inflate(R.layout.fragment_materias, container, false)
    }

    // 3. Si tienes botones o listas en Materias, pon la lógica aquí
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ejemplo: val btnAgregar = view.findViewById<Button>(R.id.btn_agregar)

        /* Nota: El código de "WindowInsets" (EdgeToEdge) ya lo maneja la MainActivity
           para toda la pantalla, no es necesario repetirlo en cada Fragment.
        */
    }
}
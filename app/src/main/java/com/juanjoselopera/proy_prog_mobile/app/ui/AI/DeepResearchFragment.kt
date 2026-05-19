package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.juanjoselopera.proy_prog_mobile.R

class DeepResearchFragment : Fragment() {

    // Lanzador del contrato para capturar una imagen con la cámara del dispositivo
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        // Procesar la imagen recibida e iniciar la investigación
        if (bitmap != null) {
            Toast.makeText(
                requireContext(),
                "Imagen capturada. Procesando investigación profunda...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_deep_research, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarBotonRegresar(view)
        configurarOpcionTexto(view)
        configurarOpcionCamara(view)
        animarEntrada(view)
    }

    // Regresa a la pantalla anterior al presionar la flecha de retroceso
    private fun configurarBotonRegresar(view: View) {
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    // Valida el campo de texto y dispara la investigación por tema escrito
    private fun configurarOpcionTexto(view: View) {
        val inputLayout = view.findViewById<TextInputLayout>(R.id.topicInputLayout)
        val etTopic = view.findViewById<TextInputEditText>(R.id.etTopic)

        view.findViewById<MaterialButton>(R.id.btnInvestigate).setOnClickListener {
            val tema = etTopic.text?.toString()?.trim()
            if (tema.isNullOrEmpty()) {
                inputLayout.error = "Por favor escribe un tema"
            } else {
                inputLayout.error = null
                Toast.makeText(requireContext(), "Investigando: $tema", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Abre la cámara al tocar la tarjeta de foto de apuntes
    private fun configurarOpcionCamara(view: View) {
        view.findViewById<MaterialCardView>(R.id.cardCamera).setOnClickListener {
            takePictureLauncher.launch(null)
        }
    }

    // Anima la aparición de las tarjetas de opción al entrar a la pantalla
    private fun animarEntrada(view: View) {
        val content = view.findViewById<View>(R.id.deepResearchContent)
        content.alpha = 0f
        content.translationY = 32f
        content.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(350)
            .start()
    }
}

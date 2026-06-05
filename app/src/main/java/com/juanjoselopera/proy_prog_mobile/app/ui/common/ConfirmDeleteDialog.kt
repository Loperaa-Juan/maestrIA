package com.juanjoselopera.proy_prog_mobile.app.ui.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.juanjoselopera.proy_prog_mobile.R

/**
 * Diálogo de confirmación de borrado, con el estilo de la app (ícono de
 * advertencia en círculo rojo suave + acción destructiva). Reutilizable para
 * apuntes, materias, etc.
 */
object ConfirmDeleteDialog {

    fun show(
        context: Context,
        title: String,
        message: CharSequence,
        confirmText: String = "Eliminar",
        onConfirm: () -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete, null)
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .create()
        // Fondo transparente para que se vea solo nuestra card redondeada.
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.findViewById<TextView>(R.id.tvConfirmTitle).text = title
        view.findViewById<TextView>(R.id.tvConfirmMessage).text = message

        view.findViewById<MaterialButton>(R.id.btnConfirmDelete).apply {
            text = confirmText
            setOnClickListener {
                dialog.dismiss()
                onConfirm()
            }
        }
        view.findViewById<MaterialButton>(R.id.btnCancelDelete).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

package com.juanjoselopera.proy_prog_mobile.app.ui.landing

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentNoteAdapter(
    private val onNoteClick: (Note) -> Unit
) : ListAdapter<Note, RecentNoteAdapter.ViewHolder>(NoteDiffCallback()) {

    private var subjects: List<Subject> = emptyList()
    private val dateFormat = SimpleDateFormat("d MMM", Locale("es"))

    // Los mismos colores de acento que usa ApuntesFragment
    private val accentColors = listOf(
        Color.parseColor("#1565C0"),
        Color.parseColor("#2E7D32"),
        Color.parseColor("#C2185B"),
        Color.parseColor("#EF6C00"),
        Color.parseColor("#6A1B9A"),
        Color.parseColor("#00838F")
    )

    // Actualiza la lista de materias para asignar colores correctamente
    fun updateSubjects(newSubjects: List<Subject>) {
        subjects = newSubjects
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_note, parent, false)
        return ViewHolder(view)
    }

    // Vincula el apunte en la posición indicada a su ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorBar = itemView.findViewById<View>(R.id.noteColorBar)
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvNoteTitle)
        private val tvPreview = itemView.findViewById<TextView>(R.id.tvNotePreview)
        private val tvSubject = itemView.findViewById<TextView>(R.id.tvNoteSubject)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvNoteDate)

        // Rellena la tarjeta con los datos del apunte y aplica el color de su materia
        fun bind(note: Note) {
            val subject = subjects.find { it.id == note.subjectId }
            val accent = subject
                ?.let { accentColors.getOrElse(it.colorIndex) { accentColors[0] } }
                ?: accentColors[4]

            colorBar.setBackgroundColor(accent)
            tvTitle.text = note.title

            if (note.content.isNotBlank()) {
                tvPreview.text = note.content
                tvPreview.visibility = View.VISIBLE
            } else {
                tvPreview.visibility = View.GONE
            }

            if (note.subjectName.isNotBlank()) {
                tvSubject.text = note.subjectName
                tvSubject.setTypeface(tvSubject.typeface, Typeface.BOLD)
                tvSubject.setTextColor(accent)
                tvSubject.background = GradientDrawable().apply {
                    setColor(Color.argb(22, Color.red(accent), Color.green(accent), Color.blue(accent)))
                    cornerRadius = 40f
                }
                tvSubject.visibility = View.VISIBLE
            } else {
                tvSubject.visibility = View.GONE
            }

            tvDate.text = dateFormat.format(Date(note.createdAt))
            itemView.setOnClickListener { onNoteClick(note) }
        }
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}

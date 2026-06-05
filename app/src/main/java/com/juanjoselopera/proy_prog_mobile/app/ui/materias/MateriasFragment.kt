package com.juanjoselopera.proy_prog_mobile.app.ui.materias

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.ApuntesFragment
import com.juanjoselopera.proy_prog_mobile.app.util.animateChildrenSlideInFromRight
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MateriasFragment : Fragment() {

    data class Materia(
        val name: String,
        @DrawableRes val iconRes: Int,
        @ColorInt val accentColor: Int
    )

    private data class ColorPair(@ColorInt val bg: Int, @ColorInt val accent: Int)

    companion object {
        private val iconOptions = listOf(
            R.drawable.ic_book,
            R.drawable.ic_calculator,
            R.drawable.ic_globe,
            R.drawable.ic_history,
            R.drawable.ic_science,
            R.drawable.ic_music_note,
            R.drawable.ic_palette,
            R.drawable.ic_map_pin,
            R.drawable.ic_code,
            R.drawable.ic_leaf,
            R.drawable.ic_trophy,
            R.drawable.ic_folder,
            R.drawable.ic_document,
            R.drawable.ic_star
        )

        private val colorOptions = listOf(
            ColorPair(Color.parseColor("#E3F2FD"), Color.parseColor("#1565C0")),
            ColorPair(Color.parseColor("#E8F5E9"), Color.parseColor("#2E7D32")),
            ColorPair(Color.parseColor("#FCE4EC"), Color.parseColor("#C2185B")),
            ColorPair(Color.parseColor("#FFF3E0"), Color.parseColor("#EF6C00")),
            ColorPair(Color.parseColor("#F3E5F5"), Color.parseColor("#6A1B9A")),
            ColorPair(Color.parseColor("#E0F7FA"), Color.parseColor("#00838F")),
            ColorPair(Color.parseColor("#FFEBEE"), Color.parseColor("#C62828")),
            ColorPair(Color.parseColor("#E8EAF6"), Color.parseColor("#283593")),
            ColorPair(Color.parseColor("#E0F2F1"), Color.parseColor("#00695C")),
            ColorPair(Color.parseColor("#FFFDE7"), Color.parseColor("#F57F17")),
            ColorPair(Color.parseColor("#EFEBE9"), Color.parseColor("#4E342E")),
            ColorPair(Color.parseColor("#F5F5F5"), Color.parseColor("#424242")),
            ColorPair(Color.parseColor("#FBE9E7"), Color.parseColor("#BF360C")),
            ColorPair(Color.parseColor("#E1F5FE"), Color.parseColor("#0277BD")),
            ColorPair(Color.parseColor("#F9FBE7"), Color.parseColor("#558B2F")),
            ColorPair(Color.parseColor("#FFF8E1"), Color.parseColor("#FF6F00")),
            ColorPair(Color.parseColor("#EDE7F6"), Color.parseColor("#4527A0")),
            ColorPair(Color.parseColor("#ECEFF1"), Color.parseColor("#37474F"))
        )
    }

    private val viewModel: MateriasViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_materias, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val grid = view.findViewById<GridLayout>(R.id.materiasGrid)
        var lastCount = -1

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subjects.collect { subjects ->
                    grid.removeAllViews()
                    subjects.forEach { subject ->
                        addMateriaCard(grid, subject.id, subject.toMateria(), subject.name)
                    }
                    when {
                        lastCount == -1 && subjects.isNotEmpty() -> grid.animateChildrenSlideInFromRight()
                        subjects.size > lastCount && lastCount >= 0 -> animateNewCard(grid.getChildAt(grid.childCount - 1))
                    }
                    lastCount = subjects.size
                }
            }
        }

        view.findViewById<FloatingActionButton>(R.id.fabAddMateria).setOnClickListener {
            showCreateMateriaDialog()
        }
    }

    private fun Subject.toMateria(): Materia {
        val icon = iconOptions.getOrElse(iconIndex) { iconOptions[0] }
        val pair = colorOptions.getOrElse(colorIndex) { colorOptions[0] }
        return Materia(name = name, iconRes = icon, accentColor = pair.accent)
    }

    private fun addMateriaCard(grid: GridLayout, subjectId: String, materia: Materia, subjectName: String) {
        val context = grid.context

        val card = CardView(context).apply {
            radius = dp(20f)
            cardElevation = dp(6f)
            setCardBackgroundColor(materia.accentColor)
        }
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = dp(160).toInt()
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
            setMargins(dp(8).toInt(), dp(8).toInt(), dp(8).toInt(), dp(8).toInt())
        }

        // Root frame to allow layering
        val frame = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Decorative translucent circle, top-right
        val decorCircle = View(context).apply {
            val size = dp(90).toInt()
            val lp = FrameLayout.LayoutParams(size, size, Gravity.TOP or Gravity.END)
            lp.topMargin = -dp(22).toInt()
            lp.rightMargin = -dp(22).toInt()
            layoutParams = lp
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(35, 255, 255, 255))
            }
        }

        // Content column pinned to card edges
        val content = LinearLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16).toInt(), dp(16).toInt(), dp(16).toInt(), dp(14).toInt())
        }

        // Circular icon container
        val iconCircle = FrameLayout(context).apply {
            val size = dp(48).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(55, 255, 255, 255))
            }
        }
        val iconView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(dp(26).toInt(), dp(26).toInt(), Gravity.CENTER)
            setImageResource(materia.iconRes)
            imageTintList = ColorStateList.valueOf(Color.WHITE)
        }
        iconCircle.addView(iconView)

        // Push name to the bottom
        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val nameView = TextView(context).apply {
            text = subjectName
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 15f
            maxLines = 2
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val hintView = TextView(context).apply {
            text = "Ver apuntes →"
            setTextColor(Color.argb(180, 255, 255, 255))
            textSize = 11f
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(2).toInt()
            layoutParams = lp
        }

        content.addView(iconCircle)
        content.addView(spacer)
        content.addView(nameView)
        content.addView(hintView)

        frame.addView(decorCircle)
        frame.addView(content)
        card.addView(frame)

        card.setOnClickListener {
            (activity as? MainActivity)?.replaceMainFragment(
                ApuntesFragment.newInstance(subjectId, subjectName)
            )
        }

        card.setOnLongClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar materia")
                .setMessage("¿Seguro que quieres eliminar \"$subjectName\"?")
                .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteSubject(subjectId) }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        grid.addView(card, params)
    }

    private fun showCreateMateriaDialog() {
        val context = requireContext()
        val dialog = BottomSheetDialog(context)
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_create_materia, null)
        dialog.setContentView(dialogView)

        var iconIdx = 0
        var colorIdx = 0

        val previewBg = dialogView.findViewById<FrameLayout>(R.id.previewBg)
        val previewIcon = dialogView.findViewById<ImageView>(R.id.previewIcon)
        val nameInput = dialogView.findViewById<EditText>(R.id.etMateriaName)
        val confirmBtn = dialogView.findViewById<MaterialButton>(R.id.btnConfirm)
        val iconPicker = dialogView.findViewById<LinearLayout>(R.id.iconPicker)
        val colorPicker = dialogView.findViewById<LinearLayout>(R.id.colorPicker)

        fun applyPreview() {
            val pair = colorOptions[colorIdx]
            previewBg.background = circle(pair.bg)
            previewIcon.setImageResource(iconOptions[iconIdx])
            previewIcon.imageTintList = ColorStateList.valueOf(pair.accent)
            confirmBtn.backgroundTintList = ColorStateList.valueOf(pair.accent)
        }

        iconOptions.forEachIndexed { index, iconRes ->
            val swatch = buildIconSwatch(context, iconRes, index == iconIdx)
            swatch.setOnClickListener {
                iconIdx = index
                refreshIconSelection(iconPicker, iconIdx)
                applyPreview()
            }
            iconPicker.addView(swatch)
        }

        colorOptions.forEachIndexed { index, pair ->
            val swatch = buildColorSwatch(context, pair, index == colorIdx)
            swatch.setOnClickListener {
                colorIdx = index
                refreshColorSelection(colorPicker, colorIdx)
                applyPreview()
            }
            colorPicker.addView(swatch)
        }

        applyPreview()

        confirmBtn.setOnClickListener {
            val name = nameInput.text?.toString()?.trim().orEmpty()
            if (name.isEmpty()) {
                Toast.makeText(context, "Ponle un nombre a la materia", Toast.LENGTH_SHORT).show()
                nameInput.requestFocus()
                return@setOnClickListener
            }
            viewModel.addSubject(name, iconIdx, colorIdx)
            dialog.dismiss()
        }

        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                confirmBtn.isEnabled = !s.isNullOrBlank()
            }
        })
        confirmBtn.isEnabled = false

        dialog.show()
    }

    private fun buildIconSwatch(context: Context, @DrawableRes iconRes: Int, selected: Boolean): View {
        val size = dp(56).toInt()
        val wrap = FrameLayout(context).apply {
            val lp = LinearLayout.LayoutParams(size, size)
            lp.rightMargin = dp(10).toInt()
            layoutParams = lp
            background = iconSwatchBackground(selected)
            tag = "icon-swatch"
        }
        val img = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(dp(28).toInt(), dp(28).toInt(), Gravity.CENTER)
            setImageResource(iconRes)
            imageTintList = ColorStateList.valueOf(Color.parseColor("#444444"))
        }
        wrap.addView(img)
        return wrap
    }

    private fun buildColorSwatch(context: Context, pair: ColorPair, selected: Boolean): View {
        val size = dp(40).toInt()
        return View(context).apply {
            val lp = LinearLayout.LayoutParams(size, size)
            lp.rightMargin = dp(12).toInt()
            layoutParams = lp
            background = colorSwatchBackground(pair.accent, selected)
            tag = "color-swatch"
        }
    }

    private fun refreshIconSelection(picker: LinearLayout, selectedIdx: Int) {
        for (i in 0 until picker.childCount) {
            picker.getChildAt(i).background = iconSwatchBackground(i == selectedIdx)
        }
    }

    private fun refreshColorSelection(picker: LinearLayout, selectedIdx: Int) {
        for (i in 0 until picker.childCount) {
            picker.getChildAt(i).background =
                colorSwatchBackground(colorOptions[i].accent, i == selectedIdx)
        }
    }

    private fun iconSwatchBackground(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(requireContext().getColor(R.color.colorChipNeutralBackground))
            if (selected) setStroke(dp(2).toInt(), requireContext().getColor(R.color.colorAccentPrimary))
        }
    }

    private fun colorSwatchBackground(@ColorInt accent: Int, selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(accent)
            if (selected) setStroke(dp(3).toInt(), Color.parseColor("#1F2937"))
        }
    }

    private fun circle(@ColorInt color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun animateNewCard(view: View?) {
        view ?: return
        view.alpha = 0f
        view.scaleX = 0.85f
        view.scaleY = 0.85f
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(OvershootInterpolator(1.4f))
            .setDuration(320)
            .start()
    }

    private fun dp(value: Int): Float = dp(value.toFloat())
    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}

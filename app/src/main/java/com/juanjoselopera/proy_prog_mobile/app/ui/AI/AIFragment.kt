package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.data.local.AIModelPrefs
import com.juanjoselopera.proy_prog_mobile.app.util.animateChildrenSlideInFromBottom
import com.juanjoselopera.proy_prog_mobile.app.util.findFirstViewGroupById

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AIFragment : Fragment() {

    private data class AITool(
        val cardId: Int,
        val title: String,
        val description: String,
        @DrawableRes val iconRes: Int,
        val accentColor: Int,
        val iconBgRes: Int
    )

    private val tools by lazy {
        listOf(
            AITool(
                R.id.cardResearch,
                "Deep Research",
                "Investiga temas a fondo con fuentes verificadas y resúmenes contextualizados.",
                R.drawable.ic_search,
                Color.parseColor("#7C3AED"),
                R.drawable.bg_ai_icon_purple
            ),
            AITool(
                R.id.cardQuestions,
                "Preguntas",
                "Genera cuestionarios automáticos para repasar antes de un examen.",
                R.drawable.ic_help,
                Color.parseColor("#1D4ED8"),
                R.drawable.bg_ai_icon_blue
            ),
            AITool(
                R.id.cardConcepts,
                "Conceptos",
                "Identifica las ideas centrales y obtén explicaciones claras y breves.",
                R.drawable.ic_edit,
                Color.parseColor("#047857"),
                R.drawable.bg_ai_icon_green
            ),
            AITool(
                R.id.cardSummary,
                "Resúmenes",
                "Condensa tus apuntes largos en resúmenes accionables al instante.",
                R.drawable.ic_summarize,
                Color.parseColor("#C2410C"),
                R.drawable.bg_ai_icon_orange
            )
        )
    }

    private val availableModels: List<AIModel> = listOf(
        AIModel("gemini-2.5-flash", "Gemini 2.5 Flash", "Google"),
        AIModel("gemini-2.5-pro", "Gemini 2.5 Pro", "Google"),
        AIModel("gemini-2.5-flash-lite", "Gemini 2.5 Flash Lite", "Google"),
    )

    private var pulseAnimator: ValueAnimator? = null
    private var selectedCardId: Int? = null
    private var tvSelectedModel: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_ai, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findFirstViewGroupById(R.id.aiToolsGrid)?.animateChildrenSlideInFromBottom()
        animateHeroEntrance(view.findViewById(R.id.aiHeroCard))
        startPulse(view.findViewById(R.id.heroIcon))

        tools.forEach { tool ->
            val card = view.findViewById<MaterialCardView>(tool.cardId)
            card.setOnClickListener { selectTool(view, tool, card) }
        }

        view.findViewById<MaterialButton>(R.id.selectedAction)
            .setOnClickListener { btn ->
                btn.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).withEndAction {
                    btn.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()
                val fragment = when (selectedCardId) {
                    R.id.cardResearch -> DeepResearchFragment()
                    R.id.cardQuestions -> NotePickerFragment.newInstance(ToolType.QUESTIONS)
                    R.id.cardConcepts -> NotePickerFragment.newInstance(ToolType.CONCEPTS)
                    R.id.cardSummary -> NotePickerFragment.newInstance(ToolType.SUMMARY)
                    else -> return@setOnClickListener
                }
                (requireActivity() as MainActivity).replaceMainFragment(fragment)
            }

        tvSelectedModel = view.findViewById(R.id.tvSelectedModel)
        val savedId = AIModelPrefs.getSelectedId(requireContext())
        val current = availableModels.find { it.id == savedId } ?: availableModels.first()
        AIModelPrefs.save(requireContext(), current.id, current.name)
        tvSelectedModel?.text = current.name

        view.findViewById<View>(R.id.modelSelectorCard).setOnClickListener {
            showModelPicker()
        }
    }

    private fun showModelPicker() {
        val ctx = requireContext()
        val dialog = BottomSheetDialog(ctx)
        val sheetView = LayoutInflater.from(ctx).inflate(R.layout.dialog_model_picker, null)
        dialog.setContentView(sheetView)

        val modelList = sheetView.findViewById<LinearLayout>(R.id.modelList)
        val emptyState = sheetView.findViewById<View>(R.id.emptyState)

        if (availableModels.isEmpty()) {
            emptyState.visibility = View.VISIBLE
        } else {
            val selectedId = AIModelPrefs.getSelectedId(ctx)
            availableModels.forEach { model ->
                val row = buildModelRow(model, model.id == selectedId) {
                    AIModelPrefs.save(ctx, model.id, model.name)
                    tvSelectedModel?.text = model.name
                    dialog.dismiss()
                }
                modelList.addView(row)
            }
        }

        dialog.show()
    }

    private fun buildModelRow(model: AIModel, isSelected: Boolean, onClick: () -> Unit): View {
        val ctx = requireContext()
        val dp = resources.displayMetrics.density

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, (14 * dp).toInt(), 0, (14 * dp).toInt())
            isClickable = true
            isFocusable = true
            val outValue = TypedValue()
            ctx.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
            setOnClickListener { onClick() }
        }

        val textBlock = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nameView = TextView(ctx).apply {
            text = model.name
            textSize = 14f
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
        }

        val providerView = TextView(ctx).apply {
            text = model.provider
            textSize = 12f
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextSecondary))
        }

        textBlock.addView(nameView)
        textBlock.addView(providerView)
        row.addView(textBlock)

        if (isSelected) {
            val check = ImageView(ctx).apply {
                setImageResource(R.drawable.ic_verified)
                imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.colorAccentPurple)
                )
                layoutParams = LinearLayout.LayoutParams(
                    (22 * dp).toInt(), (22 * dp).toInt()
                )
            }
            row.addView(check)
        }

        val divider = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).also { it.topMargin = (14 * dp).toInt() }
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorDivider))
        }

        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(row)
            addView(divider)
        }
    }

    private fun animateHeroEntrance(hero: View) {
        hero.alpha = 0f
        hero.translationY = -40f
        hero.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(420)
            .start()
    }

    private fun startPulse(view: View) {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.12f).apply {
            duration = 900L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                val v = it.animatedValue as Float
                view.scaleX = v
                view.scaleY = v
            }
            start()
        }
    }

    private fun selectTool(root: View, tool: AITool, card: MaterialCardView) {
        if (selectedCardId == tool.cardId) return

        tools.forEach { t ->
            val c = root.findViewById<MaterialCardView>(t.cardId)
            c.strokeColor = Color.TRANSPARENT
            c.cardElevation = 2f * resources.displayMetrics.density
        }
        card.strokeColor = tool.accentColor
        card.cardElevation = 6f * resources.displayMetrics.density

        card.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90)
            .withEndAction {
                card.animate().scaleX(1f).scaleY(1f)
                    .setInterpolator(OvershootInterpolator(2f))
                    .setDuration(180).start()
            }.start()

        selectedCardId = tool.cardId
        updatePreview(root, tool)
    }

    private fun updatePreview(root: View, tool: AITool) {
        val iconWrap = root.findViewById<View>(R.id.selectedIconWrap)
        val icon = root.findViewById<ImageView>(R.id.selectedIcon)
        val title = root.findViewById<TextView>(R.id.selectedTitle)
        val desc = root.findViewById<TextView>(R.id.selectedDesc)
        val action = root.findViewById<MaterialButton>(R.id.selectedAction)

        iconWrap.setBackgroundResource(tool.iconBgRes)
        icon.setImageResource(tool.iconRes)
        icon.imageTintList = ColorStateList.valueOf(tool.accentColor)
        title.text = tool.title
        desc.text = tool.description
        action.isEnabled = true
        action.backgroundTintList = ColorStateList.valueOf(tool.accentColor)
        action.text = "Probar ${tool.title}"

        val target = root.findViewById<View>(R.id.selectedCard)
        target.alpha = 0f
        target.translationY = 24f
        target.animate().alpha(1f).translationY(0f).setDuration(260).start()
    }

    override fun onDestroyView() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        tvSelectedModel = null
        super.onDestroyView()
    }
}

package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.util.animateChildrenSlideInFromBottom
import com.juanjoselopera.proy_prog_mobile.app.util.findFirstViewGroupById

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
                android.R.drawable.ic_menu_search,
                Color.parseColor("#7C3AED"),
                R.drawable.bg_ai_icon_purple
            ),
            AITool(
                R.id.cardQuestions,
                "Preguntas",
                "Genera cuestionarios automáticos para repasar antes de un examen.",
                android.R.drawable.ic_menu_help,
                Color.parseColor("#1D4ED8"),
                R.drawable.bg_ai_icon_blue
            ),
            AITool(
                R.id.cardConcepts,
                "Conceptos",
                "Identifica las ideas centrales y obtén explicaciones claras y breves.",
                android.R.drawable.ic_menu_edit,
                Color.parseColor("#047857"),
                R.drawable.bg_ai_icon_green
            ),
            AITool(
                R.id.cardSummary,
                "Resúmenes",
                "Condensa tus apuntes largos en resúmenes accionables al instante.",
                android.R.drawable.ic_menu_agenda,
                Color.parseColor("#C2410C"),
                R.drawable.bg_ai_icon_orange
            )
        )
    }

    private var pulseAnimator: ValueAnimator? = null
    private var selectedCardId: Int? = null

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
            .setOnClickListener { it.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start() }
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
        super.onDestroyView()
    }
}

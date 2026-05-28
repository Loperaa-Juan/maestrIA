package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.GrammarLocatorDef
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j

object MarkwonProvider {
    fun create(context: Context): Markwon {
        val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val prism4j = Prism4j(GrammarLocatorDef())
        val textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 15f, context.resources.displayMetrics
        )
        return Markwon.builder(context)
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, if (isDark) Prism4jThemeDarkula.create() else Prism4jThemeDefault.create()))
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create(textSizePx) { it.inlinesEnabled(true) })
            .build()
    }
}

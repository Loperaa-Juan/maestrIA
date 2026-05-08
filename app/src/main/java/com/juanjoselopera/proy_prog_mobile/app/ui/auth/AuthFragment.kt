package com.juanjoselopera.proy_prog_mobile.app.ui.auth

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.ui.login.LoginFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.signup.SignUpFragment
import com.juanjoselopera.proy_prog_mobile.databinding.FragmentAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            val startTab = arguments?.getString(ARG_TAB) ?: TAB_LOGIN
            // commitNow avoids a one-frame flicker before the form appears
            if (startTab == TAB_SIGNUP) showTab(TAB_SIGNUP, animate = false)
            else showTab(TAB_LOGIN, animate = false)
        }

        binding.btnTabLogin.setOnClickListener { showTab(TAB_LOGIN) }
        binding.btnTabSignup.setOnClickListener { showTab(TAB_SIGNUP) }
    }

    private fun showTab(tab: String, animate: Boolean = true) {
        val loginSelected = tab == TAB_LOGIN

        binding.tvAuthSubtitle.setText(
            if (loginSelected) R.string.auth_subtitle_login else R.string.auth_subtitle_signup
        )
        updateTabStyle(loginSelected)

        val fragment = if (loginSelected) LoginFragment() else SignUpFragment()
        val transaction = childFragmentManager.beginTransaction()
        if (animate) transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        transaction.replace(R.id.authFormContainer, fragment)
        if (animate) transaction.commit() else transaction.commitNow()
    }

    private fun updateTabStyle(loginSelected: Boolean) {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.colorActionButton)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.colorCardBackground)
        val activeTextColor = Color.WHITE
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.colorTextSecondary)

        binding.btnTabLogin.apply {
            backgroundTintList = ColorStateList.valueOf(if (loginSelected) activeColor else inactiveColor)
            setTextColor(if (loginSelected) activeTextColor else inactiveTextColor)
        }
        binding.btnTabSignup.apply {
            backgroundTintList = ColorStateList.valueOf(if (!loginSelected) activeColor else inactiveColor)
            setTextColor(if (!loginSelected) activeTextColor else inactiveTextColor)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAB = "tab"
        const val TAB_LOGIN = "login"
        const val TAB_SIGNUP = "signup"

        fun newInstance(tab: String = TAB_LOGIN) = AuthFragment().apply {
            arguments = Bundle().apply { putString(ARG_TAB, tab) }
        }
    }
}

package com.juanjoselopera.proy_prog_mobile.app.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.ui.landing.LandingFragment
import com.juanjoselopera.proy_prog_mobile.app.ui.signup.SignUpFragment
import com.juanjoselopera.proy_prog_mobile.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initListeners()
    }

    private fun initListeners() {
        with(binding) {
            btnIngresar.setOnClickListener {
                val email = useremail.text.toString().trim()
                val password = userpassword.text.toString().trim()
                viewModel.login(email, password)
            }

            tvCreateAccount.setOnClickListener {
                (activity as? MainActivity)?.let { mainActivity ->
                    mainActivity.setNavComponentsVisibility(false)
                    mainActivity.replaceMainFragment(SignUpFragment())
                }
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun handleUiState(state: LoginUiState) {
        with(binding) {
            tilEmail.error = state.emailError
            tilPassword.error = state.passwordError

            btnIngresar.isEnabled = !state.isLoading

            if (state.isSuccess) {
                navigateToHome()
            }

            state.error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.resetError()
            }
        }
    }

    private fun navigateToHome() {
        (activity as? MainActivity)?.let { mainActivity ->
            mainActivity.setNavComponentsVisibility(true)
            mainActivity.replaceMainFragment(LandingFragment(), false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

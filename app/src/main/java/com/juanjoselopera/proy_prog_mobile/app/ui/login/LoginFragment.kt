package com.juanjoselopera.proy_prog_mobile.app.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.ui.landing.LandingFragment
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import com.juanjoselopera.proy_prog_mobile.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

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
        binding.btnIngresar.setOnClickListener {
            handleLogin()
        }
    }

    private fun initObservers() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    (activity as? MainActivity)?.let { mainActivity ->
                        mainActivity.setNavComponentsVisibility(true)
                        mainActivity.replaceMainFragment(LandingFragment(), false)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Opcional: mostrar un progress bar
                }
                is Resource.Finished -> {
                    // Opcional: ocultar progress bar
                }
            }
        }
    }

    private fun handleLogin() {
        val email = binding.useremail.text.toString().trim()
        val password = binding.userpassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            viewModel.login(email, password)
        } else {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

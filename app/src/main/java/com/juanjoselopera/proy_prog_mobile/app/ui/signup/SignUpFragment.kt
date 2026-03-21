package com.juanjoselopera.proy_prog_mobile.app.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import com.juanjoselopera.proy_prog_mobile.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initListeners()
    }

    private fun initListeners() {
        with(binding) {
            btnRegistrar.setOnClickListener {
                handleSignUp()
            }
        }
    }

    private fun initObservers() {
        viewModel.signUpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()

                    activity?.onBackPressed()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun handleSignUp() {
        val email = binding.useremail.text.toString()
        val password = binding.userpassword.text.toString()
        viewModel.signUp(email, password)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

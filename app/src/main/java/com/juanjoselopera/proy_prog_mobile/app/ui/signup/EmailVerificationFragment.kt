package com.juanjoselopera.proy_prog_mobile.app.ui.signup

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.app.MainActivity
import com.juanjoselopera.proy_prog_mobile.app.ui.landing.LandingFragment
import com.juanjoselopera.proy_prog_mobile.app.util.SessionManager
import com.juanjoselopera.proy_prog_mobile.databinding.FragmentEmailVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class EmailVerificationFragment : Fragment() {

    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var sessionManager: SessionManager

    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!

    private var lastResendTime = 0L
    private val resendCooldownMs = 60_000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = firebaseAuth.currentUser?.email ?: ""
        binding.tvMessage.text = "Enviamos un correo a $email.\nVerifica tu cuenta para continuar."

        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnVerified.setOnClickListener {
            checkVerification()
        }

        binding.btnResend.setOnClickListener {
            resendVerificationEmail()
        }
    }

    private fun checkVerification() {
        binding.btnVerified.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                firebaseAuth.currentUser?.reload()?.await()
                if (firebaseAuth.currentUser?.isEmailVerified == true) {
                    val uid = firebaseAuth.currentUser?.uid ?: ""
                    val email = firebaseAuth.currentUser?.email ?: ""
                    sessionManager.saveSession(uid = uid, email = email)
                    (activity as? MainActivity)?.let {
                        it.setNavComponentsVisibility(true)
                        it.replaceMainFragment(LandingFragment(), false)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Aún no hemos detectado la verificación. Revisa tu correo.",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.btnVerified.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error al verificar", Toast.LENGTH_SHORT).show()
                binding.btnVerified.isEnabled = true
            }
        }
    }

    private fun resendVerificationEmail() {
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - lastResendTime
        if (lastResendTime > 0 && elapsed < resendCooldownMs) {
            val remaining = ((resendCooldownMs - elapsed) / 1000).toInt()
            Toast.makeText(requireContext(), "Espera $remaining segundos para reenviar", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnResend.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                firebaseAuth.currentUser?.sendEmailVerification()?.await()
                lastResendTime = SystemClock.elapsedRealtime()
                Toast.makeText(requireContext(), "Correo reenviado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error al reenviar", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnResend.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

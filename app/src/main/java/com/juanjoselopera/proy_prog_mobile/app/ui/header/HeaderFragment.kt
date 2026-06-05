package com.juanjoselopera.proy_prog_mobile.app.ui.header

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.ui.profile.UserProfileActivity
import com.juanjoselopera.proy_prog_mobile.app.util.ProfileImageStore

class HeaderFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_header, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            startActivity(Intent(requireContext(), UserProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAvatar()
    }

    // Refleja la foto de perfil (local u online) en el avatar del header.
    // Público para poder refrescarlo al iniciar sesión (cuando el header ya
    // existe pero su onResume corrió sin usuario).
    fun refreshAvatar() {
        val iv = view?.findViewById<ImageView>(R.id.ivProfile) ?: return
        val context = iv.context
        val local = ProfileImageStore.existing(context)
        val photoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl

        when {
            local != null -> {
                iv.setPadding(0, 0, 0, 0)
                iv.imageTintList = null
                Glide.with(this)
                    .load(local)
                    .signature(ObjectKey(local.lastModified()))
                    .circleCrop()
                    .into(iv)
            }
            photoUrl != null -> {
                iv.setPadding(0, 0, 0, 0)
                iv.imageTintList = null
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(iv)
            }
            else -> {
                val pad = (8 * resources.displayMetrics.density).toInt()
                Glide.with(this).clear(iv)
                iv.setImageResource(R.drawable.ic_person)
                iv.imageTintList = ColorStateList.valueOf(context.getColor(R.color.colorAccentPrimary))
                iv.setPadding(pad, pad, pad, pad)
            }
        }
    }
}

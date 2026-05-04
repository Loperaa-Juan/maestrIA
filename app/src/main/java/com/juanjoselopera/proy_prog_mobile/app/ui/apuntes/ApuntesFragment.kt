package com.juanjoselopera.proy_prog_mobile.app.ui.apuntes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juanjoselopera.proy_prog_mobile.R
import com.juanjoselopera.proy_prog_mobile.app.util.animateChildrenSlideInFromBottom
import com.juanjoselopera.proy_prog_mobile.app.util.findFirstViewGroupById

class ApuntesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apuntes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findFirstViewGroupById(R.id.apuntesList)?.animateChildrenSlideInFromBottom()
    }
}

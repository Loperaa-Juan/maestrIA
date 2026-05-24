package com.juanjoselopera.proy_prog_mobile.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val imageUri: String? = null
) : Parcelable

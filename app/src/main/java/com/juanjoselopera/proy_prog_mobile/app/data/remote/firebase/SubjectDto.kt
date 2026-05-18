package com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase

import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject

data class SubjectDto(
    val id: String = "",
    val name: String = "",
    val iconIndex: Int = 0,
    val colorIndex: Int = 0
) {
    fun toSubject() = Subject(id = id, name = name, iconIndex = iconIndex, colorIndex = colorIndex)

    companion object {
        fun fromSubject(subject: Subject) = SubjectDto(
            id = subject.id,
            name = subject.name,
            iconIndex = subject.iconIndex,
            colorIndex = subject.colorIndex
        )
    }
}

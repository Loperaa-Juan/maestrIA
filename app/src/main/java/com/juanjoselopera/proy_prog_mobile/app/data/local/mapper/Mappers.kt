package com.juanjoselopera.proy_prog_mobile.app.data.local.mapper

import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.NoteEntity
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.SubjectEntity
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectDto
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject

// ----- Subject -----
// Nota: toDomain() omite a propósito updatedAt/pendingSync/deleted: son metadatos
// de sincronización y el modelo de dominio se mantiene ajeno a ellos.

fun SubjectEntity.toDomain() = Subject(
    id = id, name = name, iconIndex = iconIndex, colorIndex = colorIndex
)

fun SubjectEntity.toDto() = SubjectDto(
    id = id, name = name, iconIndex = iconIndex, colorIndex = colorIndex, updatedAt = updatedAt
)

fun SubjectDto.toEntity(pendingSync: Boolean) = SubjectEntity(
    id = id, name = name, iconIndex = iconIndex, colorIndex = colorIndex,
    updatedAt = updatedAt, pendingSync = pendingSync, deleted = false
)

// ----- Note -----

fun NoteEntity.toDomain() = Note(
    id = id, title = title, content = content, subjectId = subjectId,
    subjectName = subjectName, tags = tags, createdAt = createdAt, imageUri = imageUri
)

fun NoteEntity.toDto() = NoteDto(
    id = id, title = title, content = content, subjectId = subjectId,
    subjectName = subjectName, tags = tags, createdAt = createdAt,
    imageUri = imageUri, updatedAt = updatedAt
)

fun NoteDto.toEntity(pendingSync: Boolean) = NoteEntity(
    id = id, title = title, content = content, subjectId = subjectId,
    subjectName = subjectName, tags = tags, createdAt = createdAt,
    imageUri = imageUri, updatedAt = updatedAt, pendingSync = pendingSync, deleted = false
)

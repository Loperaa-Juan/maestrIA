package com.juanjoselopera.proy_prog_mobile.data.sync

import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.NoteEntity
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.SubjectEntity
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectDto
import com.juanjoselopera.proy_prog_mobile.app.data.sync.SyncEngine
import com.juanjoselopera.proy_prog_mobile.data.fakes.FakeNoteDao
import com.juanjoselopera.proy_prog_mobile.data.fakes.FakeNoteSyncSource
import com.juanjoselopera.proy_prog_mobile.data.fakes.FakeSubjectDao
import com.juanjoselopera.proy_prog_mobile.data.fakes.FakeSubjectSyncSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncEngineTest {

    private lateinit var subjectDao: FakeSubjectDao
    private lateinit var noteDao: FakeNoteDao
    private lateinit var subjectRemote: FakeSubjectSyncSource
    private lateinit var noteRemote: FakeNoteSyncSource
    private lateinit var engine: SyncEngine

    private fun subject(
        id: String, name: String = "M", updatedAt: Long = 0L,
        pendingSync: Boolean = false, deleted: Boolean = false
    ) = SubjectEntity(id, name, 0, 0, updatedAt, pendingSync, deleted)

    private fun note(
        id: String, title: String = "T", updatedAt: Long = 0L,
        pendingSync: Boolean = false, deleted: Boolean = false
    ) = NoteEntity(
        id = id, title = title, content = "content", subjectId = "s1", subjectName = "Sub",
        tags = emptyList(), createdAt = 0L, imageUri = null,
        updatedAt = updatedAt, pendingSync = pendingSync, deleted = deleted
    )

    @Before
    fun setup() {
        subjectDao = FakeSubjectDao()
        noteDao = FakeNoteDao()
        subjectRemote = FakeSubjectSyncSource()
        noteRemote = FakeNoteSyncSource()
        engine = SyncEngine(subjectDao, noteDao, subjectRemote, noteRemote)
    }

    @Test
    fun `push sends pending subject to remote and marks it synced`() = runTest {
        subjectDao.rows.value = listOf(subject("a", name = "Algebra", updatedAt = 10, pendingSync = true))
        engine.sync()
        assertTrue(subjectRemote.remote.containsKey("a"))
        assertFalse(subjectDao.getById("a")!!.pendingSync)
    }

    @Test
    fun `push deletes tombstone remotely and hard-deletes locally`() = runTest {
        subjectDao.rows.value = listOf(subject("a", updatedAt = 5, pendingSync = true, deleted = true))
        engine.sync()
        assertTrue(subjectRemote.deleted.contains("a"))
        assertNull(subjectDao.getById("a"))
    }

    @Test
    fun `pull inserts remote subject that is missing locally`() = runTest {
        subjectRemote.remote["b"] = SubjectDto(id = "b", name = "Bio", updatedAt = 7)
        engine.sync()
        val local = subjectDao.getById("b")
        assertEquals("Bio", local?.name)
        assertFalse(local!!.pendingSync)
    }

    @Test
    fun `pull overwrites local when remote is newer and local not pending`() = runTest {
        subjectDao.rows.value = listOf(subject("a", name = "Old", updatedAt = 1, pendingSync = false))
        subjectRemote.remote["a"] = SubjectDto(id = "a", name = "New", updatedAt = 9)
        engine.sync()
        assertEquals("New", subjectDao.getById("a")?.name)
    }

    @Test
    fun `pull does not overwrite local when local is newer`() = runTest {
        subjectDao.rows.value = listOf(subject("a", name = "Local", updatedAt = 20, pendingSync = false))
        subjectRemote.remote["a"] = SubjectDto(id = "a", name = "Remote", updatedAt = 5)
        engine.sync()
        assertEquals("Local", subjectDao.getById("a")?.name)
    }

    @Test
    fun `pull does not overwrite local pending changes`() = runTest {
        subjectDao.rows.value = listOf(subject("a", name = "Local", updatedAt = 1, pendingSync = true))
        subjectRemote.remote["a"] = SubjectDto(id = "a", name = "Remote", updatedAt = 9)
        engine.sync()
        // push lo envía primero; tras markSynced el pull no debe revertirlo
        assertEquals("Local", subjectDao.getById("a")?.name)
    }

    @Test
    fun `pull removes local synced subject that is absent remotely`() = runTest {
        subjectDao.rows.value = listOf(subject("a", name = "Gone", updatedAt = 3, pendingSync = false))
        // remoto vacío
        engine.sync()
        assertNull(subjectDao.getById("a"))
    }

    @Test
    fun `push sends pending note to remote and marks it synced`() = runTest {
        noteDao.rows.value = listOf(note("n1", title = "Derivadas", updatedAt = 10, pendingSync = true))
        engine.sync()
        assertTrue(noteRemote.remote.containsKey("n1"))
        assertFalse(noteDao.getById("n1")!!.pendingSync)
    }

    @Test
    fun `push deletes note tombstone remotely and hard-deletes locally`() = runTest {
        noteDao.rows.value = listOf(note("n1", updatedAt = 5, pendingSync = true, deleted = true))
        engine.sync()
        assertTrue(noteRemote.deleted.contains("n1"))
        assertNull(noteDao.getById("n1"))
    }

    @Test
    fun `pull inserts remote note that is missing locally`() = runTest {
        noteRemote.remote["n2"] = NoteDto(id = "n2", title = "Bio", updatedAt = 7)
        engine.sync()
        assertEquals("Bio", noteDao.getById("n2")?.title)
        assertFalse(noteDao.getById("n2")!!.pendingSync)
    }

    @Test
    fun `pull removes local synced note that is absent remotely`() = runTest {
        noteDao.rows.value = listOf(note("n1", title = "Gone", updatedAt = 3, pendingSync = false))
        engine.sync()
        assertNull(noteDao.getById("n1"))
    }
}

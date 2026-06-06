package com.juanjoselopera.proy_prog_mobile.data

import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.NoteRepositoryImpl
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.data.fakes.FakeNoteDao
import com.juanjoselopera.proy_prog_mobile.data.fakes.FakeSyncTrigger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteRepositoryImplTest {

    private lateinit var dao: FakeNoteDao
    private lateinit var trigger: FakeSyncTrigger
    private lateinit var repo: NoteRepositoryImpl

    @Before
    fun setup() {
        dao = FakeNoteDao()
        trigger = FakeSyncTrigger()
        repo = NoteRepositoryImpl(dao, trigger)
    }

    @Test
    fun `addNote stores a pending markdown note with generated id`() = runTest {
        val id = repo.addNote(
            Note(title = "Derivadas", content = "# Derivadas\n- regla", subjectId = "s1", subjectName = "Mate")
        )
        assertTrue(id.isNotEmpty())
        val row = dao.getById(id)!!
        assertEquals("# Derivadas\n- regla", row.content)
        assertTrue(row.pendingSync)
        assertFalse(row.deleted)
        assertEquals(1, trigger.count)
    }

    @Test
    fun `deleteNote creates a tombstone`() = runTest {
        val id = repo.addNote(Note(title = "X", subjectId = "s1", subjectName = "Mate"))
        repo.deleteNote(id)
        val row = dao.getById(id)!!
        assertTrue(row.deleted)
        assertTrue(row.pendingSync)
        assertEquals(2, trigger.count)
    }

    @Test
    fun `updateNote updates content, preserves createdAt and stays pending`() = runTest {
        val id = repo.addNote(Note(title = "A", content = "old", subjectId = "s1", subjectName = "Mate"))
        val createdAt = dao.getById(id)!!.createdAt
        repo.updateNote(Note(id = id, title = "A2", content = "new", subjectId = "s1", subjectName = "Mate"))
        val row = dao.getById(id)!!
        assertEquals("new", row.content)
        assertEquals("A2", row.title)
        assertEquals(createdAt, row.createdAt)
        assertTrue(row.pendingSync)
    }

    @Test
    fun `updateNote on missing id does not create a row`() = runTest {
        repo.updateNote(Note(id = "nope", title = "X", subjectId = "s1", subjectName = "Mate"))
        assertNull(dao.getById("nope"))
    }

    @Test
    fun `getNotes filters by subject`() = runTest {
        repo.addNote(Note(title = "A", subjectId = "s1", subjectName = "Mate"))
        repo.addNote(Note(title = "B", subjectId = "s2", subjectName = "Bio"))
        val s1Notes = repo.getNotes("s1").first()
        assertEquals(1, s1Notes.size)
        assertEquals("A", s1Notes[0].title)
    }

    @Test
    fun `getNotes with null subject returns all notes`() = runTest {
        repo.addNote(Note(title = "A", subjectId = "s1", subjectName = "Mate"))
        repo.addNote(Note(title = "B", subjectId = "s2", subjectName = "Bio"))
        val all = repo.getNotes(null).first()
        assertEquals(2, all.size)
    }
}

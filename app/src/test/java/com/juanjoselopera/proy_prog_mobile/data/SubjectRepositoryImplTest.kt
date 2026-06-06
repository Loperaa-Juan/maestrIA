package com.juanjoselopera.proy_prog_mobile.data

import com.juanjoselopera.proy_prog_mobile.app.data.remote.firebase.SubjectRepositoryImpl
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.data.fakes.FakeSubjectDao
import com.juanjoselopera.proy_prog_mobile.data.fakes.FakeSyncTrigger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubjectRepositoryImplTest {

    private lateinit var dao: FakeSubjectDao
    private lateinit var trigger: FakeSyncTrigger
    private lateinit var repo: SubjectRepositoryImpl

    @Before
    fun setup() {
        dao = FakeSubjectDao()
        trigger = FakeSyncTrigger()
        repo = SubjectRepositoryImpl(dao, trigger)
    }

    @Test
    fun `addSubject stores a pending row with a generated id and requests sync`() = runTest {
        val id = repo.addSubject(Subject(name = "Algebra", iconIndex = 1, colorIndex = 2))
        assertTrue(id.isNotEmpty())
        val row = dao.getById(id)!!
        assertEquals("Algebra", row.name)
        assertTrue(row.pendingSync)
        assertFalse(row.deleted)
        assertEquals(1, trigger.count)
    }

    @Test
    fun `deleteSubject creates a tombstone and requests sync`() = runTest {
        val id = repo.addSubject(Subject(name = "Bio"))
        repo.deleteSubject(id)
        val row = dao.getById(id)!!
        assertTrue(row.deleted)
        assertTrue(row.pendingSync)
        assertEquals(2, trigger.count)
    }

    @Test
    fun `updateSubject updates existing row and keeps it pending`() = runTest {
        val id = repo.addSubject(Subject(name = "Old", iconIndex = 1, colorIndex = 1))
        repo.updateSubject(Subject(id = id, name = "New", iconIndex = 2, colorIndex = 3))
        val row = dao.getById(id)!!
        assertEquals("New", row.name)
        assertEquals(2, row.iconIndex)
        assertTrue(row.pendingSync)
    }

    @Test
    fun `updateSubject on missing id does not create a row`() = runTest {
        repo.updateSubject(Subject(id = "nope", name = "X"))
        assertNull(dao.getById("nope"))
    }

    @Test
    fun `getSubjects maps entities to domain models`() = runTest {
        repo.addSubject(Subject(name = "Quimica", iconIndex = 3, colorIndex = 4))
        val subjects = repo.getSubjects().first()
        assertEquals(1, subjects.size)
        assertEquals("Quimica", subjects[0].name)
        assertEquals(3, subjects[0].iconIndex)
    }
}

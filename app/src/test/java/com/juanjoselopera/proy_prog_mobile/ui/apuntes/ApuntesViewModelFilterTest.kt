package com.juanjoselopera.proy_prog_mobile.ui.apuntes

import com.juanjoselopera.proy_prog_mobile.app.domain.model.ConceptItem
import com.juanjoselopera.proy_prog_mobile.app.domain.model.DeepResearchResult
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.model.QAItem
import com.juanjoselopera.proy_prog_mobile.app.domain.model.ResearchSource
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AiRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.NoteRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.ExtractTextUseCase
import com.juanjoselopera.proy_prog_mobile.app.ui.apuntes.ApuntesViewModel
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import com.juanjoselopera.proy_prog_mobile.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApuntesViewModelFilterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeNotes = MutableStateFlow<List<Note>>(emptyList())

    private val noteRepository = object : NoteRepository {
        override fun getNotes(subjectId: String?): Flow<List<Note>> = fakeNotes
        override suspend fun addNote(note: Note): String = ""
        override suspend fun updateNote(note: Note) = Unit
        override suspend fun deleteNote(noteId: String) = Unit
    }

    private val subjectRepository = object : SubjectRepository {
        override fun getSubjects(): Flow<List<Subject>> = MutableStateFlow(emptyList())
        override suspend fun addSubject(subject: Subject): String = ""
        override suspend fun updateSubject(subject: Subject) = Unit
        override suspend fun deleteSubject(subjectId: String) = Unit
    }

    private val aiRepository = object : AiRepository {
        override suspend fun getQuestions(note: String, model: String) =
            Resource.Success(emptyList<QAItem>())
        override suspend fun getConcepts(note: String, model: String) =
            Resource.Success(emptyList<ConceptItem>())
        override suspend fun getSummary(note: String, model: String) =
            Resource.Success("")
        override suspend fun extractText(imageBytes: ByteArray, mimeType: String, model: String) =
            Resource.Success("")
        override suspend fun deepResearch(model: String, topic: String?, note: String?) =
            Resource.Success(DeepResearchResult("", emptyList<ResearchSource>()))
    }

    private lateinit var viewModel: ApuntesViewModel

    @Before
    fun setup() {
        viewModel = ApuntesViewModel(noteRepository, subjectRepository, ExtractTextUseCase(aiRepository))
    }

    @Test
    fun `filteredNotes returns all notes when query is blank`() = runTest {
        val results = mutableListOf<List<Note>>()
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.filteredNotes.collect { results.add(it) }
        }
        fakeNotes.value = listOf(
            Note(id = "1", title = "Derivadas", subjectId = "s1", subjectName = "Matemáticas"),
            Note(id = "2", title = "Integrales", subjectId = "s1", subjectName = "Matemáticas")
        )
        viewModel.setTitleQuery("")
        advanceUntilIdle()
        assertEquals(2, results.last().size)
    }

    @Test
    fun `filteredNotes filters by title case insensitive`() = runTest {
        val results = mutableListOf<List<Note>>()
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.filteredNotes.collect { results.add(it) }
        }
        fakeNotes.value = listOf(
            Note(id = "1", title = "Derivadas", subjectId = "s1", subjectName = "Matemáticas"),
            Note(id = "2", title = "Integrales", subjectId = "s1", subjectName = "Matemáticas")
        )
        viewModel.setTitleQuery("der")
        advanceUntilIdle()
        assertEquals(1, results.last().size)
        assertEquals("Derivadas", results.last()[0].title)
    }

    @Test
    fun `filteredNotes returns empty when no title matches`() = runTest {
        val results = mutableListOf<List<Note>>()
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.filteredNotes.collect { results.add(it) }
        }
        fakeNotes.value = listOf(
            Note(id = "1", title = "Derivadas", subjectId = "s1", subjectName = "Matemáticas")
        )
        viewModel.setTitleQuery("xyz")
        advanceUntilIdle()
        assertEquals(0, results.last().size)
    }

    @Test
    fun `filteredNotes is case insensitive for uppercase query`() = runTest {
        val results = mutableListOf<List<Note>>()
        backgroundScope.launch(mainDispatcherRule.dispatcher) {
            viewModel.filteredNotes.collect { results.add(it) }
        }
        fakeNotes.value = listOf(
            Note(id = "1", title = "derivadas", subjectId = "s1", subjectName = "Matemáticas")
        )
        viewModel.setTitleQuery("DERIV")
        advanceUntilIdle()
        assertEquals(1, results.last().size)
    }
}

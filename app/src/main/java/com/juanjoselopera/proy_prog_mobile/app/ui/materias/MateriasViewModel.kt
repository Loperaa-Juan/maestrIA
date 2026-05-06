package com.juanjoselopera.proy_prog_mobile.app.ui.materias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.data.local.dao.MateriaDao
import com.juanjoselopera.proy_prog_mobile.app.data.local.entity.MateriaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MateriasViewModel @Inject constructor(
    private val materiaDao: MateriaDao
) : ViewModel() {

    // stateIn convierte el Flow de Room en un StateFlow que el fragment puede observar.
    // WhileSubscribed(5000) detiene la recolección 5 segundos después de que no haya
    // observadores activos, evitando consultas innecesarias a la BD en segundo plano
    val materias: StateFlow<List<MateriaEntity>> = materiaDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedDefaultsIfEmpty()
    }

    fun addMateria(materia: MateriaEntity) {
        viewModelScope.launch { materiaDao.insert(materia) }
    }

    fun deleteMateria(materia: MateriaEntity) {
        viewModelScope.launch { materiaDao.delete(materia) }
    }

    // Solo inserta las materias de ejemplo si la tabla está vacía (primer arranque)
    private fun seedDefaultsIfEmpty() {
        viewModelScope.launch {
            if (materiaDao.count() == 0) {
                materiaDao.insert(MateriaEntity(name = "Cálculo I", iconIndex = 0, colorIndex = 0))
                materiaDao.insert(MateriaEntity(name = "Física General", iconIndex = 0, colorIndex = 1))
            }
        }
    }
}

package com.example.patients.ui.consultations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.repository.ConsultationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsultationViewModel(
    private val patientId: String,
    private val repository: ConsultationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConsultationsUiState>(ConsultationsUiState.Loading)
    val uiState: StateFlow<ConsultationsUiState> = _uiState.asStateFlow()

    init {
        loadConsultations()
    }

    fun loadConsultations() {
        _uiState.value = ConsultationsUiState.Loading
        viewModelScope.launch {
            repository.getConsultations(patientId)
                .onSuccess { _uiState.value = ConsultationsUiState.Success(it) }
                .onFailure { e -> _uiState.value = ConsultationsUiState.Error(e.message ?: "Erreur inconnue") }
        }
    }

    companion object {
        // Ce ViewModel a besoin d'un paramètre (l'id du patient) :
        // la Factory est donc une fonction qui reçoit cet id
        fun factory(patientId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                ConsultationViewModel(patientId, app.container.consultationRepository)
            }
        }
    }
}
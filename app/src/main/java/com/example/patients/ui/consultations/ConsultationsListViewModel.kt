package com.example.patients.ui.consultations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.model.Consultation
import com.example.patients.data.model.ConsultationRequest
import com.example.patients.data.model.Patient
import com.example.patients.data.repository.ConsultationRepository
import com.example.patients.data.repository.PatientRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de la page "Consultations" (toutes les consultations)
class ConsultationsListViewModel(
    private val repository: ConsultationRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    // ÉTAT de l'écran
    private val _uiState = MutableStateFlow<ConsultationsUiState>(ConsultationsUiState.Loading)
    val uiState: StateFlow<ConsultationsUiState> = _uiState.asStateFlow()

    // ÉVÉNEMENTS ponctuels (Snackbar)
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    // Filtre : null = Toutes, "En attente", "Terminée"
    private val _filtre = MutableStateFlow<String?>(null)
    val filtre: StateFlow<String?> = _filtre.asStateFlow()

    // Liste des patients pour choisir le patient dans le formulaire
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadConsultations()
        loadPatients()
    }

    fun loadConsultations() {
        loadJob?.cancel()
        _uiState.value = ConsultationsUiState.Loading
        loadJob = viewModelScope.launch {
            repository.getConsultations(statut = _filtre.value)
                .onSuccess { _uiState.value = ConsultationsUiState.Success(it) }
                .onFailure { e -> _uiState.value = ConsultationsUiState.Error(e.message ?: "Erreur inconnue") }
        }
    }

    fun loadPatients() {
        viewModelScope.launch {
            patientRepository.getPatients()
                .onSuccess { _patients.value = it }
        }
    }

    fun setFiltre(statut: String?) {
        if (statut == _filtre.value) return
        _filtre.value = statut
        loadConsultations()
    }

    fun addConsultation(request: ConsultationRequest) {
        viewModelScope.launch {
            repository.createConsultation(request)
                .onSuccess { consultation ->
                    updateList { it + consultation }
                    _messages.emit("Consultation ajoutée")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Ajout impossible") }
        }
    }

    fun updateConsultation(id: String, request: ConsultationRequest) {
        viewModelScope.launch {
            repository.updateConsultation(id, request)
                .onSuccess { consultation ->
                    updateList { list -> list.map { if (it.id == consultation.id) consultation else it } }
                    _messages.emit("Consultation modifiée")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Modification impossible") }
        }
    }

    fun deleteConsultation(consultation: Consultation) {
        viewModelScope.launch {
            repository.deleteConsultation(consultation.id)
                .onSuccess {
                    updateList { list -> list.filterNot { it.id == consultation.id } }
                    _messages.emit("Consultation supprimée")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Suppression impossible") }
        }
    }

    // Modifie la liste affichée, garde seulement celles du filtre, triées par date (récentes en haut)
    private fun updateList(transform: (List<Consultation>) -> List<Consultation>) {
        val f = _filtre.value
        _uiState.update { state ->
            if (state is ConsultationsUiState.Success) {
                state.copy(
                    consultations = transform(state.consultations)
                        .filter { f == null || it.statut == f }
                        .sortedByDescending { it.date }
                )
            } else state
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                ConsultationsListViewModel(
                    app.container.consultationRepository,
                    app.container.patientRepository
                )
            }
        }
    }
}
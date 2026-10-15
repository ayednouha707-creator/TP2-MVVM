package com.example.patients.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
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

class PatientViewModel(
    private val repository: PatientRepository
) : ViewModel() {

    // ÉTAT de l'écran : modifiable ici, lecture seule pour l'UI
    private val _uiState = MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)
    val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

    // ÉVÉNEMENTS ponctuels à afficher une seule fois (Snackbar) — Partie 8.2
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    // Filtre choisi : null = Tous, "Actif", "Inactif" — Exercice 2
    private val _filtre = MutableStateFlow<String?>(null)
    val filtre: StateFlow<String?> = _filtre.asStateFlow()

    // Chargement en cours (pour l'annuler si on change de filtre rapidement)
    private var loadJob: Job? = null

    init {
        loadPatients()
    }

    fun loadPatients() {
        loadJob?.cancel()
        _uiState.value = PatientsUiState.Loading
        loadJob = viewModelScope.launch {
            repository.getPatients(_filtre.value)
                .onSuccess { patients -> _uiState.value = PatientsUiState.Success(patients) }
                .onFailure { e -> _uiState.value = PatientsUiState.Error(e.message ?: "Erreur inconnue") }
        }
    }

    // Exercice 2 : changer de filtre relance le chargement avec ?statut=...
    fun setFiltre(statut: String?) {
        if (statut == _filtre.value) return
        _filtre.value = statut
        loadPatients()
    }

    // Partie 8 : POST
    fun addPatient(request: PatientRequest) {
        viewModelScope.launch {
            repository.createPatient(request)
                .onSuccess { patient ->
                    // On n'ajoute à la liste affichée que si le patient correspond au filtre
                    val filtreActuel = _filtre.value
                    if (filtreActuel == null || filtreActuel == patient.statut) {
                        updateList { it + patient }
                    }
                    _messages.emit("${patient.prenom} ${patient.nom} ajouté")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Ajout impossible") }
        }
    }

    // Exercice 1 : PUT
    fun updatePatient(id: String, request: PatientRequest) {
        viewModelScope.launch {
            repository.updatePatient(id, request)
                .onSuccess { patient ->
                    updateList { list -> list.map { if (it.id == patient.id) patient else it } }
                    _messages.emit("${patient.prenom} ${patient.nom} modifié")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Modification impossible") }
        }
    }

    // Partie 9 : DELETE
    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient.id)
                .onSuccess {
                    updateList { list -> list.filterNot { it.id == patient.id } }
                    _messages.emit("${patient.prenom} ${patient.nom} supprimé")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Suppression impossible") }
        }
    }

    // Modifie la liste affichée sans recharger depuis le serveur
    private fun updateList(transform: (List<Patient>) -> List<Patient>) {
        _uiState.update { state ->
            if (state is PatientsUiState.Success) state.copy(patients = transform(state.patients))
            else state
        }
    }

    // Indique à Android comment construire ce ViewModel (il a besoin du repository)
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                PatientViewModel(app.container.patientRepository)
            }
        }
    }
}
package com.example.patients.ui.consultations

import com.example.patients.data.model.Consultation

// Son propre état scellé, indépendant de celui de l'écran Patients
sealed interface ConsultationsUiState {
    data object Loading : ConsultationsUiState
    data class Success(val consultations: List<Consultation>) : ConsultationsUiState
    data class Error(val message: String) : ConsultationsUiState
}
package com.example.patients.data.repository

import com.example.patients.data.model.Consultation
import com.example.patients.data.remote.ConsultationApi
import kotlinx.serialization.json.Json

class ConsultationRepository(
    private val api: ConsultationApi,
    private val json: Json
) {

    // Consultations d'un patient, triées par date décroissante (tri fait par le serveur)
    suspend fun getConsultations(patientId: String): Result<List<Consultation>> =
        safeCall(json) { api.getConsultations(patientId = patientId) }
}
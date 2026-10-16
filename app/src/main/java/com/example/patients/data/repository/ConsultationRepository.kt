package com.example.patients.data.repository

import com.example.patients.data.model.Consultation
import com.example.patients.data.model.ConsultationRequest
import com.example.patients.data.remote.ConsultationApi
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class ConsultationRepository(
    private val api: ConsultationApi,
    private val json: Json
) {

    // Toutes les consultations, ou celles d'un patient, ou filtrées par statut
    suspend fun getConsultations(
        patientId: String? = null,
        statut: String? = null
    ): Result<List<Consultation>> =
        safeCall(json) { api.getConsultations(patientId, statut) }

    suspend fun createConsultation(request: ConsultationRequest): Result<Consultation> =
        safeCall(json) { api.createConsultation(request) }

    suspend fun updateConsultation(id: String, request: ConsultationRequest): Result<Consultation> =
        safeCall(json) { api.updateConsultation(id, request) }

    suspend fun deleteConsultation(id: String): Result<Unit> = safeCall(json) {
        val response = api.deleteConsultation(id)
        if (!response.isSuccessful) throw HttpException(response)
    }
}
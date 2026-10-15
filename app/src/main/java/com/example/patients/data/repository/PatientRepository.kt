package com.example.patients.data.repository

import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.remote.PatientApi
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class PatientRepository(
    private val api: PatientApi,
    private val json: Json
) {

    // GET /patients  (ou /patients?statut=Actif si statut != null)  — Exercice 2
    suspend fun getPatients(statut: String? = null): Result<List<Patient>> =
        safeCall(json) { api.getPatients(statut) }

    // POST /patients  — Partie 8
    suspend fun createPatient(request: PatientRequest): Result<Patient> =
        safeCall(json) { api.createPatient(request) }

    // PUT /patients/{id}  — Exercice 1
    suspend fun updatePatient(id: String, request: PatientRequest): Result<Patient> =
        safeCall(json) { api.updatePatient(id, request) }

    // DELETE /patients/{id}  — Partie 9
    // Response<Unit> ne lève pas d'exception sur 4xx/5xx : on vérifie nous-mêmes
    suspend fun deletePatient(id: String): Result<Unit> = safeCall(json) {
        val response = api.deletePatient(id)
        if (!response.isSuccessful) throw HttpException(response)
    }
}
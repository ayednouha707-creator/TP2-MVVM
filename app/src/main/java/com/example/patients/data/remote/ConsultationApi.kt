package com.example.patients.data.remote

import com.example.patients.data.model.Consultation
import retrofit2.http.GET
import retrofit2.http.Query

interface ConsultationApi {

    // GET /api/consultations?patient=<id>&statut=...  (paramètres omis s'ils sont null)
    @GET("consultations")
    suspend fun getConsultations(
        @Query("patient") patientId: String? = null,
        @Query("statut") statut: String? = null
    ): List<Consultation>
}
package com.example.patients.data.model

import kotlinx.serialization.Serializable

// Le patient tel que renvoyé par .populate("patient", "nom prenom") côté Node
@Serializable
data class PatientResume(
    val id: String,
    val nom: String,
    val prenom: String
)

// Consultation telle que renvoyée par l'API
@Serializable
data class Consultation(
    val id: String,
    val patient: PatientResume? = null, // null si le patient a été supprimé
    val date: String,                   // "2026-10-05T09:30:00.000Z"
    val motif: String,
    val traitement: String? = null,
    val medecin: String,
    val notes: String? = null,
    val statut: String = "En attente"
)

// Données envoyées pour créer ou modifier une consultation (POST / PUT)
// statut sans valeur par défaut : il est toujours envoyé (sinon "En attente" serait omis)
@Serializable
data class ConsultationRequest(
    val patient: String,   // id du patient
    val date: String,      // "2026-10-16"
    val motif: String,
    val traitement: String? = null,
    val medecin: String,
    val notes: String? = null,
    val statut: String
)
package com.example.patients.data.model

import kotlinx.serialization.Serializable

// Le patient tel que renvoyé par .populate("patient", "nom prenom") côté Node
@Serializable
data class PatientResume(
    val id: String,
    val nom: String,
    val prenom: String
)

// Consultation telle que renvoyée par GET /api/consultations (TP 1, exercice)
@Serializable
data class Consultation(
    val id: String,
    val patient: PatientResume? = null, // null si le patient a été supprimé entre-temps
    val date: String,                   // date ISO : "2026-10-05T09:30:00.000Z"
    val motif: String,
    val traitement: String? = null,
    val medecin: String,
    val notes: String? = null,
    val statut: String = "En attente"
)
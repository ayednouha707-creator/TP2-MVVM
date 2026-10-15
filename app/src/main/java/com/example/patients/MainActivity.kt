package com.example.patients

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.patients.ui.consultations.ConsultationsScreen
import com.example.patients.ui.patients.PatientsScreen
import com.example.patients.ui.theme.PatientsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PatientsTheme { // nom généré par Android Studio
                // Navigation simple entre 2 écrans, sans bibliothèque :
                // null = liste des patients, sinon = consultations de ce patient
                var patientId by rememberSaveable { mutableStateOf<String?>(null) }
                var patientNom by rememberSaveable { mutableStateOf("") }

                val id = patientId
                if (id == null) {
                    PatientsScreen(
                        onOpenConsultations = { patient ->
                            patientId = patient.id
                            patientNom = "${patient.prenom} ${patient.nom}"
                        }
                    )
                } else {
                    // Bouton retour du téléphone -> revient à la liste au lieu de quitter l'appli
                    BackHandler { patientId = null }
                    ConsultationsScreen(
                        patientId = id,
                        patientNom = patientNom,
                        onBack = { patientId = null }
                    )
                }
            }
        }
    }
}
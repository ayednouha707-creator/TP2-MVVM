package com.example.patients

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.patients.ui.consultations.ConsultationsListScreen
import com.example.patients.ui.consultations.ConsultationsScreen
import com.example.patients.ui.patients.PatientsScreen
import com.example.patients.ui.theme.PatientsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PatientsTheme { // nom généré par Android Studio
                AppPatients()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppPatients() {
    // 0 = page Patients, 1 = page Consultations
    var onglet by rememberSaveable { mutableStateOf(0) }
    // Consultations d'un patient précis (ouvert avec l'icône 📅 d'une carte patient)
    var patientId by rememberSaveable { mutableStateOf<String?>(null) }
    var patientNom by rememberSaveable { mutableStateOf("") }

    val id = patientId
    if (id != null) {
        BackHandler { patientId = null }
        ConsultationsScreen(
            patientId = id,
            patientNom = patientNom,
            onBack = { patientId = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // la barre du bas gère déjà la zone de navigation du téléphone
                    .consumeWindowInsets(WindowInsets.navigationBars)
            ) {
                when (onglet) {
                    0 -> PatientsScreen(
                        onOpenConsultations = { patient ->
                            patientId = patient.id
                            patientNom = "${patient.prenom} ${patient.nom}"
                        }
                    )
                    else -> ConsultationsListScreen()
                }
            }
            NavigationBar {
                NavigationBarItem(
                    selected = onglet == 0,
                    onClick = { onglet = 0 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Patients") }
                )
                NavigationBarItem(
                    selected = onglet == 1,
                    onClick = { onglet = 1 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Consultations") }
                )
            }
        }
    }
}
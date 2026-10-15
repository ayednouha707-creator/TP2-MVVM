package com.example.patients.ui.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    onOpenConsultations: (Patient) -> Unit = {}, // Exercice 3 : ouvrir l'écran Consultations
    viewModel: PatientViewModel = viewModel(factory = PatientViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filtre by viewModel.filtre.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    // id du patient en cours de modification (un String est "saveable", un Patient non)
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }

    // Affiche chaque message envoyé par le ViewModel
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients") },
                actions = {
                    IconButton(onClick = { viewModel.loadPatients() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Exercice 2 : filtres au-dessus de la liste
            StatutFilter(selected = filtre, onSelect = { viewModel.setFiltre(it) })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (val state = uiState) {
                    is PatientsUiState.Loading ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    is PatientsUiState.Error ->
                        ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.loadPatients() },
                            modifier = Modifier.align(Alignment.Center)
                        )

                    is PatientsUiState.Success ->
                        PatientList(
                            patients = state.patients,
                            onEdit = { editingId = it.id },
                            onOpenConsultations = onOpenConsultations,
                            onDelete = { viewModel.deletePatient(it) }
                        )
                }
            }
        }
    }

    // Partie 8 : ajout
    if (showAddDialog) {
        PatientDialog(
            patient = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.addPatient(request)
                showAddDialog = false
            }
        )
    }

    // Exercice 1 : modification (même boîte de dialogue, pré-remplie)
    val editingPatient = (uiState as? PatientsUiState.Success)
        ?.patients
        ?.find { it.id == editingId }

    if (editingPatient != null) {
        PatientDialog(
            patient = editingPatient,
            onDismiss = { editingId = null },
            onConfirm = { request ->
                viewModel.updatePatient(editingPatient.id, request)
                editingId = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatutFilter(selected: String?, onSelect: (String?) -> Unit) {
    // libellé affiché -> valeur envoyée à l'API (null = pas de filtre)
    val options = listOf("Tous" to null, "Actif" to "Actif", "Inactif" to "Inactif")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (label, value) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun PatientList(
    patients: List<Patient>,
    onEdit: (Patient) -> Unit,
    onOpenConsultations: (Patient) -> Unit,
    onDelete: (Patient) -> Unit
) {
    if (patients.isEmpty()) {
        Text("Aucun patient", modifier = Modifier.padding(24.dp))
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(patients, key = { it.id }) { patient ->
                PatientCard(
                    patient = patient,
                    onClick = { onEdit(patient) },
                    onConsultations = { onOpenConsultations(patient) },
                    onDelete = { onDelete(patient) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onConsultations: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${patient.prenom} ${patient.nom}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = listOfNotNull(
                        patient.age?.let { "$it ans" },
                        patient.medecin,
                        patient.statut
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onConsultations) {
                Icon(Icons.Default.DateRange, contentDescription = "Consultations")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
            }
        }
    }
}

// patient == null  -> ajout ("Nouveau patient")
// patient != null  -> modification, champs pré-remplis ("Modifier le patient")
@Composable
fun PatientDialog(
    patient: Patient?,
    onDismiss: () -> Unit,
    onConfirm: (PatientRequest) -> Unit
) {
    var nom by rememberSaveable { mutableStateOf(patient?.nom ?: "") }
    var prenom by rememberSaveable { mutableStateOf(patient?.prenom ?: "") }
    var age by rememberSaveable { mutableStateOf(patient?.age?.toString() ?: "") }
    var medecin by rememberSaveable { mutableStateOf(patient?.medecin ?: "") }

    val formulaireValide = nom.isNotBlank() && prenom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (patient == null) "Nouveau patient" else "Modifier le patient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom *") }, singleLine = true
                )
                OutlinedTextField(
                    value = prenom, onValueChange = { prenom = it },
                    label = { Text("Prénom *") }, singleLine = true
                )
                OutlinedTextField(
                    value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Âge") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = medecin, onValueChange = { medecin = it },
                    label = { Text("Médecin") }, singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulaireValide,
                onClick = {
                    onConfirm(
                        PatientRequest(
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            age = age.toIntOrNull(),
                            telephone = patient?.telephone, // pas de champ : on garde l'existant
                            medecin = medecin.trim().ifBlank { null }
                        )
                    )
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
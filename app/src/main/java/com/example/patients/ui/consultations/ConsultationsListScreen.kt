package com.example.patients.ui.consultations

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.data.model.Consultation
import com.example.patients.data.model.ConsultationRequest
import com.example.patients.data.model.Patient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationsListScreen(
    viewModel: ConsultationsListViewModel = viewModel(factory = ConsultationsListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filtre by viewModel.filtre.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultations") },
                actions = {
                    IconButton(onClick = { viewModel.loadConsultations() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.loadPatients() // liste des patients à jour pour le formulaire
                showAddDialog = true
            }) {
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
            ConsultationStatutFilter(selected = filtre, onSelect = { viewModel.setFiltre(it) })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (val state = uiState) {
                    is ConsultationsUiState.Loading ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    is ConsultationsUiState.Error ->
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.message, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadConsultations() }) { Text("Réessayer") }
                        }

                    is ConsultationsUiState.Success ->
                        if (state.consultations.isEmpty()) {
                            Text("Aucune consultation", modifier = Modifier.padding(24.dp))
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.consultations, key = { it.id }) { consultation ->
                                    ConsultationItem(
                                        consultation = consultation,
                                        onClick = {
                                            viewModel.loadPatients()
                                            editingId = consultation.id
                                        },
                                        onDelete = { viewModel.deleteConsultation(consultation) }
                                    )
                                }
                            }
                        }
                }
            }
        }
    }

    // Ajout
    if (showAddDialog) {
        ConsultationDialog(
            consultation = null,
            patients = patients,
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.addConsultation(request)
                showAddDialog = false
            }
        )
    }

    // Modification (même boîte de dialogue, pré-remplie)
    val editing = (uiState as? ConsultationsUiState.Success)
        ?.consultations
        ?.find { it.id == editingId }

    if (editing != null) {
        ConsultationDialog(
            consultation = editing,
            patients = patients,
            onDismiss = { editingId = null },
            onConfirm = { request ->
                viewModel.updateConsultation(editing.id, request)
                editingId = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationStatutFilter(selected: String?, onSelect: (String?) -> Unit) {
    val options = listOf("Toutes" to null, "En attente" to "En attente", "Terminée" to "Terminée")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationItem(consultation: Consultation, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = consultation.patient?.let { "${it.prenom} ${it.nom}" } ?: "Patient supprimé",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${consultation.date.take(10)} · ${consultation.motif}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${consultation.medecin} · ${consultation.statut}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
            }
        }
    }
}

// consultation == null -> ajout ; sinon modification avec champs pré-remplis
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationDialog(
    consultation: Consultation?,
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onConfirm: (ConsultationRequest) -> Unit
) {
    var patientId by rememberSaveable { mutableStateOf(consultation?.patient?.id) }
    var date by rememberSaveable { mutableStateOf(consultation?.date?.take(10) ?: "") }
    var motif by rememberSaveable { mutableStateOf(consultation?.motif ?: "") }
    var medecin by rememberSaveable { mutableStateOf(consultation?.medecin ?: "") }
    var traitement by rememberSaveable { mutableStateOf(consultation?.traitement ?: "") }
    var notes by rememberSaveable { mutableStateOf(consultation?.notes ?: "") }
    var statut by rememberSaveable { mutableStateOf(consultation?.statut ?: "En attente") }
    var menuOuvert by remember { mutableStateOf(false) }

    val dateValide = Regex("""\d{4}-\d{2}-\d{2}""").matches(date)
    val formulaireValide = patientId != null && dateValide && motif.isNotBlank() && medecin.isNotBlank()

    val nomPatient = patients.find { it.id == patientId }?.let { "${it.prenom} ${it.nom}" }
        ?: consultation?.patient?.let { "${it.prenom} ${it.nom}" }
        ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (consultation == null) "Nouvelle consultation" else "Modifier la consultation") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Choix du patient dans une liste déroulante
                Box {
                    OutlinedTextField(
                        value = nomPatient,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Patient *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Calque transparent qui capte le clic (un champ readOnly ne réagit pas seul)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { menuOuvert = true }
                    )
                    DropdownMenu(expanded = menuOuvert, onDismissRequest = { menuOuvert = false }) {
                        patients.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.prenom} ${p.nom}") },
                                onClick = {
                                    patientId = p.id
                                    menuOuvert = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = date, onValueChange = { date = it },
                    label = { Text("Date * (AAAA-MM-JJ)") }, singleLine = true,
                    isError = date.isNotEmpty() && !dateValide
                )
                OutlinedTextField(
                    value = motif, onValueChange = { motif = it },
                    label = { Text("Motif *") }, singleLine = true
                )
                OutlinedTextField(
                    value = medecin, onValueChange = { medecin = it },
                    label = { Text("Médecin *") }, singleLine = true
                )
                OutlinedTextField(
                    value = traitement, onValueChange = { traitement = it },
                    label = { Text("Traitement") }, singleLine = true
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") }
                )
                Text("Statut", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("En attente", "Terminée").forEach { s ->
                        FilterChip(
                            selected = statut == s,
                            onClick = { statut = s },
                            label = { Text(s) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulaireValide,
                onClick = {
                    patientId?.let { id ->
                        onConfirm(
                            ConsultationRequest(
                                patient = id,
                                date = date,
                                motif = motif.trim(),
                                traitement = traitement.trim().ifBlank { null },
                                medecin = medecin.trim(),
                                notes = notes.trim().ifBlank { null },
                                statut = statut
                            )
                        )
                    }
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
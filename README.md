# Patients — Android client (MVVM)

An Android app to manage patients and their medical consultations, connected to my own REST API.

**Stack:** Kotlin · Jetpack Compose · MVVM (ViewModel + UI state) · Retrofit · repository pattern with a simple DI container

Back end: [tp-node-mongo](https://github.com/ayednouha707-creator/tp-node-mongo) (Node.js, Express, MongoDB/Mongoose, with Patient, Consultation and Measurement models).

## Features

- List, add, edit and delete patients
- Consultations per patient, with a list screen and a detail screen
- Loading / error / success states handled in the UI (`SafeCall` wrapper around network calls)

## Structure

```
data/model        Patient, Consultation
data/remote       Retrofit APIs
data/repository   PatientRepository, ConsultationRepository
di/AppContainer   dependency container
ui/patients       screen + ViewModel + UI state
ui/consultations  list and detail screens + ViewModels
```

## Run it

1. Start the back end (`npm install && npm run dev` in tp-node-mongo, MongoDB running locally).
2. Run the app on the Android emulator. It calls `http://10.0.2.2:5000/api/`, which is the host machine's localhost seen from the emulator.

## Author

Nouha Ayed — ISET Bizerte

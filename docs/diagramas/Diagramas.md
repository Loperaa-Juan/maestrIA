# D01 - Diagrama de Casos de Uso

```mermaid
flowchart LR

guest[Usuario No Autenticado]
user[Usuario Autenticado]

firebase[Firebase Auth]
firestore[Firebase Firestore]
ai[API IA]
crash[Crashlytics]

guest --> UC1[Registrarse]
guest --> UC2[Iniciar Sesión]
guest --> UC3[Recuperar Contraseña]

user --> UC4[Ver Materias]
user --> UC5[Crear Materia]
user --> UC6[Editar Materia]
user --> UC7[Eliminar Materia]

user --> UC8[Ver Apuntes]
user --> UC9[Crear Apunte]
user --> UC10[Editar Apunte]
user --> UC11[Eliminar Apunte]

user --> UC12[Generar Resumen]
user --> UC13[Generar Preguntas]
user --> UC14[Extraer Texto]
user --> UC15[Investigación Profunda]
user --> UC16[Obtener Conceptos]

user --> UC17[Ver Perfil]
user --> UC18[Cerrar Sesión]

UC1 --> firebase
UC2 --> firebase
UC3 --> firebase

UC4 --> firestore
UC5 --> firestore
UC6 --> firestore
UC7 --> firestore

UC8 --> firestore
UC9 --> firestore
UC10 --> firestore
UC11 --> firestore

UC12 --> ai
UC13 --> ai
UC14 --> ai
UC15 --> ai
UC16 --> ai

ai --> crash
firebase --> crash
firestore --> crash
```

# D02 - Flujo de Navegación

```mermaid
flowchart TD

A[PresentationActivity] --> B[LandingFragment]

B --> C[LoginFragment]
B --> D[SignUpFragment]

C --> E{Login correcto?}

E -->|Sí| F[MenuFragment]
E -->|No| G[Error de autenticación]

C --> H[ForgotPasswordFragment]

D --> I[EmailVerificationFragment]
I --> F

F --> J[LandingFragment]
F --> K[MateriasFragment]
F --> L[ApuntesFragment]
F --> M[AIFragment]
F --> N[UserProfileActivity]

K --> O[Detalle Materia]

L --> P[NoteDetailFragment]
L --> Q[ExtractTextFragment]

M --> R[SummaryResultFragment]
M --> S[QuestionsResultFragment]
M --> T[ConceptsResultFragment]
M --> U[DeepResearchFragment]
M --> V[NotePickerFragment]

N --> W[Cerrar sesión]

W --> C

G --> C
```

# D03 - Arquitectura Móvil (MVVM)

```mermaid
flowchart TD

subgraph UI
A[Fragments / Activities]
A1[LoginFragment]
A2[MateriasFragment]
A3[ApuntesFragment]
A4[AIFragment]
A5[Profile]
end

subgraph ViewModel
B1[LoginViewModel]
B2[MateriasViewModel]
B3[ApuntesViewModel]
B4[DeepResearchViewModel]
B5[UserProfileViewModel]
end

subgraph Repository
C1[AuthRepository]
C2[SubjectRepository]
C3[NoteRepository]
C4[AiRepository]
end

subgraph Local
D1[Room Database]
D2[UserDao]
D3[MateriaDao]
D4[UserEntity]
D5[MateriaEntity]
end

subgraph Remote
E1[Firebase Auth]
E2[Firestore]
E3[AI API]
end

subgraph Monitoring
F1[Crashlytics]
end

A --> B1
A --> B2
A --> B3
A --> B4
A --> B5

B1 --> C1
B2 --> C2
B3 --> C3
B4 --> C4

C2 --> D1
C3 --> D1

C1 --> E1
C2 --> E2
C3 --> E2
C4 --> E3

E1 --> F1
E2 --> F1
E3 --> F1
```
## D04 — Diagrama de Modelo de Datos Local y Remoto

```mermaid
erDiagram
    %% ── ROOM (LOCAL) ─────────────────────────────────────
    USERS_LOCAL {
        TEXT uid PK "UID de Firebase Auth"
        TEXT email "Correo electrónico"
        TEXT displayName "Nombre del usuario (nullable)"
    }

    MATERIAS_LOCAL {
        INTEGER id PK "AUTOINCREMENT"
        TEXT name "Nombre de la materia"
        INTEGER iconIndex "Índice del ícono"
        INTEGER colorIndex "Índice del color"
    }

    %% ── FIRESTORE (REMOTO) ───────────────────────────────
    FIRESTORE_SUBJECTS {
        STRING id PK "ID generado por Firestore"
        STRING name "Nombre de la materia"
        NUMBER iconIndex "Índice del ícono"
        NUMBER colorIndex "Índice del color"
    }

    FIRESTORE_NOTES {
        STRING id PK "ID generado por Firestore"
        STRING title "Título del apunte"
        STRING content "Cuerpo en Markdown"
        STRING subjectId FK "Referencia a subjects"
        STRING subjectName "Nombre desnormalizado"
        ARRAY tags "Etiquetas"
        NUMBER createdAt "Timestamp epoch ms"
        STRING imageUri "URI imagen OCR (nullable)"
    }

    %% ── RELACIONES ───────────────────────────────────────
    USERS_LOCAL ||--o{ MATERIAS_LOCAL : "cachea (por sesión)"
    FIRESTORE_SUBJECTS ||--o{ FIRESTORE_NOTES : "subjectId →"
```

> **Nota de ubicación:**
> - `USERS_LOCAL` y `MATERIAS_LOCAL` → Room (SQLite en el dispositivo)
> - `FIRESTORE_SUBJECTS` → `Firestore: users/{uid}/subjects/{subjectId}`
> - `FIRESTORE_NOTES` → `Firestore: users/{uid}/notes/{noteId}`
> - Los apuntes **no** se cachean en Room; siempre se leen desde Firestore.

---

## D05 — Diagrama de Sincronización Local-Remoto

```mermaid
sequenceDiagram
    actor U as Usuario
    participant App as App (UI + VM)
    participant Repo as Repository
    participant Room as Room (local)
    participant FS as Cloud Firestore

    Note over App,FS: ── LECTURA (al abrir pantalla) ──

    U->>App: Abre MateriasFragment
    App->>Repo: getSubjects()
    Repo->>Room: consulta caché local
    Room-->>App: lista materias (si existe)
    App-->>U: muestra datos en caché

    Repo->>FS: addSnapshotListener (tiempo real)
    FS-->>Repo: snapshot con materias actualizadas
    Repo->>Room: actualiza caché local
    Repo-->>App: emite nueva lista por StateFlow
    App-->>U: refresca UI automáticamente

    Note over App,FS: ── ESCRITURA (crear materia) ──

    U->>App: Rellena diálogo y confirma
    App->>Repo: saveSubject(subject)
    Repo->>FS: add(subjectDto)
    FS-->>Repo: OK (docRef id generado)
    Repo->>Room: insert(materiaEntity)
    Repo-->>App: éxito → StateFlow actualizado
    App-->>U: materia aparece en lista

    Note over App,FS: ── ERROR DE RED ──

    U->>App: Intenta crear materia sin internet
    App->>Repo: saveSubject(subject)
    Repo->>FS: add(subjectDto)
    FS-->>Repo: ❌ FirebaseNetworkException
    Repo-->>App: Result.Failure(error)
    App-->>U: Snackbar "Sin conexión a internet"
    Note over App: Room no se actualiza\nEstado de UI no cambia

    Note over App,FS: ── ESTADO OFFLINE ──

    Note over App,FS: Si el usuario solo navega sin crear/editar,\nlos datos en caché de Room se muestran\nsin intentar llamadas a Firestore activas.
```

---

## D06 — Diagramas de Secuencia

### D06.1 — Login / Recuperación de sesión

```mermaid
sequenceDiagram
    actor U as Usuario
    participant LF as LoginFragment
    participant LVM as LoginViewModel
    participant UC as FirebaseLoginUseCase
    participant Repo as FirebaseAuthRepositoryImpl
    participant FB as Firebase Authentication
    participant SM as SessionManager (Room)
    participant NAV as NavController

    U->>LF: Ingresa correo + contraseña → toca "Ingresar"
    LF->>LVM: login(email, password)
    LVM->>LVM: uiState = Loading
    LF-->>U: muestra ProgressBar

    LVM->>UC: invoke(email, password)
    UC->>Repo: login(email, password)
    Repo->>FB: signInWithEmailAndPassword(email, password)

    alt Credenciales correctas
        FB-->>Repo: FirebaseUser (uid, email)
        Repo-->>UC: Result.Success(user)
        UC-->>LVM: Result.Success(user)
        LVM->>SM: saveUser(userEntity) en Room
        LVM->>LVM: uiState = Success
        LVM-->>LF: StateFlow emite Success
        LF->>NAV: navigate(action_login_to_landing)\npopUpTo(authFragment, inclusive=true)
        NAV-->>U: muestra LandingFragment
    else Credenciales incorrectas
        FB-->>Repo: FirebaseAuthException (wrong-password)
        Repo-->>UC: Result.Failure(exception)
        UC-->>LVM: Result.Failure
        LVM->>LVM: uiState = Error(mensaje)
        LVM-->>LF: StateFlow emite Error
        LF-->>U: Snackbar "Contraseña incorrecta"
    else Sin conexión
        FB-->>Repo: FirebaseNetworkException
        Repo-->>UC: Result.Failure
        LVM-->>LF: StateFlow emite Error
        LF-->>U: Snackbar "Sin conexión a internet"
    end

    Note over SM,NAV: Si la app se reabre con sesión activa,\nSessionManager detecta el UID guardado\ny MainActivity navega directamente a Landing\nsin pasar por LoginFragment.
```

---

### D06.2 — Crear apunte con persistencia en Firestore

```mermaid
sequenceDiagram
    actor U as Usuario
    participant AF as ApuntesFragment
    participant DIAL as DialogCrearApunte
    participant AVM as ApuntesViewModel
    participant Repo as NoteRepositoryImpl
    participant DS as NoteRemoteDataSource
    participant FS as Cloud Firestore

    U->>AF: Toca FAB "+"
    AF->>DIAL: muestra dialog_create_note
    U->>DIAL: Rellena título + contenido + materia
    U->>DIAL: Toca "Guardar"

    DIAL->>AVM: saveNote(title, content, subjectId, subjectName)
    AVM->>AVM: uiState = Loading

    alt Campos válidos
        AVM->>Repo: saveNote(note)
        Repo->>DS: save(noteDto, uid)
        DS->>FS: collection("users/{uid}/notes").add(noteDto)
        FS-->>DS: DocumentReference (noteId generado)
        DS-->>Repo: Result.Success(noteId)
        Repo-->>AVM: Result.Success
        AVM->>AVM: uiState = Success
        AVM-->>AF: StateFlow actualiza lista de apuntes
        AF-->>U: Dialog se cierra\nApunte aparece en la lista

    else Título vacío
        AVM-->>AF: validación falla antes de llamar Repo
        AF-->>U: campo título marcado en rojo\n"El título no puede estar vacío"

    else Error de red
        FS-->>DS: FirebaseNetworkException
        DS-->>Repo: Result.Failure
        Repo-->>AVM: Result.Failure
        AVM->>AVM: uiState = Error
        AVM-->>AF: StateFlow emite Error
        AF-->>U: Snackbar "Error al guardar el apunte"
    end
```

---

### D06.3 — Error de red y reporte a Crashlytics

```mermaid
sequenceDiagram
    actor U as Usuario
    participant FRAG as AIFragment / NotePickerFragment
    participant VM as SummaryViewModel
    participant UC as GetSummaryUseCase
    participant Repo as AiRepositoryImpl
    participant HTTP as AiApiService (Retrofit + OkHttp)
    participant API as API REST de IA
    participant CR as Firebase Crashlytics

    U->>FRAG: Selecciona apunte → toca "Generar resumen"
    FRAG->>VM: getSummary(noteContent)
    VM->>VM: uiState = Loading
    FRAG-->>U: muestra ProgressBar

    VM->>UC: invoke(noteContent)
    UC->>Repo: getSummary(text)
    Repo->>HTTP: POST /api/summary {text}
    HTTP->>API: HTTP Request

    alt API responde OK
        API-->>HTTP: 200 OK {summary: "..."}
        HTTP-->>Repo: SummaryDto
        Repo-->>UC: Result.Success(summary)
        UC-->>VM: Result.Success
        VM->>VM: uiState = Success(summary)
        VM-->>FRAG: StateFlow emite resumen
        FRAG-->>U: muestra resumen renderizado en Markdown

    else Timeout / Sin conexión
        HTTP-->>Repo: SocketTimeoutException / IOException
        Repo->>CR: recordException(exception)\n+ log("getSummary failed")
        Repo-->>UC: Result.Failure("Sin conexión")
        UC-->>VM: Result.Failure
        VM->>VM: uiState = Error
        VM-->>FRAG: StateFlow emite Error
        FRAG-->>U: Snackbar "Error de red\nVerifica tu conexión"

    else API devuelve error 4xx / 5xx
        API-->>HTTP: 500 Internal Server Error
        HTTP-->>Repo: HttpException(code=500)
        Repo->>CR: recordException(httpException)\n+ log("AI API error 500")
        Repo-->>UC: Result.Failure("Error del servidor")
        UC-->>VM: Result.Failure
        VM-->>FRAG: StateFlow emite Error
        FRAG-->>U: Snackbar "Error del servidor de IA"

    else Token inválido / 401
        API-->>HTTP: 401 Unauthorized
        HTTP-->>Repo: HttpException(code=401)
        Repo->>CR: recordException(authException)
        Repo-->>VM: Result.Failure("No autorizado")
        VM-->>FRAG: StateFlow emite Error
        FRAG-->>U: Snackbar "Error de autenticación con la IA"
    end

    Note over CR: Crashlytics agrupa automáticamente\ntodos los errores por tipo y frecuencia\nvisibles en Firebase Console.
```

---

## D07 — Diagrama de Estructura de Carpetas

```
maestrIA/
│
├── app/                                ← Módulo principal de la aplicación Android
│   └── src/main/
│       ├── AndroidManifest.xml         ← Permisos, activities, FileProvider
│       └── java/.../proy_prog_mobile/
│           └── app/
│               │
│               ├── MaestrIAApplication.kt   ← Punto de entrada de Hilt (@HiltAndroidApp)
│               ├── MainActivity.kt          ← Activity principal, aloja NavHostFragment
│               ├── PresentationActivity.kt  ← Splash / pantalla de presentación
│               │
│               ├── ui/                      ← Capa de presentación (Fragments + ViewModels)
│               │   ├── auth/                ← AuthFragment (contenedor Login/SignUp)
│               │   ├── login/               ← LoginFragment, ForgotPasswordFragment, LoginViewModel
│               │   ├── signup/              ← SignUpFragment, EmailVerificationFragment, SignUpViewModel
│               │   ├── landing/             ← LandingFragment, LandingViewModel (apuntes recientes)
│               │   ├── materias/            ← MateriasFragment, MateriasViewModel
│               │   ├── apuntes/             ← ApuntesFragment, NoteDetailFragment,
│               │   │                           ExtractTextFragment + sus ViewModels
│               │   └── AI/                  ← AIFragment, NotePickerFragment, DeepResearchFragment
│               │       └── result/          ← SummaryResultFragment, ConceptsResultFragment,
│               │                               QuestionsResultFragment + sus ViewModels
│               │
│               ├── domain/                  ← Capa de dominio (independiente de Android)
│               │   ├── model/               ← Clases de dominio: Note, Subject, User,
│               │   │                           ConceptItem, QAItem, DeepResearchResult
│               │   ├── repository/          ← Interfaces: AuthRepository, NoteRepository,
│               │   │                           SubjectRepository, AiRepository
│               │   └── usecase/             ← Casos de uso: FirebaseLoginUseCase,
│               │                               FirebaseSignUpUseCase, ForgotPasswordUseCase,
│               │                               GetSummaryUseCase, GetConceptsUseCase,
│               │                               GetQuestionsUseCase, DeepResearchUseCase,
│               │                               ExtractTextUseCase
│               │
│               ├── data/                    ← Capa de datos (implementaciones concretas)
│               │   ├── local/               ← Room: AppDatabase, DAOs, Entities, Prefs
│               │   │   ├── AppDatabase.kt
│               │   │   ├── AIModelPrefs.kt
│               │   │   ├── dao/             ← UserDao, MateriaDao
│               │   │   └── entity/          ← UserEntity, MateriaEntity
│               │   └── remote/
│               │       ├── FirebaseAuthRepositoryImpl.kt   ← Auth con Firebase
│               │       ├── firebase/        ← NoteRemoteDataSource, SubjectRemoteDataSource,
│               │       │                       NoteRepositoryImpl, SubjectRepositoryImpl,
│               │       │                       NoteDto, SubjectDto
│               │       └── ai/              ← AiApiService (Retrofit), AiRepositoryImpl,
│               │                               AuthInterceptor, DTOs (SummaryDto, ConceptsDto,
│               │                               QuestionsDto, DeepResearchDto, ExtractTextDto)
│               │
│               └── di/                      ← Módulos Hilt (inyección de dependencias)
│                   ├── AiModule.kt          ← provee Retrofit, OkHttp, AiApiService
│                   ├── DatabaseModule.kt    ← provee AppDatabase, DAOs
│                   ├── FirebaseModule.kt    ← provee FirebaseAuth, Firestore
│                   ├── RepositoryModule.kt  ← bindea interfaces con implementaciones
│                   └── SubjectNoteModule.kt ← provee SubjectRemoteDataSource, NoteRemoteDataSource
│
├── docs/                               ← Documentación del proyecto
│   ├── SRS.md                          ← Especificación de requisitos (IEEE 830)
│   ├── arquitectura.md                 ← Decisiones de arquitectura
│   └── diagramas/
│       └── diagramas.md                ← Este archivo (D01–D08)
│
├── Entregable-MVVM/                    ← Documentos de entrega académica
│   ├── PROMPT.md
│   └── DECISIONES.md
│
├── gradle/
│   └── libs.versions.toml              ← Catálogo de versiones de dependencias
├── build.gradle.kts                    ← Configuración global del proyecto
├── settings.gradle.kts                 ← Nombre del proyecto y módulos
└── .gitignore
```

> **Responsabilidad de cada capa:**
> - `ui/` — solo renderiza estado y delega acciones al ViewModel. No tiene lógica de negocio.
> - `domain/` — núcleo de la app. No depende de Android ni de Firebase. Es testeable de forma aislada.
> - `data/` — implementa las interfaces del dominio. Conoce Firebase, Room y Retrofit.
> - `di/` — conecta todo mediante Hilt. Define el ciclo de vida de cada dependencia.

---

## D08 — Diagrama de Despliegue / Servicios

```mermaid
graph TB
    subgraph DEVICE["📱 Dispositivo Android (API 24+)"]
        subgraph APP["App MaestrIA"]
            UI_LAYER["UI — Fragments / Activities"]
            VM_LAYER["ViewModels + Use Cases"]
            ROOM_DB["Room Database\n(SQLite local)\nusers · materias"]
            PREFS["SharedPreferences\nAIModelPrefs · SessionManager"]
            FILE_PROV["FileProvider\narchivos temporales cámara"]
        end
        CAM["📷 Cámara del dispositivo"]
    end

    subgraph GOOGLE_SERVICES["☁️ Google / Firebase Cloud"]
        FB_AUTH["🔥 Firebase Authentication\nRegistro · Login · Verificación\ncorreo · Reset password"]
        FS_DB["☁️ Cloud Firestore\nusers/{uid}/subjects\nusers/{uid}/notes"]
        CRASHLYTICS["📊 Firebase Crashlytics\nReporte automático de crashes\ny errores no fatales"]
    end

    subgraph AI_BACKEND["🤖 Backend de IA (servicio propio)"]
        AI_API["API REST\nGemini backend\nPOST /api/summary\nPOST /api/concepts\nPOST /api/questions\nPOST /api/deep-research\nPOST /api/extract-text"]
    end

    subgraph PERMISOS["🔐 Permisos del sistema"]
        PERM_NET["INTERNET (normal)"]
        PERM_CAM["android.hardware.camera\n(required=false)"]
        PERM_FILE["FileProvider\n(contenido seguro entre apps)"]
    end

    UI_LAYER <-->|StateFlow / eventos| VM_LAYER
    VM_LAYER <-->|CRUD local| ROOM_DB
    VM_LAYER <-->|preferencias| PREFS
    CAM <-->|Uri de imagen| FILE_PROV
    FILE_PROV <-->|multipart upload| AI_API

    VM_LAYER <-->|Auth calls| FB_AUTH
    VM_LAYER <-->|snapshot / write / delete| FS_DB
    VM_LAYER <-->|HTTP Bearer token| AI_API
    VM_LAYER -.->|auto-reporte errores| CRASHLYTICS

    APP -.->|requiere| PERM_NET
    APP -.->|requiere| PERM_CAM
    APP -.->|requiere| PERM_FILE

    style DEVICE fill:#1a1a2e,stroke:#2E86C1,color:#fff
    style GOOGLE_SERVICES fill:#1B3A4B,stroke:#f57c00,color:#fff
    style AI_BACKEND fill:#1B4F2A,stroke:#2E7D32,color:#fff
    style PERMISOS fill:#3B1B4F,stroke:#7B1FA2,color:#fff
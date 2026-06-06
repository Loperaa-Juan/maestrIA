# 🧠 MaestrIA

> Tu asistente de estudio con inteligencia artificial, siempre en tu bolsillo.

MaestrIA es una aplicación **Android nativa** que organiza los apuntes de un
estudiante por materia y los convierte en herramientas de estudio con IA
(resúmenes, conceptos clave, preguntas de práctica, investigación profunda y OCR
desde la cámara), funcionando **incluso sin conexión**.

---

## 📑 Tabla de contenido

- [Acerca de la aplicación](#-acerca-de-la-aplicación)
- [Stack tecnológico](#-stack-tecnológico)
- [Funcionalidades principales](#-funcionalidades-principales)
- [Arquitectura](#-arquitectura)
- [Nuestra estrategia offline](#-nuestra-estrategia-offline)
- [Diagramas](#-diagramas)
- [Cómo ejecutar localmente](#-cómo-ejecutar-localmente)

---

## 📱 Acerca de la aplicación

El problema que resuelve MaestrIA no es la falta de información, sino su
**desorganización** y el **tiempo** que cuesta procesarla en época de parciales.
Los apuntes de un estudiante suelen estar dispersos entre cuadernos, fotos, PDFs
y notas del teléfono.

MaestrIA centraliza ese material en un solo lugar, organizado por materia y
asociado a la cuenta del usuario, y le suma una capa de IA que automatiza las
tareas repetitivas del estudio:

1. **Captura** — fotografía un apunte (OCR) o escríbelo en Markdown.
2. **Procesa** — la IA genera resumen, conceptos clave y preguntas de práctica.
3. **Estudia** — todo queda guardado, sincronizado y disponible sin internet.

| Solución alternativa | Limitación que MaestrIA supera |
|---|---|
| Cuaderno físico | No hay IA, no se organiza, se pierde |
| Google Docs | Sin IA educativa ni OCR integrado |
| ChatGPT suelto | No organiza apuntes ni guarda historial por materia |
| Notion | Complejo y lento para captura rápida |
| **MaestrIA** | **Todo en uno: captura, organización, IA y offline** |

---

## 🛠 Stack tecnológico

| Capa / decisión | Tecnología | Justificación |
|---|---|---|
| **Lenguaje / plataforma** | Kotlin · Android nativo (minSdk 24, targetSdk 36) | Acceso directo a APIs del dispositivo (cámara) e integración con Jetpack |
| **UI** | XML Layouts + ViewBinding + Material Components (DayNight) | Modo oscuro nativo y compatibilidad sin restricciones |
| **Navegación** | Navigation Component + Safe Args | Back stack centralizado y paso de argumentos tipado |
| **Estado / lógica de UI** | ViewModel + StateFlow / LiveData | Sobrevive cambios de configuración; patrón MVVM |
| **Inyección de dependencias** | Hilt (Dagger) | Estándar oficial de Android, reduce boilerplate |
| **Persistencia local** | Room (SQLite) | Fuente de verdad offline-first para materias y apuntes |
| **Persistencia remota** | Cloud Firestore | Base NoSQL multiusuario en la nube, scoped por `uid` |
| **Autenticación** | Firebase Authentication | Registro, login, recuperación y verificación de correo |
| **Red / API de IA** | Retrofit 2 + OkHttp + Gson | Consumo REST con interceptor de token Bearer y Multipart (OCR) |
| **Funciones de IA** | API REST propia (backend externo) | El token del LLM nunca queda expuesto en el cliente |
| **Renderizado de contenido** | Markwon 4.6 + Prism4j 2.0 | Markdown nativo con resaltado de sintaxis, sin WebView |
| **Carga de imágenes** | Glide 4.16 | Caché eficiente de imágenes y placeholders |
| **Monitoreo de fallos** | Firebase Crashlytics | Reporte automático de crashes y errores no fatales |

> 📄 Justificación detallada con alternativas y riesgos en [`docs/arquitectura.md`](docs/arquitectura.md).

---

## ✨ Funcionalidades principales

| # | Funcionalidad | Pantalla | Offline |
|---|---|---|:---:|
| 1 | Registro, login y recuperación de contraseña | `AuthFragment` / `LoginFragment` | ❌ |
| 2 | Verificación de correo electrónico | `EmailVerificationFragment` | ❌ |
| 3 | Dashboard / inicio con apuntes recientes | `LandingFragment` | ✅ |
| 4 | CRUD completo de materias | `MateriasFragment` | ✅ |
| 5 | CRUD completo de apuntes (contenido en Markdown) | `ApuntesFragment` / `NoteDetailFragment` | ✅ |
| 6 | OCR — extracción de texto desde la cámara | `ExtractTextFragment` | ❌ |
| 7 | Generación de resumen con IA | `SummaryResultFragment` | ❌ |
| 8 | Generación de conceptos clave con IA | `ConceptsResultFragment` | ❌ |
| 9 | Generación de preguntas de práctica con IA | `QuestionsResultFragment` | ❌ |
| 10 | Investigación profunda (Deep Research) por tema | `DeepResearchFragment` | ❌ |
| 11 | Perfil de usuario y cierre de sesión | `UserProfileActivity` | ❌ |
| 12 | Modo oscuro (persistente) | App-wide | ✅ |
| 13 | Banner "offline" en vivo según conectividad | `MainActivity` | ✅ |
| 14 | Registro de fallos no fatales | `AiRepositoryImpl` → Crashlytics | ❌ |

> Las funciones de IA, autenticación, perfil y OCR requieren conexión a internet.
> Las materias y los apuntes funcionan completamente offline (ver siguiente sección).

---

## 🏛 Arquitectura

MaestrIA sigue **Clean Architecture + MVVM**, con tres capas y separación estricta
de responsabilidades. La capa `domain` no conoce Android, Firebase ni Retrofit, lo
que la hace testeable de forma aislada.

```
┌─────────────────────────────────────────────────────────────┐
│  UI  (ui/)                                                   │
│  Fragments / Activities ──observa──► ViewModels ──► UiState  │
│                                  │                           │
│                          invocan Use Cases                   │
└──────────────────────────────────┼──────────────────────────┘
                                   ▼
┌─────────────────────────────────────────────────────────────┐
│  DOMAIN  (domain/)                                          │
│  model/      → Note, Subject, User, ConceptItem, QAItem...   │
│  repository/ → AuthRepository, NoteRepository (interfaces)   │
│  usecase/    → FirebaseLoginUseCase, GetSummaryUseCase...    │
└──────────────────────────────────┼──────────────────────────┘
                                   ▼
┌─────────────────────────────────────────────────────────────┐
│  DATA  (data/)                                              │
│  ┌────────────────────┐   ┌──────────────────────────────┐  │
│  │ local/  Room        │   │ remote/  Firestore · Auth     │  │
│  │  AppDatabase, DAOs  │   │ ai/      Retrofit (API IA)    │  │
│  │  Entities, Mappers  │   │ sync/    SyncEngine/Manager   │  │
│  └────────────────────┘   └──────────────────────────────┘  │
│                       Crashlytics                            │
└─────────────────────────────────────────────────────────────┘
```

### Estructura de carpetas

```
app/src/main/java/com/juanjoselopera/proy_prog_mobile/app/
├── MaestrIAApplication.kt      ← @HiltAndroidApp, aplica modo oscuro y arranca sync
├── MainActivity.kt             ← Host de navegación + banner offline
├── PresentationActivity.kt     ← Splash / entrada
│
├── di/                         ← Módulos Hilt (Firebase, AI, Repository, SubjectNote...)
│
├── domain/                     ← Núcleo independiente de frameworks
│   ├── model/                  ← Modelos puros de dominio
│   ├── repository/             ← Interfaces (AuthRepository, NoteRepository...)
│   └── usecase/                ← Casos de uso
│
├── data/                       ← Implementaciones concretas
│   ├── local/                  ← Room: AppDatabase, dao/, entity/, mapper/, Converters
│   ├── remote/
│   │   ├── firebase/           ← DataSources + DTOs + SyncSources (Firestore)
│   │   └── ai/                 ← AiApiService, AuthInterceptor, dto/
│   └── sync/                   ← SyncEngine, SyncManager, SyncTrigger
│
├── core/connectivity/          ← ConnectivityObserver + NetworkConnectivityObserver
└── ui/                         ← auth/, landing/, materias/, apuntes/, AI/, profile/...
```

### Principios clave

- **Estado en el ViewModel** con `StateFlow`: sobrevive a la rotación; el Fragment
  solo observa y renderiza (`Resource` sealed class para Loading/Success/Error).
- **Interfaces de dominio estables**: cambiar Firestore por otro backend solo toca
  la capa `data` (`SubjectRepositoryImpl` / `NoteRepositoryImpl`); ningún ViewModel
  ni Fragment se modifica.
- **Hilt** provee dependencias como singletons (`FirebaseModule`, `AiModule`,
  `RepositoryModule`, `SubjectNoteModule`).

---

## ✈️ Nuestra estrategia offline

Las materias y los apuntes son **offline-first**: **Room es la única fuente de
verdad que lee la UI**. Esto permite crear, editar y borrar sin conexión con
respuesta instantánea, y reconciliar con Firestore cuando vuelve la red.


### Cómo funciona

1. **Lectura** — los `Flow` de los DAOs (`SubjectDao.observeAll()`,
   `NoteDao.observeAll()`) alimentan a los ViewModels. La UI nunca lee Firestore
   directamente.
2. **Escritura** — toda escritura impacta **Room primero** y marca la fila como
   `pendingSync = true` (instantáneo, funciona offline). Los borrados son
   *tombstones* lógicos (`deleted = true`) hasta confirmarse en remoto.
3. **Reconciliación** — el `SyncEngine` ejecuta un ciclo **push → pull** por
   *last-write-wins* (LWW) sobre el timestamp `updatedAt`:
   - **Push:** envía a Firestore los registros `pendingSync`; los tombstones se
     borran en remoto y luego en local (`hardDelete`).
   - **Pull:** trae el remoto con `get()` y solo sobrescribe filas locales más
     viejas que **no** estén pendientes. Si una fila local sincronizada ya no
     existe en remoto, se borra localmente.

### Disparadores de sincronización

El `SyncManager` (singleton) coordina cuándo se sincroniza, serializando los
ciclos con un `Mutex` para evitar solapamientos:

- **Al arrancar la app** y **cada vez que la conectividad pasa a online**
  (`ConnectivityObserver` expone `Flow<Boolean>`).
- **Tras cada escritura local** (los repositorios llaman a `SyncTrigger.requestSync()`)
  para dar experiencia online inmediata cuando hay red.
- **Al iniciar sesión** se sincroniza para traer los datos del usuario.
- **Al cerrar sesión** se limpia Room (`clearAllTables`) para no filtrar datos a
  otra cuenta en el mismo dispositivo.

### Banner offline

`MainActivity` colecta el estado de conectividad con `repeatOnLifecycle(STARTED)`
y muestra una barra "offline" cuando no hay red.

### Alcance

| Funciona offline | Requiere red |
|---|---|
| Materias y apuntes (lectura y escritura) | Funciones de IA (resumen, conceptos, preguntas, deep research) |
| Modo oscuro | OCR / extracción de texto |
| Navegación y datos en caché | Login, registro, recuperación y perfil |

---

## 📊 Diagramas

Los diagramas viven en [`docs/diagramas/`](docs/diagramas/). El archivo
[`docs/diagramas/Diagramas.md`](docs/diagramas/Diagramas.md) contiene todos en
formato **Mermaid** (renderizables en GitHub); las versiones `.png` están junto a él.

| # | Diagrama | Fuente (Mermaid) | Imagen |
|---|---|---|---|
| D01 | Casos de uso | [Diagramas.md](docs/diagramas/Diagramas.md#d01---diagrama-de-casos-de-uso) | [PNG](docs/diagramas/Diagrama%20de%20Casos%20de%20Uso.png) |
| D02 | Flujo de navegación | [Diagramas.md](docs/diagramas/Diagramas.md#d02---flujo-de-navegación) | [PNG](docs/diagramas/Diagrama%20Flujo%20de%20Navegación.png) |
| D03 | Arquitectura móvil (MVVM) | [Diagramas.md](docs/diagramas/Diagramas.md#d03---arquitectura-móvil-mvvm) | [PNG](docs/diagramas/Diagrama%20Arquitectura%20Móvil%20(MVVM).png) |
| D04 | Modelo de datos local y remoto | [Diagramas.md](docs/diagramas/Diagramas.md#d04--diagrama-de-modelo-de-datos-local-y-remoto) | [PNG](docs/diagramas/Diagrama%20de%20Modelo%20de%20Datos%20Local%20y%20Remo.png) |
| D05 | Sincronización local-remoto | [Diagramas.md](docs/diagramas/Diagramas.md#d05--diagrama-de-sincronización-local-remoto) | [PNG](docs/diagramas/Diagrama%20de%20sincronizacion%20local%20remota.png) |
| D06 | Diagramas de secuencia | [Diagramas.md](docs/diagramas/Diagramas.md#d06--diagramas-de-secuencia) | [PNG 1](docs/diagramas/Diagrama%20de%20secuencia%201.png) · [PNG 2](docs/diagramas/Diagrama%20de%20secuencia%202.png) |
| D07 | Estructura de carpetas | [Diagramas.md](docs/diagramas/Diagramas.md#d07--diagrama-de-estructura-de-carpetas) | [PNG](docs/diagramas/Diagrama%20de%20carpetas.png) |
| D08 | Despliegue / servicios | [Diagramas.md](docs/diagramas/Diagramas.md#d08--diagrama-de-despliegue--servicios) | [PNG](docs/diagramas/Diagrama%20despliegue%20de%20servicios.png) |

> Documentación adicional: [`docs/SRS.md`](docs/SRS.md) (requerimientos) y
> [`docs/arquitectura.md`](docs/arquitectura.md) (justificación del stack).

---

## 🚀 Cómo ejecutar localmente

### Requisitos previos

- **Android Studio** (Ladybug o superior recomendado).
- **JDK 11** (configurado en el proyecto via `sourceCompatibility`/`targetCompatibility`).
- **Android SDK 36** (compileSdk/targetSdk) y un emulador o dispositivo con **API 24+**.
- Una cuenta de **Firebase** y el backend de la **API de IA** en ejecución.

### 1. Clonar el repositorio

```bash
git clone https://github.com/Loperaa-Juan/maestrIA.git
cd maestrIA
```

### 2. Configurar Firebase

El archivo `app/google-services.json` es necesario para compilar y **no debe
versionarse con llaves**. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/),
habilita **Authentication (Email/Password)**, **Cloud Firestore** y **Crashlytics**,
y descarga el `google-services.json` a la carpeta `app/`.

### 3. Configurar la URL del backend de IA

La URL de la API de IA se define como `buildConfigField` en
[`app/build.gradle.kts`](app/build.gradle.kts):

```kotlin
buildConfigField("String", "AI_API_BASE_URL", "\"http://192.168.1.9:8000/\"")
```

Cámbiala por la IP/host donde corra tu backend de IA. Si usas un emulador y el
backend está en tu máquina, usa `http://10.0.2.2:8000/`. Para tráfico HTTP en
texto plano, asegúrate de tener configurado el `network-security-config`.

### 4. Compilar y ejecutar

Desde Android Studio: abre el proyecto, espera la sincronización de Gradle y pulsa
**Run ▶**. O desde la terminal:

```bash
# Linux / macOS
./gradlew assembleDebug
./gradlew installDebug      # instala en el dispositivo/emulador conectado

# Windows (PowerShell)
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

### 5. Ejecutar las pruebas

```bash
./gradlew test                 # tests unitarios (incluye SyncEngine y repositorios)
./gradlew connectedAndroidTest # tests instrumentados (requiere dispositivo/emulador)
```

---

<div align="center">

**MaestrIA** · Proyecto de Programación Móvil
Autenticación · Persistencia offline-first · Firestore · API REST de IA · Crashlytics · Modo oscuro · OCR

</div>

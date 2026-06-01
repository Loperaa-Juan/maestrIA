# Especificación de Requisitos de Software (SRS)
## MaestrIA — Aplicación Móvil de Apuntes con IA

**Versión:** 1.0  
**Fecha:** 2026-05-31  
**Autor:** Juan José Lopera Londoño

---

## 1. Introducción

### 1.1 Propósito del documento

Este documento especifica los requisitos de software de la aplicación móvil **MaestrIA**, una herramienta académica que combina la gestión de apuntes con capacidades de inteligencia artificial. El SRS establece el comportamiento esperado del sistema, sus restricciones técnicas y las funcionalidades que debe proveer, sirviendo como contrato entre el equipo de desarrollo y los interesados del proyecto.

### 1.2 Alcance de la app móvil

**MaestrIA** es una aplicación móvil nativa para Android que permite a estudiantes:

- Crear y organizar apuntes académicos agrupados por materias.
- Aplicar herramientas de IA sobre sus propios apuntes: resumen, extracción de conceptos clave, generación de preguntas de estudio e investigación profunda.
- Extraer texto de imágenes para convertirlo en apuntes editables.
- Gestionar su cuenta de usuario con autenticación segura.

La aplicación no incluye funcionalidades de colaboración en tiempo real ni integración con plataformas educativas de terceros (LMS) en su versión inicial.

### 1.3 Público objetivo del documento

Este documento está dirigido a:

- **Desarrolladores** responsables de implementar y mantener la aplicación.
- **Evaluadores o docentes** que necesiten comprender el alcance y la arquitectura del sistema.
- **El autor del proyecto** como referencia técnica durante el desarrollo.

### 1.4 Definiciones, siglas y abreviaturas

| Término | Definición |
|---|---|
| **SRS** | Software Requirements Specification — Especificación de Requisitos de Software. |
| **IA** | Inteligencia Artificial. Conjunto de técnicas computacionales que simulan capacidades cognitivas humanas. |
| **MVVM** | Model-View-ViewModel. Patrón arquitectónico que separa la lógica de negocio de la interfaz de usuario. |
| **Firebase** | Plataforma de Google que provee servicios backend como autenticación y base de datos en la nube. |
| **Firestore** | Base de datos NoSQL en tiempo real de Firebase, usada para persistir materias y apuntes en la nube. |
| **Room** | Biblioteca de Android (Jetpack) que abstrae SQLite para persistencia local. |
| **Hilt** | Framework de inyección de dependencias para Android basado en Dagger. |
| **OCR** | Optical Character Recognition — Reconocimiento Óptico de Caracteres. |
| **Apunte / Nota** | Unidad de contenido textual creada por el usuario dentro de una materia. |
| **Materia** | Categoría o agrupador de apuntes que representa una asignatura académica. |
| **Deep Research** | Funcionalidad de IA que analiza un apunte y genera una investigación expandida del tema. |
| **Markwon** | Biblioteca Android para renderizar Markdown en vistas nativas. |
| **Prism4j** | Biblioteca para resaltado de sintaxis de código en contenido Markdown. |
| **API** | Application Programming Interface — Interfaz de Programación de Aplicaciones. |

### 1.5 Referencias

- [Android Developers Documentation](https://developer.android.com/docs)
- [Firebase Authentication Docs](https://firebase.google.com/docs/auth)
- [Cloud Firestore Docs](https://firebase.google.com/docs/firestore)
- [Android Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Markwon — Markdown for Android](https://noties.io/Markwon/)
- IEEE Std 830-1998 — *Recommended Practice for Software Requirements Specifications*.

---

## 2. Descripción General

### 2.1 Contexto del problema

Los estudiantes universitarios y de secundaria enfrentan dificultades para organizar y estudiar eficientemente sus apuntes. Tomar notas es solo el primer paso; el verdadero reto está en procesar, resumir y memorizar la información de múltiples materias. Las soluciones existentes (Google Keep, Notion, Obsidian) son herramientas genéricas que no están diseñadas para el flujo de estudio académico, y ninguna integra IA directamente sobre el contenido del propio usuario de forma simple y accesible desde el móvil.

### 2.2 Usuario objetivo

El usuario principal de MaestrIA es un **estudiante de nivel universitario o técnico** con las siguientes características:

- Edad: 17–30 años.
- Usa su dispositivo Android como herramienta principal de estudio.
- Toma apuntes de múltiples materias durante el semestre.
- Necesita preparar exámenes de forma autónoma, sin tutores.
- Tiene acceso a internet en la mayoría de los contextos donde estudia.
- No requiere conocimientos técnicos; espera una interfaz intuitiva.

### 2.3 Descripción de la solución móvil

MaestrIA es una aplicación móvil que actúa como un **cuaderno inteligente**. El flujo principal del usuario es:

1. **Autenticación:** el usuario crea una cuenta o inicia sesión con correo y contraseña mediante Firebase Authentication, incluyendo verificación de correo electrónico.
2. **Gestión de materias:** el usuario crea materias (asignaturas) que agrupan sus apuntes.
3. **Gestión de apuntes:** dentro de cada materia, el usuario puede crear, ver y eliminar apuntes. Los apuntes soportan formato Markdown con resaltado de código.
4. **Extracción de texto por imagen:** el usuario puede fotografiar o cargar una imagen con texto escrito, y la app usa IA para transcribirlo automáticamente como apunte.
5. **Herramientas de IA:** desde cualquier apunte, el usuario puede:
   - Generar un **resumen** conciso del contenido.
   - Extraer los **conceptos clave** del apunte.
   - Obtener **preguntas de estudio** generadas automáticamente.
   - Ejecutar una **investigación profunda** (Deep Research) que amplía y enriquece el tema del apunte.
6. **Perfil de usuario:** el usuario puede consultar sus estadísticas de uso (apuntes creados, materias activas) y cerrar sesión.
7. **Pantalla de inicio (Landing):** muestra los apuntes recientes del usuario para acceso rápido.

La app respeta las preferencias del sistema operativo para **modo oscuro / modo claro**, ajustando automáticamente toda la paleta de colores.

### 2.4 Plataforma elegida: Android nativo (Kotlin)

La aplicación está desarrollada para **Android nativo** usando **Kotlin** como lenguaje principal, por las siguientes razones:

- **Acceso directo a APIs del dispositivo:** cámara, almacenamiento local y notificaciones sin capas de abstracción.
- **Material Design:** integración nativa con los componentes de Material Design 3 (`Theme.MaterialComponents.DayNight`), garantizando consistencia visual con el ecosistema Android.
- **Rendimiento:** la ejecución nativa es óptima para operaciones de UI intensivas como renderizado de Markdown y animaciones.
- **Ecosistema Jetpack:** acceso a Room, Hilt, Navigation Component, ViewModel y LiveData de forma cohesionada.
- **Audiencia:** el usuario objetivo utiliza mayoritariamente dispositivos Android en el contexto educativo latinoamericano.

**Versión mínima de Android:** API 26 (Android 8.0 Oreo).  
**Versión objetivo:** API 34 (Android 14).

### 2.5 Supuestos, restricciones y dependencias

#### Supuestos

- El usuario dispone de conexión a internet para autenticación, sincronización de datos y uso de las funciones de IA.
- El dispositivo del usuario tiene cámara disponible para la funcionalidad de extracción de texto por imagen.
- Firebase Authentication y Firestore permanecen disponibles como servicios externos durante el ciclo de vida del proyecto.
- La API de IA externa (servicio REST propietario) está disponible y autenticada mediante token Bearer.

#### Restricciones

- La aplicación no tiene versión web ni iOS en su alcance actual.
- Las funciones de IA requieren conectividad; no operan en modo offline.
- Los apuntes se almacenan en Firestore (nube); Room se usa para caché y datos de sesión local.
- No se implementa sincronización en tiempo real entre múltiples dispositivos del mismo usuario en esta versión.

#### Dependencias externas

| Dependencia | Rol en el sistema |
|---|---|
| **Firebase Authentication** | Registro, inicio de sesión, recuperación de contraseña y verificación de correo. |
| **Cloud Firestore** | Persistencia en la nube de materias y apuntes del usuario. |
| **API de IA (REST)** | Backend externo que procesa las solicitudes de resumen, conceptos, preguntas, OCR y Deep Research. |
| **Room (local)** | Caché local de datos de usuario y materias para reducir latencia. |
| **Markwon + Prism4j** | Renderizado de Markdown y resaltado de código en la vista de apuntes. |

---

## 3. Requisitos Funcionales

### 3.1 Autenticación

| ID | Requerimiento funcional | Prioridad | Criterios de aceptación |
|---|---|---|---|
| **RF-01** | La app debe permitir iniciar sesión con correo y contraseña. | Alta | Dado un usuario registrado y verificado, cuando ingresa credenciales correctas, entonces accede a la pantalla principal (Landing). |
| **RF-02** | La app debe permitir cerrar sesión. | Alta | Al cerrar sesión desde el perfil, el usuario vuelve a la pantalla de login y no puede acceder a pantallas protegidas sin autenticarse de nuevo. |
| **RF-03** | La app debe permitir registrar una cuenta nueva. | Alta | Dado un formulario válido (correo no registrado, contraseña ≥ 6 caracteres), cuando se envía, entonces se crea la cuenta en Firebase y se envía correo de verificación. |
| **RF-04** | La app debe verificar el correo electrónico del usuario. | Alta | Dado un usuario recién registrado, cuando accede con correo no verificado, entonces ve la pantalla de verificación y no puede continuar hasta confirmar el correo. |
| **RF-05** | La app debe permitir recuperar la contraseña olvidada. | Media | Dado un correo registrado, cuando el usuario solicita recuperación, entonces Firebase envía un correo de restablecimiento y la app muestra confirmación del envío. |

### 3.2 Gestión de Materias

| ID | Requerimiento funcional | Prioridad | Criterios de aceptación |
|---|---|---|---|
| **RF-06** | La app debe permitir crear una materia. | Alta | Dado un nombre de materia válido (no vacío), cuando el usuario confirma la creación, entonces la materia se guarda en Firestore y aparece en la lista inmediatamente. |
| **RF-07** | La app debe listar las materias del usuario autenticado. | Alta | Dado un usuario con materias creadas, cuando abre la sección de materias, entonces ve la lista de sus materias cargadas desde Firestore. |
| **RF-08** | La app debe permitir eliminar una materia. | Media | Dado que el usuario selecciona eliminar una materia, cuando confirma la acción, entonces la materia se elimina de Firestore y desaparece de la lista. |

### 3.3 Gestión de Apuntes

| ID | Requerimiento funcional | Prioridad | Criterios de aceptación |
|---|---|---|---|
| **RF-09** | La app debe permitir crear un apunte dentro de una materia. | Alta | Dado un título y contenido válidos, cuando el usuario guarda el apunte, entonces se persiste en Firestore vinculado a la materia y aparece en la lista de apuntes. |
| **RF-10** | La app debe mostrar el contenido del apunte renderizado en Markdown. | Alta | Dado un apunte con formato Markdown (encabezados, listas, bloques de código), cuando el usuario lo abre, entonces el contenido se muestra renderizado con resaltado de sintaxis. |
| **RF-11** | La app debe permitir eliminar un apunte. | Media | Dado que el usuario selecciona eliminar un apunte, cuando confirma la acción, entonces el apunte se elimina de Firestore y desaparece de la lista. |
| **RF-12** | La app debe mostrar los apuntes recientes en la pantalla principal. | Media | Dado un usuario autenticado con apuntes existentes, cuando abre el Landing, entonces ve los últimos apuntes creados ordenados por fecha descendente. |

### 3.4 Herramientas de Inteligencia Artificial

| ID | Requerimiento funcional | Prioridad | Criterios de aceptación |
|---|---|---|---|
| **RF-13** | La app debe generar un resumen del contenido de un apunte. | Alta | Dado un apunte seleccionado, cuando el usuario activa "Resumen", entonces la app envía el texto a la API de IA y muestra el resumen generado renderizado en Markdown. |
| **RF-14** | La app debe extraer los conceptos clave de un apunte. | Alta | Dado un apunte seleccionado, cuando el usuario activa "Conceptos", entonces la API devuelve una lista de conceptos que se muestran como tarjetas individuales. |
| **RF-15** | La app debe generar preguntas de estudio basadas en un apunte. | Alta | Dado un apunte seleccionado, cuando el usuario activa "Preguntas", entonces la API retorna pares pregunta-respuesta que se muestran en la pantalla de resultado. |
| **RF-16** | La app debe ejecutar una investigación profunda (Deep Research) sobre el tema de un apunte. | Media | Dado un apunte seleccionado, cuando el usuario activa "Deep Research", entonces la API genera un documento extendido sobre el tema y se muestra renderizado en Markdown con indicador de carga animado. |
| **RF-17** | La app debe extraer texto de una imagen usando IA (OCR). | Alta | Dado que el usuario carga o fotografía una imagen con texto, cuando selecciona el modelo de IA y confirma, entonces el texto extraído se muestra como contenido editable listo para guardar como apunte. |

### 3.5 Perfil de Usuario

| ID | Requerimiento funcional | Prioridad | Criterios de aceptación |
|---|---|---|---|
| **RF-18** | La app debe mostrar las estadísticas de uso del usuario. | Baja | Dado un usuario autenticado, cuando accede a su perfil, entonces ve el total de apuntes creados, materias activas y otros datos de actividad calculados desde Firestore. |
| **RF-19** | La app debe mostrar el nombre y correo del usuario autenticado en su perfil. | Baja | Dado un usuario autenticado, cuando abre la pantalla de perfil, entonces se muestran su nombre y correo obtenidos de Firebase Authentication. |

---

## 4. Requerimientos No Funcionales

Los siguientes requerimientos no describen funcionalidades específicas, sino cualidades del sistema que condicionan su diseño e implementación.

| Categoría | Requerimiento no funcional | Criterio verificable |
|---|---|---|
| **Usabilidad móvil** | La app debe ser clara para usuarios no técnicos. | Un usuario nuevo puede completar el flujo principal (registro → crear materia → crear apunte → usar IA) sin explicación externa. |
| **Rendimiento** | La app debe responder sin bloqueos visibles en el hilo principal. | Toda operación de red o base de datos se ejecuta en corrutinas (`Dispatchers.IO`); las pantallas muestran indicador de carga (`LoadingDotsAnimator`, `ProgressBar`) durante operaciones largas. |
| **Persistencia** | Los datos locales deben sobrevivir al cierre de la app. | Cerrar y reabrir la app conserva la sesión activa (via `SessionManager`) y los datos cacheados en Room sin requerir nueva autenticación. |
| **Disponibilidad parcial** | La app debe manejar la ausencia de internet sin bloquearse. | Cuando no hay conexión, la app muestra un mensaje de error descriptivo y permite navegar por los datos en caché sin forzar cierre. |
| **Seguridad** | Las credenciales del usuario no deben almacenarse en texto plano. | La autenticación está completamente delegada a Firebase Authentication; la app nunca persiste contraseñas localmente. Los tokens de la API de IA se transmiten vía HTTPS como Bearer token en cabecera HTTP. |
| **Mantenibilidad** | El código debe estar modularizado por capas de responsabilidad. | UI (Fragments/Activities), estado (ViewModels), lógica de negocio (UseCases), acceso a datos (Repositories) y fuentes de datos (Remote/Local) están en paquetes separados. Inyección de dependencias gestionada por Hilt. |
| **Observabilidad** | La app debe registrar los fallos relevantes en producción. | Firebase Crashlytics (plugin habilitado en `build.gradle.kts`) registra automáticamente excepciones no capturadas; al menos un error controlado debe quedar trazado por flujo crítico. |
| **Accesibilidad** | La interfaz debe ser legible y usable en distintos contextos de luz. | La app soporta modo oscuro y modo claro mediante `Theme.MaterialComponents.DayNight`; todos los colores se definen como semánticos en `values/colors.xml` y `values-night/colors.xml`, garantizando contraste adecuado en ambos modos. |
| **Compatibilidad** | La app debe funcionar en el rango de versiones Android declarado. | `minSdk = 24` (Android 7.0) y `targetSdk = 36`; sin uso de APIs exclusivas de versiones superiores al mínimo sin guardia de versión. |

---

## 5. Reglas de Negocio

Las siguientes reglas definen restricciones y políticas que el sistema debe respetar independientemente de la interfaz o el flujo de uso.

### 5.1 Acceso y privacidad

- Un usuario solo puede ver, editar y eliminar sus propios apuntes y materias; los datos están aislados por UID de Firebase Authentication.
- No se puede acceder a ninguna pantalla protegida (Landing, Materias, Apuntes, IA, Perfil) sin una sesión autenticada activa.
- Un usuario con correo no verificado no puede acceder a las funcionalidades principales hasta completar la verificación por correo.

### 5.2 Creación de registros

- No se puede crear una materia con nombre vacío o compuesto solo de espacios en blanco.
- No se puede guardar un apunte sin título.
- No se puede registrar una cuenta con un correo electrónico ya existente en Firebase Authentication.
- La contraseña de registro debe tener un mínimo de 6 caracteres, según las reglas de Firebase Authentication.

### 5.3 Uso de herramientas de IA

- No se puede ejecutar ninguna herramienta de IA (resumen, conceptos, preguntas, Deep Research) sobre un apunte con contenido vacío.
- El modelo de IA seleccionado para la extracción de texto (OCR) se persiste localmente y se mantiene como preferencia entre sesiones; el usuario no necesita seleccionarlo cada vez.
- Las funciones de IA requieren conexión activa a internet; si no hay conectividad, la acción debe bloquearse con un mensaje descriptivo antes de realizar la llamada a la API.

### 5.4 Integridad de datos

- Un apunte pertenece exclusivamente a una materia y no puede estar sin materia asociada.
- No se puede duplicar una materia con el mismo nombre para el mismo usuario.
- La eliminación de una materia o apunte debe requerir confirmación explícita del usuario antes de ejecutarse.

---

## 6. Modelo de Datos

### 6.1 Entidades y atributos

#### User (Usuario)

Representa al usuario autenticado. En la capa de dominio solo expone el correo; la persistencia local extiende los atributos.

| Atributo | Tipo | Descripción |
|---|---|---|
| `uid` | `String` | Identificador único de Firebase Authentication. Clave primaria en Room. |
| `email` | `String` | Correo electrónico del usuario. |
| `displayName` | `String?` | Nombre visible del usuario. Puede ser nulo si no fue configurado. |

#### Subject (Materia)

Representa una asignatura académica que agrupa apuntes.

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | Identificador único generado por Firestore (documento ID). |
| `name` | `String` | Nombre de la materia. No puede estar vacío. |
| `iconIndex` | `Int` | Índice que apunta al ícono visual dentro de la lista de íconos disponibles. |
| `colorIndex` | `Int` | Índice que apunta al color de la materia dentro de la lista de colores disponibles. |

#### Note (Apunte)

Unidad de contenido textual creada por el usuario dentro de una materia.

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | Identificador único generado por Firestore (documento ID). |
| `title` | `String` | Título del apunte. No puede estar vacío. |
| `content` | `String` | Cuerpo del apunte en formato Markdown. |
| `subjectId` | `String` | Referencia al `id` de la materia a la que pertenece. |
| `subjectName` | `String` | Nombre de la materia (desnormalizado para evitar joins). |
| `tags` | `List<String>` | Etiquetas opcionales para clasificar el apunte. |
| `createdAt` | `Long` | Timestamp de creación en milisegundos (epoch Unix). |
| `imageUri` | `String?` | URI de la imagen adjunta al apunte (usado por OCR). Puede ser nulo. |

---

### 6.2 Relaciones

```
User  ──< Subject   (un usuario tiene muchas materias)
User  ──< Note      (un usuario tiene muchos apuntes)
Subject ──< Note    (una materia tiene muchos apuntes, vía subjectId)
```

- La relación **User → Subject** y **User → Note** se representa en Firestore mediante rutas de subcolección bajo el UID del usuario.
- La relación **Subject → Note** se representa mediante la clave foránea `subjectId` almacenada dentro de cada documento `Note`.
- No existe una relación directa Subject → Note en Room local; la capa local solo cachea materias y el perfil del usuario.

---

### 6.3 Datos locales (Room)

Room se utiliza para caché de sesión y materias. No persiste apuntes localmente; estos siempre se leen desde Firestore.

**Tabla `users`**

| Columna | Tipo SQL | Restricción |
|---|---|---|
| `uid` | `TEXT` | PRIMARY KEY |
| `email` | `TEXT` | NOT NULL |
| `displayName` | `TEXT` | NULLABLE |

**Tabla `materias`**

| Columna | Tipo SQL | Restricción |
|---|---|---|
| `id` | `INTEGER` | PRIMARY KEY AUTOINCREMENT |
| `name` | `TEXT` | NOT NULL |
| `iconIndex` | `INTEGER` | NOT NULL |
| `colorIndex` | `INTEGER` | NOT NULL |

> Los índices `iconIndex` y `colorIndex` no almacenan IDs de recursos de Android (que cambian entre compilaciones) sino índices posicionales de las listas de opciones definidas en el Fragment.

---

### 6.4 Datos remotos (Cloud Firestore)

La estructura en Firestore es jerárquica y aísla los datos de cada usuario bajo su propio documento.

```
Firestore
└── users/                          ← colección raíz
    └── {uid}/                      ← documento por usuario (UID de Firebase Auth)
        ├── subjects/               ← subcolección de materias
        │   └── {subjectId}/        ← documento por materia (ID autogenerado)
        │       ├── name        : String
        │       ├── iconIndex   : Number
        │       └── colorIndex  : Number
        └── notes/                  ← subcolección de apuntes
            └── {noteId}/           ← documento por apunte (ID autogenerado)
                ├── title       : String
                ├── content     : String  (Markdown)
                ├── subjectId   : String  (referencia a subjects/{subjectId})
                ├── subjectName : String  (desnormalizado)
                ├── tags        : Array<String>
                ├── createdAt   : Number  (epoch ms)
                └── imageUri    : String | null
```

---

### 6.5 Estructura de la API de IA (REST)

La API de IA es un servicio REST externo consumido mediante Retrofit. Todos los endpoints requieren autenticación Bearer token en la cabecera HTTP.

| Endpoint | Método | Payload de entrada | Respuesta |
|---|---|---|---|
| `/api/summary` | `POST` | `{ "text": String }` | `{ "summary": String }` |
| `/api/concepts` | `POST` | `{ "text": String }` | `{ "concepts": List<ConceptItem> }` |
| `/api/questions` | `POST` | `{ "text": String }` | `{ "questions": List<QAItem> }` |
| `/api/deep-research` | `POST` | `{ "text": String }` | `{ "result": String }` (Markdown extendido) |
| `/api/extract-text` | `POST multipart` | `image` (Part), `model` (Part) | `{ "text": String }` |

---

## 7. Interfaces Externas

### 7.1 Firebase Authentication

- Proveedor: Google Firebase Authentication.
- Operaciones usadas: registro con correo/contraseña, inicio de sesión, cierre de sesión, verificación de correo, recuperación de contraseña.
- Integración: SDK oficial `firebase-auth` para Android. El UID del usuario autenticado es la clave de aislamiento de todos los datos en Firestore.
- Seguridad: las credenciales nunca transitan ni se almacenan en la capa de la app; Firebase gestiona tokens de sesión internamente.

### 7.2 Cloud Firestore

- Proveedor: Google Firebase Firestore (base de datos NoSQL en la nube).
- Operaciones usadas: lectura en tiempo real con `addSnapshotListener`, escritura con `add` y `set`, eliminación con `delete`.
- Colecciones accedidas: `users/{uid}/subjects` y `users/{uid}/notes`.
- Integración: SDK `firebase-firestore-ktx` con corrutinas via `kotlinx-coroutines-play-services`.

### 7.3 API REST de Inteligencia Artificial

- Tipo: servicio REST propietario (backend externo).
- URL base actual: `http://192.168.1.9:8000/` (entorno local de desarrollo; debe reemplazarse por URL de producción).
- Autenticación: Bearer token en cabecera `Authorization`, gestionado por `AuthInterceptor` (OkHttp).
- Cliente HTTP: Retrofit 2.11 + OkHttp 4.12 + convertidor Gson.
- Nota: el flag `android:usesCleartextTraffic="true"` está habilitado en el `AndroidManifest.xml` para permitir HTTP en desarrollo. Debe desactivarse en producción y migrar a HTTPS.

### 7.4 Firebase Crashlytics

- Proveedor: Google Firebase Crashlytics.
- Uso: reporte automático de crashes y errores no fatales en producción.
- Integración: plugin `com.google.firebase.crashlytics` habilitado en `build.gradle.kts`.

### 7.5 Permisos del dispositivo

| Permiso | Tipo | Motivo de uso |
|---|---|---|
| `INTERNET` | Normal (automático) | Requerido para Firebase, Firestore y la API de IA. |
| `android.hardware.camera` | Feature (no obligatoria) | Permite capturar imágenes para la funcionalidad de extracción de texto (OCR). Declarada como `required="false"` para no excluir dispositivos sin cámara. |
| `FileProvider` (`com.juanjoselopera.proy_prog_mobile.fileprovider`) | Proveedor de contenido | Permite compartir URIs de archivos temporales de imagen entre la cámara y la app de forma segura, sin exponer rutas del sistema de archivos. |

---

## 8. Criterios de Aceptación del Proyecto

Los siguientes criterios deben cumplirse para considerar la aplicación lista para evaluación o entrega.

- [ ] La app compila y ejecuta correctamente en emulador o dispositivo físico Android (API 24+) sin errores de build.
- [ ] El flujo de login funciona: un usuario con credenciales válidas accede a la pantalla principal; credenciales incorrectas muestran mensaje de error sin crashear.
- [ ] El cierre de sesión funciona: al cerrar sesión el usuario vuelve al login y no puede acceder a pantallas protegidas con el botón Atrás.
- [ ] La persistencia local funciona: cerrar y reabrir la app mantiene la sesión activa y los datos cacheados en Room sin requerir nuevo login.
- [ ] La persistencia remota funciona: crear una materia o apunte en un dispositivo y verificarlo en la consola de Firestore muestra el documento creado bajo `users/{uid}`.
- [ ] Las herramientas de IA funcionan: dado un apunte con contenido, las funciones de resumen, conceptos y preguntas retornan resultados renderizados en pantalla.
- [ ] La app maneja la ausencia de conexión sin crashear: al deshabilitar el WiFi/datos, las operaciones de red muestran un mensaje de error y la app permanece navegable.
- [ ] Se registra al menos un evento o error no fatal en Firebase Crashlytics desde un dispositivo de prueba.
- [ ] La arquitectura MVVM está documentada y se refleja en la estructura de paquetes del código: `ui/`, `domain/`, `data/`, `di/`.
- [ ] El autor puede explicar las decisiones técnicas principales: elección de plataforma, patrón arquitectónico, manejo de estado y estrategia de persistencia.

---

## 9. Matriz de Trazabilidad

| RF | Pantalla relacionada | Capa / clase relacionada | Persistencia / API relacionada | Cómo se demuestra |
|---|---|---|---|---|
| **RF-01** | `LoginFragment` / `LoginActivity` | `LoginViewModel` · `FirebaseLoginUseCase` · `FirebaseAuthRepositoryImpl` | Firebase Authentication | Demo iniciando sesión con credenciales válidas y navegando al Landing. |
| **RF-02** | `UserProfileActivity` | `UserProfileViewModel` | Firebase Authentication (`signOut`) | Demo cerrando sesión desde perfil y verificando que el botón Atrás no regresa a pantallas protegidas. |
| **RF-03** | `SignUpFragment` / `SignupActivity` | `SignUpViewModel` · `FirebaseSignUpUseCase` · `FirebaseAuthRepositoryImpl` | Firebase Authentication | Demo registrando cuenta nueva con correo no existente y recibiendo correo de verificación. |
| **RF-04** | `EmailVerificationFragment` | `SignUpViewModel` | Firebase Authentication (`sendEmailVerification`) | Demo intentando acceder con correo no verificado y viendo la pantalla de bloqueo. |
| **RF-05** | `ForgotPasswordFragment` | `ForgotPasswordViewModel` · `ForgotPasswordUseCase` | Firebase Authentication (`sendPasswordResetEmail`) | Demo ingresando correo registrado y recibiendo el email de restablecimiento. |
| **RF-06** | `MateriasFragment` | `MateriasViewModel` · `SubjectRepositoryImpl` · `SubjectRemoteDataSource` | Cloud Firestore (`users/{uid}/subjects`) | Demo creando materia y verificando el documento en la consola de Firestore. |
| **RF-07** | `MateriasFragment` | `MateriasViewModel` · `SubjectRepositoryImpl` · `SubjectRemoteDataSource` | Cloud Firestore (snapshot listener) | Demo abriendo la sección Materias y viendo la lista actualizada en tiempo real. |
| **RF-08** | `MateriasFragment` | `MateriasViewModel` · `SubjectRepositoryImpl` · `SubjectRemoteDataSource` | Cloud Firestore (`delete`) | Demo eliminando materia con confirmación y verificando que desaparece de la lista y de Firestore. |
| **RF-09** | `ApuntesFragment` | `ApuntesViewModel` · `NoteRepositoryImpl` · `NoteRemoteDataSource` | Cloud Firestore (`users/{uid}/notes`) | Demo creando apunte con título y contenido Markdown, verificando persistencia en Firestore. |
| **RF-10** | `NoteDetailFragment` | `MarkwonProvider` · `Prism4jBundler` | Cloud Firestore (lectura) | Demo abriendo apunte con encabezados, listas y bloques de código y verificando renderizado. |
| **RF-11** | `ApuntesFragment` | `ApuntesViewModel` · `NoteRepositoryImpl` · `NoteRemoteDataSource` | Cloud Firestore (`delete`) | Demo eliminando apunte con confirmación y verificando que ya no aparece en la lista ni en Firestore. |
| **RF-12** | `LandingFragment` | `LandingViewModel` · `RecentNoteAdapter` · `NoteRepositoryImpl` | Cloud Firestore (notas ordenadas por `createdAt` desc) | Demo creando apuntes nuevos y verificando que aparecen primeros en el Landing. |
| **RF-13** | `SummaryResultFragment` | `SummaryViewModel` · `GetSummaryUseCase` · `AiRepositoryImpl` | API REST `/api/summary` | Demo seleccionando un apunte y activando Resumen; verificar resultado renderizado en Markdown. |
| **RF-14** | `ConceptsResultFragment` | `ConceptsViewModel` · `GetConceptsUseCase` · `AiRepositoryImpl` | API REST `/api/concepts` | Demo activando Conceptos sobre un apunte y verificando las tarjetas de conceptos generadas. |
| **RF-15** | `QuestionsResultFragment` | `QuestionsViewModel` · `GetQuestionsUseCase` · `AiRepositoryImpl` | API REST `/api/questions` | Demo activando Preguntas y verificando los pares pregunta-respuesta mostrados en pantalla. |
| **RF-16** | `DeepResearchFragment` | `DeepResearchViewModel` · `DeepResearchUseCase` · `AiRepositoryImpl` | API REST `/api/deep-research` | Demo activando Deep Research, verificar animación de carga (`LoadingDotsAnimator`) y resultado extendido en Markdown. |
| **RF-17** | `ExtractTextFragment` | `ExtractTextViewModel` · `ExtractTextUseCase` · `AiRepositoryImpl` | API REST `/api/extract-text` (multipart) · `FileProvider` | Demo fotografiando o cargando una imagen con texto y verificando la transcripción generada. |
| **RF-18** | `UserProfileActivity` | `UserProfileViewModel` | Cloud Firestore (conteo de materias y apuntes) | Demo abriendo perfil y verificando que el total de apuntes y materias corresponde a los datos reales. |
| **RF-19** | `UserProfileActivity` | `UserProfileViewModel` | Firebase Authentication (`currentUser.email`) | Demo abriendo perfil y verificando que el correo mostrado coincide con la cuenta autenticada. |

Actúa como desarrollador Android senior con experiencia en arquitectura MVVM.

CONTEXTO:
MaestrIA es una aplicación enfocada en automatizar y mejorar la toma de notas digitales, integrando inteligencia artificial como núcleo de su funcionamiento. Está diseñada para que los usuarios puedan organizar su conocimiento de forma estructurada mediante la creación de materias, apuntes y contenidos relacionados, todo en un solo lugar.

La app incorpora funcionalidades avanzadas como OCR (Reconocimiento Óptico de Caracteres) para digitalizar información desde imágenes o documentos físicos, y capacidades de deep research que permiten analizar, resumir y enriquecer automáticamente los apuntes con información relevante.

PANTALLA A IMPLEMENTAR:
La pantalla a implementar corresponde al sistema de autenticación de maestrIA, compuesto por dos vistas principales: fragment_login y fragment_sign_up, encargadas del inicio de sesión y registro de usuarios mediante Firebase Authentication.

Actualmente, estas pantallas deben ser refactorizadas para adoptar la arquitectura MVVM (Model-View-ViewModel), con el objetivo de mejorar la separación de responsabilidades, la mantenibilidad del código y la escalabilidad de la aplicación. La lógica de negocio y manejo de estado debe trasladarse a un ViewModel, mientras que los fragments se limitan a la gestión de la UI y la observación de estados.

Además, la implementación debe ser tolerante a cambios de configuración del dispositivo (como rotación de pantalla), asegurando la persistencia del estado y evitando la pérdida de datos ingresados por el usuario. Para ello, el uso de ViewModel y mecanismos como LiveData o StateFlow resulta fundamental.

El objetivo es lograr una autenticación robusta, desacoplada y alineada con buenas prácticas modernas de desarrollo Android.

REQUERIMIENTOS:
- ViewModel con StateFlow (no LiveData)
- Data class UiState con todos los campos de estado
- Fragment con View Binding
- viewLifecycleOwner para los observers
- Validación de campos en el ViewModel, no en el Fragment
  STACK:
- Kotlin
- AndroidX
- Material Design 3
- View Binding habilitado
  RESTRICCIONES:
- Sin Jetpack Compose
- Sin dependencias externas más allá de lifecycle-viewmodel-ktx
- El Fragment no debe contener lógica de negocio
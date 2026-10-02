# Arquitectura

## Resumen

El proyecto se basa en una arquitectura Android nativa, ligera y enfocada al consumo de archivos locales. La base actual evita sobrearquitectura: no hay capas artificiales ni dependencias innecesarias. La aplicación ya cumple los objetivos principales de explorar contenido local, gestionar reproducción y conservar estado mínimo de usuario.

La implementación actual incluye:

- selección y persistencia de carpetas de música y vídeo;
- navegación por carpetas con filtro por extensión;
- orden natural de archivos;
- reproducción de música y vídeo;
- cola ordenada por carpeta;
- servicio foreground para audio en segundo plano;
- recuperación del último estado de reproducción.

## Stack

- Kotlin
- Android nativo
- Jetpack Compose
- AndroidX
- Coroutines + Flow/StateFlow
- Media3 / ExoPlayer como motor multimedia base
- almacenamiento local con SAF y DocumentFile
- persistencia ligera con SharedPreferences / DataStore si es necesario

Se evita Room por defecto porque la aplicación no requiere aún una base relacional compleja. La persistencia debe seguir siendo mínima y orientada a configuración, últimos archivos y posición de reproducción.

## Arquitectura de aplicación

Se adopta una estructura simple con una separación clara entre UI y lógica de dominio:

```text
app/
├── src/main/java/com/simplemedia/app/
│   ├── MainActivity.kt
│   ├── ui/
│   │   └── theme/
│   ├── playback/
│   │   ├── PlayerManager.kt
│   │   ├── QueueManager.kt
│   │   └── MediaPlaybackService.kt
│   ├── data/
│   │   ├── repository/
│   │   ├── datasource/
│   │   └── model/
│   ├── domain/
│   │   ├── usecase/
│   │   └── model/
│   └── settings/
└── src/main/res/
```

Esta estructura es suficiente para la fase inicial y se expandirá cuando haga falta; no se añaden servicios ni repositorios complejos sin necesidad técnica.

## Capas

### UI
La capa de interfaz usa Compose y no debe controlar directamente los detalles del motor multimedia. Su obligación es exponer estado, eventos y navegación.

### Dominio
Contiene reglas de negocio pequeñas: orden natural, validación de archivos compatibles, reglas de cola y comportamiento de reproducción.

### Data
Se encarga del acceso a archivos, carpetas y metadatos. Aquí se gestionan las exploraciones asíncronas y la lectura de contenido del almacenamiento local.

### Playback
Gestiona el motor multimedia, la cola y el servicio de reproducción en segundo plano cuando sea requerido.

## Gestión del estado

Se usa un modelo simple de ViewModel + StateFlow basado en estados de pantalla y de reproducción. Esto permite mantener la UI reactiva sin introducir una arquitectura de flujo compleja como MVI para una aplicación de este tamaño.

## Gestión de archivos

Se prioriza el almacenamiento local sin permisos omnipresentes. La base del proyecto sigue estas decisiones:

- uso de Storage Access Framework y DocumentFile para carpetas elegidas por el usuario;
- soporte mínimo para carpetas de música y vídeo configurables;
- detección asíncrona y fuera del hilo principal;
- filtros por tipos compatibles;
- ordenación natural para nombres tipo Capítulo 1, Capítulo 2, S01E01, S01E10.

## Reproducción multimedia

La elección base es Media3 / ExoPlayer por su balance entre:

- compatibilidad de audio y vídeo;
- soporte de hardware y decodificación acelerada;
- mantenimiento activo;
- estabilidad y consumo razonable;
- tamaño menor comparado con soluciones más pesadas.

Se evita añadir FFmpeg o VLC por defecto porque aumentan complejidad y tamaño sin que se haya demostrado una necesidad técnica real en la fase inicial.

## Persistencia

La base del sistema usa almacenamiento ligero: preferencias o un archivo de configuración pequeño para:

- carpeta de música;
- carpeta de vídeo;
- último archivo reproducido;
- última posición;
- preferencias de reproducción.

Room no se añade de entrada porque no es necesario todavía; si más adelante se requiere historial grande o consultas complejas, se reconsiderará.

## Gestión de colas

La cola debe ser única e independiente de la UI. La lógica se manejará en un componente dedicado que:

- añade archivos manualmente;
- añade varias entradas;
- elimina y reordena;
- limpia la cola;
- avanza/anterior;
- crea cola automática desde una carpeta;
- mantiene un orden original y un orden actual de reproducción para aleatorizar sin perder la secuencia base.

No se crean dos sistemas de reproducción distintos: la cola automática de una carpeta es simplemente el conjunto inicial de elementos de la cola unificada.
## Diagrama

```text
UI (Compose)
   ↓
ViewModel / StateFlow
   ↓
UseCases + QueueManager
   ↓
File Explorer + Media Playback
   ↓
Android Storage + Media3 / ExoPlayer
```

## Decisiones técnicas

- Se prioriza simplicidad y estabilidad sobre una infraestructura demasiado grande.
- El motor multimedia base será Media3/ExoPlayer, con revisión de FFmpeg/VLC solo si la compatibilidad lo exige.
- El proyecto será offline, sin Internet, sin analítica ni servicios remotos.
- La app se mantiene útil y mantenible con un conjunto pequeño de capas y una lógica de cola clara.
- La persistencia se mantiene ligera para cumplir con la prioridad “bajo consumo”.

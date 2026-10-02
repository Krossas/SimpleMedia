# AGENTS.md

## Proyecto

Este repositorio contiene una aplicación Android nativa de código abierto para reproducir música y vídeo almacenados localmente en el dispositivo.

El nombre definitivo de la aplicación puede decidirse posteriormente.

El objetivo principal es crear un reproductor:

* ligero;
* rápido;
* estable;
* privado;
* completamente offline;
* con bajo consumo de recursos;
* compatible con una amplia variedad de archivos multimedia;
* sin publicidad;
* sin cuentas;
* sin servidores;
* sin telemetría.

---

# 1. REGLAS FUNDAMENTALES

Estas reglas tienen prioridad durante todo el desarrollo.

### 1.1 No sobrearquitecturar

La aplicación debe ser mantenible, pero no debe convertirse en un proyecto innecesariamente complejo.

No crear:

* capas artificiales;
* interfaces innecesarias;
* abstracciones sin utilidad;
* servicios innecesarios;
* dependencias externas para tareas triviales.

Si Android/Kotlin/AndroidX ya proporciona una solución adecuada, utilizarla.

---

### 1.2 Bajo consumo

El consumo de:

* RAM;
* CPU;
* batería;
* almacenamiento;
* operaciones de disco

es una prioridad fundamental.

Evitar:

* escaneos continuos;
* polling;
* servicios permanentes;
* procesamiento innecesario;
* cargar archivos completos en RAM;
* generar miniaturas innecesarias;
* mantener listas gigantes en memoria cuando puedan procesarse bajo demanda.

---

### 1.3 Offline

La aplicación debe funcionar sin conexión a Internet.

No añadir:

* servidores;
* APIs externas;
* Firebase;
* Google Analytics;
* Crashlytics;
* publicidad;
* trackers;
* cuentas;
* servicios cloud.

No añadir el permiso `INTERNET` salvo que exista una necesidad técnica imprescindible y haya sido aprobada explícitamente.

---

### 1.4 Código libre

El proyecto debe utilizar software y dependencias compatibles con su licencia.

Toda dependencia externa debe tener una razón técnica.

Antes de añadir una dependencia:

1. comprobar si Android ya proporciona la funcionalidad;
2. comprobar si AndroidX la proporciona;
3. comprobar el tamaño que añade;
4. comprobar su impacto en rendimiento;
5. comprobar su licencia;
6. documentarla.

---

# 2. STACK

El stack preferido es:

* Kotlin;
* Android nativo;
* Jetpack Compose;
* AndroidX;
* Kotlin Coroutines;
* Flow / StateFlow;
* Media3 cuando sea adecuado;
* MediaSession;
* Gradle.

No utilizar:

* Flutter;
* React Native;
* Electron;
* Cordova;
* Capacitor.

La aplicación no debe depender de Android Studio para compilar.

Debe ser posible trabajar mediante:

* Visual Studio Code;
* terminal;
* Gradle;
* Android SDK;
* ADB.

---

# 3. MOTOR MULTIMEDIA

Media3/ExoPlayer es la primera opción.

Sin embargo, NO asumir automáticamente que es la solución definitiva.

Antes de elegir el motor multimedia se debe analizar:

* Media3/ExoPlayer;
* Media3 + FFmpeg;
* libVLC;
* otras alternativas open source razonables.

Comparar:

* compatibilidad;
* códecs;
* formatos;
* RAM;
* CPU;
* batería;
* aceleración hardware;
* tamaño del APK;
* mantenimiento;
* complejidad;
* licencias.

La decisión debe quedar documentada en:

`MEDIA_ENGINE.md`

No añadir FFmpeg o VLC simplemente para aumentar el número de formatos soportados.

---

# 4. ANDROID

Utilizar APIs modernas de Android.

Evitar APIs deprecated cuando exista una alternativa razonable.

Para almacenamiento utilizar preferentemente:

* Storage Access Framework;
* DocumentFile;
* MediaStore cuando corresponda.

No utilizar permisos de almacenamiento antiguos innecesariamente.

---

# 5. ARQUITECTURA

Preferencia:

MVVM o una arquitectura moderna equivalente.

Separar claramente:

* UI;
* estado;
* navegación;
* acceso a archivos;
* reproducción;
* gestión de cola;
* persistencia.

La UI no debe controlar directamente el motor multimedia mediante código desordenado.

---

# 6. REPRODUCCIÓN

La aplicación tendrá dos categorías principales:

* música;
* vídeo.

Cada una tendrá una carpeta configurable.

El usuario podrá:

* reproducir archivos;
* añadir archivos a cola;
* seleccionar varios archivos;
* reproducir carpetas;
* utilizar colas automáticas.

---

# 7. COLA AUTOMÁTICA

Una función fundamental es crear automáticamente una cola a partir de una carpeta.

Ejemplo:

```text
Capitulo01.mkv
Capitulo02.mkv
Capitulo03.mkv
...
Capitulo13.mkv
```

debe reproducirse en ese orden sin que el usuario tenga que añadir manualmente los 13 archivos.

La ordenación debe ser NATURAL.

Incorrecto:

```text
Capitulo1
Capitulo10
Capitulo11
Capitulo2
```

Correcto:

```text
Capitulo1
Capitulo2
Capitulo10
Capitulo11
```

También debe funcionar con:

```text
S01E01
S01E02
S01E10
S01E11
```

Crear tests específicos.

---

# 8. SERIES

No implementar inicialmente una biblioteca completa de series.

No crear innecesariamente modelos de:

* serie;
* temporada;
* episodio.

Una carpeta debe poder funcionar como fuente de una cola.

Ejemplo:

```text
Serie/
├── S01E01.mkv
├── S01E02.mkv
└── S01E03.mkv
```

---

# 9. ARCHIVOS

Como mínimo estudiar soporte para:

### Audio

* MP3;
* AAC;
* M4A;
* WAV;
* OGG;
* Opus;
* FLAC;
* AIFF;
* WMA cuando sea viable.

### Vídeo

* MP4;
* MKV;
* AVI;
* WebM;
* MOV;
* M4V;
* TS.

La compatibilidad real dependerá del motor multimedia y del dispositivo.

No afirmar que un formato es universalmente compatible si depende del hardware.

---

# 10. UNICODE

La aplicación debe soportar correctamente:

* ñ;
* á;
* é;
* í;
* ó;
* ú;
* ü;
* ¿;
* ¡;
* Unicode.

No asumir ASCII.

---

# 11. SEGUNDO PLANO

La reproducción de música debe funcionar con:

* pantalla apagada;
* teléfono bloqueado;
* otra aplicación abierta.

Utilizar:

* MediaSession;
* servicio de reproducción adecuado;
* controles multimedia del sistema.

No mantener un servicio activo cuando no exista reproducción.

---

# 12. PERSISTENCIA

Guardar localmente cuando sea necesario:

* carpetas seleccionadas;
* último archivo;
* posición de reproducción;
* cola cuando corresponda;
* configuración.

No introducir Room/SQLite por defecto.

Determinar primero si una solución más ligera es suficiente.

---

# 13. ERRORES

Nunca permitir que un archivo multimedia defectuoso cierre la aplicación.

Si un archivo no puede reproducirse:

1. mostrar un mensaje comprensible;
2. registrar información técnica útil para diagnóstico;
3. continuar con la cola cuando corresponda.

---

# 14. TESTS

Las partes críticas deben tener tests.

Especialmente:

* ordenación natural;
* cola;
* cola automática;
* nombres Unicode;
* persistencia;
* archivos incompatibles;
* siguiente/anterior;
* repetición;
* aleatorio.

---

# 15. COMPILACIÓN

Después de cambios importantes:

```bash
./gradlew assembleDebug
```

Debe ejecutarse una compilación y corregirse cualquier error antes de continuar.

Cuando existan tests:

```bash
./gradlew test
```

No acumular errores para solucionarlos posteriormente.

---

# 16. CAMBIOS

Antes de modificar código existente:

1. leerlo;
2. comprenderlo;
3. identificar dependencias;
4. modificar únicamente lo necesario.

No borrar código funcional sin motivo.

No sustituir una arquitectura funcional por otra sin una razón técnica.

---

# 17. DOCUMENTACIÓN

Mantener actualizados:

* `README.md`
* `ARCHITECTURE.md`
* `MEDIA_ENGINE.md`
* `QUEUE_BEHAVIOUR.md`
* `BUILD.md`
* `THIRD_PARTY_LICENSES.md`

Si una decisión importante cambia, actualizar la documentación.

---

# 18. FASES

El proyecto se desarrollará en este orden:

1. Análisis del entorno.
2. Proyecto Android mínimo.
3. Carpetas y almacenamiento.
4. Explorador multimedia.
5. Audio.
6. Vídeo.
7. Cola.
8. Cola automática.
9. Persistencia.
10. Segundo plano.
11. Optimización.
12. Tests.
13. Release.
14. Documentación final.

No saltar directamente a una implementación gigantesca.

---

# 19. FINAL DE CADA FASE

Al terminar una fase:

* compilar;
* ejecutar tests relevantes;
* corregir errores;
* documentar cambios.

Informar de:

* qué se ha implementado;
* archivos creados;
* archivos modificados;
* tests;
* resultado de compilación;
* problemas pendientes.

---

# 20. DECISIONES QUE REQUIEREN CONSULTA

No preguntar por decisiones pequeñas.

Toma decisiones profesionales sobre:

* nombres de clases;
* nombres de variables;
* estructura interna;
* componentes de UI;
* pequeños detalles de implementación.

Sí debes detenerte y consultar antes de tomar decisiones que cambien significativamente:

* arquitectura;
* motor multimedia;
* licencias;
* privacidad;
* almacenamiento;
* comportamiento de las colas;
* compatibilidad Android;
* requisitos funcionales.

---

# 21. NO INVENTAR

Nunca:

* inventar APIs;
* inventar métodos;
* inventar dependencias;
* utilizar código pseudofuncional como si fuera código real;
* afirmar que una compilación funciona sin haberla comprobado;
* afirmar que un formato multimedia funciona sin haberlo verificado.

Si algo no está disponible, indicarlo.

---

# 22. PRINCIPIO GENERAL

Cuando haya que elegir entre:

A) una solución compleja que hace muchas cosas;

y:

B) una solución sencilla que cumple los requisitos;

preferir B.

El objetivo no es construir el reproductor multimedia más grande.

El objetivo es construir un reproductor multimedia local:

**ligero + rápido + estable + compatible + privado + libre.**

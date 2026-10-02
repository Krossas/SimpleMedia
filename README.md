# SimpleMedia

SimpleMedia es un reproductor multimedia Android nativo, ligero y completamente offline para archivos locales de música y vídeo.

## Estado actual

La base funcional ya está implementada con:

- proyecto Android nativo en Kotlin;
- Jetpack Compose como base de UI;
- arquitectura mínima MVVM-like con estado reactivo;
- selección de carpetas con Storage Access Framework;
- exploración de archivos locales por música y vídeo;
- orden natural para nombres tipo Capítulo, S01E01 y similares;
- reproducción local de audio con ExoPlayer;
- reproducción local de vídeo con ExoPlayer y PlayerView;
- cola ordenada generada a partir de una carpeta;
- persistencia de carpetas, cola y última pista;
- servicio foreground para audio en segundo plano;
- gestión de errores para archivos incompatibles o defectuosos.

La aplicación sigue manteniendo un enfoque ligero, offline y sin sobrearquitectura.

## Principios

- estabilidad por encima de la complejidad;
- bajo consumo de CPU, RAM y batería;
- trabajo completamente offline;
- compatibilidad realista con el motor multimedia elegido;
- sin publicidad, cuentas ni servicios remotos.

## Stack inicial

- Kotlin;
- Android nativo;
- Jetpack Compose;
- AndroidX;
- Kotlin Coroutines;
- StateFlow/Flow;
- Media3 / ExoPlayer como motor principal;
- almacenamiento local y opciones ligeras de persistencia.

## Documentación técnica

- [ARCHITECTURE.md](ARCHITECTURE.md)
- [MEDIA_ENGINE.md](MEDIA_ENGINE.md)
- [BUILD.md](BUILD.md)
- [QUEUE_BEHAVIOUR.md](QUEUE_BEHAVIOUR.md)
- [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md)

## Compilación

El proyecto se compila con Gradle y Android SDK local, sin requerir Android Studio para la fase base:

```bash
export ANDROID_HOME="$HOME/.local/android-sdk"
export ANDROID_SDK_ROOT="$HOME/.local/android-sdk"
export PATH="$HOME/.local/gradle/gradle-8.7/bin:$PATH"
./gradlew assembleDebug
```

## Licencia

La aplicación principal se distribuye bajo GPLv3. Esta licencia cubre el código desarrollado en este repositorio.

Las dependencias de terceros conservan sus licencias propias. En particular, las dependencias de Android, Kotlin, Compose y Media3/ExoPlayer usan licencias Apache 2.0, que son compatibles con GPLv3 y no se convierten a GPLv3 por fuerza.

Consulte:

- [LICENSE](LICENSE)
- [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md)

La compatibilidad jurídica de la combinación GPLv3 + Apache 2.0 es compatible y aceptada para este tipo de proyecto; cada dependencia mantiene su licencia original y la aplicación principal se publica bajo GPLv3.

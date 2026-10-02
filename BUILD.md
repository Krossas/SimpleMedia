# Compilación

## Requisitos del entorno

La base del proyecto se ha validado con estos componentes:

- JDK 17
- Gradle 8.7
- Android SDK Command-line Tools
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android SDK Platform-Tools

## JDK

El entorno actual usa OpenJDK 17. Notas importantes:

```bash
java -version
javac -version
```

Se recomienda usar JDK 17 porque Android Gradle Plugin 8.5.x y Kotlin 1.9.x son compatibles con esta versión.

## Android SDK

La instalación local usada para la validación está en:

```bash
/home/churripc/.local/android-sdk
```

Se configuró con variables de entorno:

```bash
export ANDROID_HOME="$HOME/.local/android-sdk"
export ANDROID_SDK_ROOT="$HOME/.local/android-sdk"
export PATH="$ANDROID_HOME/cmdline-tools/cmdline-tools/bin:$ANDROID_HOME/platform-tools:$PATH"
```

## Gradle

La distribución local de Gradle se encuentra en:

```bash
$HOME/.local/gradle/gradle-8.7
```

Y se puede usar así:

```bash
export PATH="$HOME/.local/gradle/gradle-8.7/bin:$PATH"
```

## Estructura del proyecto base

El proyecto actual está en esta estructura:

```text
SimpleMedia/
├── app/
│   ├── build.gradle.kts
│   └── src/
├── build.gradle.kts
├── gradle.properties
├── local.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── gradle/wrapper/
```

## Crear y actualizar el wrapper

El wrapper se genera con Gradle local:

```bash
cd /home/churripc/Escritorio/SimpleMedia
export PATH="$HOME/.local/gradle/gradle-8.7/bin:$PATH"
./gradlew wrapper
```

## Compilar Debug

```bash
cd /home/churripc/Escritorio/SimpleMedia
export ANDROID_HOME="$HOME/.local/android-sdk"
export ANDROID_SDK_ROOT="$HOME/.local/android-sdk"
export PATH="$HOME/.local/gradle/gradle-8.7/bin:$PATH"
./gradlew assembleDebug
```

## Ejecutar tests

En esta fase inicial no hay suite de tests del proyecto, pero la preparación general es:

```bash
./gradlew test
```

## Conectar un dispositivo

Si un terminal Android está conectado:

```bash
adb devices
```

## Instalar APK

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Generar release

```bash
./gradlew assembleRelease
```

## Ubicación del APK

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Observación importante

Este proyecto utiliza una instalación local del SDK para evitar depender de Android Studio. Los valores exactos de rutas se mantienen en `local.properties` para que el entorno pueda reproducirse en este equipo.

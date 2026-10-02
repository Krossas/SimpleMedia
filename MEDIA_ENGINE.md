# Motor multimedia

## Resumen ejecutivo

La decisión inicial es mantener Media3 / ExoPlayer como primer motor. No se incorporan FFmpeg, libVLC ni otros motores todavía. Primero debe implementarse una versión funcional y comprobable con Media3 para demostrar si hay una limitación técnica real.

## Opción A: Media3 / ExoPlayer

### Ventajas
- excelente integración con Android y AndroidX;
- soporte razonable para música y vídeo local;
- decodificación hardware y optimización del sistema;
- mantenimiento activo;
- licencia Apache 2.0 compatible con GPLv3;
- menor peso y menor complejidad que VLC o FFmpeg integrado.

### Desventajas
- no garantiza compatibilidad con cada codec raro;
- depende del hardware del dispositivo;
- puede requerir pruebas reales para mostrar que un archivo funciona bien.

### Valoración
Opción recomendada para la fase inicial.

## Opción B: Media3 + FFmpeg

### Ventajas
- compatibilidad muy alta con muchos formatos y codecs.

### Desventajas
- añade complejidad y tamaño de APK;
- más consumo de CPU y RAM;
- requiere evaluar licencias y estabilidad;
- no es necesario antes de comprobar la compatibilidad real con ExoPlayer.

### Valoración
No se añade todavía.

## Opción C: libVLC

### Ventajas
- amplia compatibilidad histórica.

### Desventajas
- más peso y mayor uso de recursos;
- más complejidad de mantenimiento;
- no es la opción más ligera para este proyecto.

### Valoración
No se añade todavía.

## Opción D: otras alternativas

No se consideran por ahora. El objetivo es mantener la app ligera y comprobar realidades del hardware antes de añadir más complejidad.

## Matriz de pruebas de compatibilidad

La compatibilidad final debe probarse en dispositivos Android reales. Estas pruebas se realizarán cuando el proyecto funcional esté listo.

### Audio

- MP3
- FLAC
- AAC / M4A
- OGG
- Opus
- WAV

### Vídeo

- MP4 / H.264
- MKV / H.264
- MKV / HEVC
- WebM / VP9
- AV1
- AVI
- MKV con AC3 / E-AC3 cuando sea posible

## Decisión

Se mantiene Media3 / ExoPlayer como solución principal. Se evita añadir FFmpeg, libVLC u otros motores mientras no exista una limitación demostrada por pruebas reales.

## Licencia

Media3 / ExoPlayer usa Apache 2.0. Esta licencia es compatible con GPLv3, y el proyecto mantiene la aplicación principal bajo GPLv3 mientras que cada dependencia conserva su propia licencia.

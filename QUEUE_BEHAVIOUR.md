# Comportamiento de las colas

## Principio general

La aplicación usa una única cola de reproducción para todos los casos: reproducción directa, carpeta, selección múltiple, añadidos manuales y futuras adiciones de carpetas. No existen dos colas de reproducción independientes.

La implementación actual sigue este principio y genera la cola desde una carpeta compatible con orden natural, luego avanza por la cola con navegación anterior/siguiente. Los elementos inválidos o vacíos se filtran antes de la reproducción para que la cola siga siendo estable.

La cola se modela como:

- orden original de la cola;
- orden de reproducción actual;
- elemento actual;
- estado de aleatorio, repetición y reanudación.

## 1. Reproducir archivo

Cuando el usuario pulsa "Reproducir" sobre un archivo:

- sustituye la cola actual;
- comienza inmediatamente ese archivo;
- el archivo elegido pasa a ser el elemento actual;
- no se mezcla con una cola previa salvo que el usuario decida explícitamente añadir elementos.

## 2. Añadir a cola

Cuando el usuario pulsa "Añadir a cola":

- añade el archivo al final de la cola actual;
- no interrumpe la reproducción en curso;
- conserva el orden actual que ya existía en la cola.

## 3. Reproducir carpeta

Cuando el usuario elige "Reproducir carpeta":

- la cola actual se sustituye por todos los archivos multimedia compatibles de esa carpeta;
- la lista se ordena de forma natural;
- la reproducción empieza por el primer archivo de la carpeta;
- la cola se comporta como una cola automática basada en la carpeta.

## 4. Reproducir desde un archivo concreto dentro de una carpeta

Si el usuario está dentro de una carpeta y pulsa "Reproducir" sobre un archivo concreto, la app debe:

1. generar la cola automática de esa carpeta;
2. mantener el orden natural;
3. comenzar por el archivo seleccionado;
4. continuar después con los siguientes archivos.

Los archivos anteriores al elemento seleccionado pueden mantenerse en la cola para permitir "Anterior" y recuperación del contexto, pero no deben reproducirse antes del archivo seleccionado.

Esto significa que la cola automática no se destruye: se genera una cola completa y el elemento actual se marca como punto de inicio.

## 5. Añadir carpeta a cola

Conceptualmente debe existir la posibilidad de añadir los archivos compatibles de una carpeta al final de la cola actual.

No es obligatorio implementarlo en la primera versión si complica innecesariamente la interfaz, pero la arquitectura debe ser compatible con esa operación posterior sin crear un segundo sistema de colas.

## 6. Ejemplo de cola única

```text
Carpeta base:
S01E01
S01E02
S01E03

Después el usuario añade:
pelicula.mkv

Estado final de la cola:
S01E01
S01E02
S01E03
pelicula.mkv
```

Cuando termina `S01E03`, la reproducción continúa con `pelicula.mkv`.

## 7. Ordenación natural

La cola debe ordenarse por orden natural para nombres como:

```text
Capitulo1
Capitulo2
Capitulo3
Capitulo10
```

Y también para patrones tipo:

```text
S01E01
S01E02
S01E10
S01E11
```

## 8. Aleatorio

El modo aleatorio no debe destruir permanentemente el orden original.

Debe existir conceptualmente:

- orden original de la cola;
- orden de reproducción actual.

Ejemplo:

```text
Orden original:
1
2
3
4
5

Orden aleatorio:
3
5
1
4
2
```

Si el usuario desactiva el aleatorio, debe poder volver al orden original. La implementación puede apoyarse en mecanismos proporcionados por el reproductor si son adecuados, siempre que la cola lógica siga siendo una única estructura y el orden original se preserve.

## 9. Anterior

El comportamiento de "Anterior" debe ser el siguiente:

- si el archivo actual lleva reproduciéndose durante más de un umbral razonable, "Anterior" puede volver al principio del archivo actual;
- si el archivo ya está cerca del inicio o el usuario pulsa "Anterior" de nuevo, se avanza al elemento anterior de la cola;
- si la cola tiene un elemento anterior válido, se reproduce ese archivo;
- si no existe elemento anterior, el reproductor se mantiene en el estado actual o se pausa según el comportamiento del motor elegido.

Si esta lógica depende del reproductor elegido (por ejemplo, Media3), debe documentarse como decisión técnica de la implementación.

## 10. Siguiente / Finalización

- "Siguiente" avanza según el orden de reproducción actual;
- si se llega al final de la cola, la reproducción se comporta según el modo de repetición o se detiene;
- la cola no se duplica ni se separa en dos sistemas distintos.

## 11. Repetición

La aplicación debe apoyar estas modalidades:

- normal;
- repetir archivo;
- repetir cola.

La lógica de repetición debe respetar la cola unificada y no crear una cola paralela con un orden distinto.

## 12. Cierre y reinicio

Cuando la aplicación se cierra, la última posición conocida, el último archivo reproducido y la cola actual pueden persistirse. Al volver a abrirla, si la opción de reanudación está activada y el archivo sigue existiendo, la aplicación puede continuar desde la posición guardada.

## 13. Archivos incompatibles o corruptos

Si un archivo no es compatible o está corrupto:

- la app no debe cerrarse;
- debe mostrar un mensaje claro;
- debe registrar información técnica útil para diagnóstico;
- debe continuar con la cola si corresponde.

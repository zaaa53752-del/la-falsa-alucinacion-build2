# La Falsa Alucinación — Forge 1.20.1

Mod de terror psicológico para **Minecraft Java 1.20.1 + Forge 47.4.16 + Java 17**.

## Obtener el `.jar` sin compilar en tu PC

Este proyecto incluye una **compilación en la nube mediante GitHub Actions**. Así no necesitas una computadora potente ni saber programar para compilarlo localmente. Necesitas internet y una cuenta gratuita de GitHub para subir el proyecto; los servidores de GitHub hacen el trabajo pesado.

1. Descarga y extrae el ZIP del proyecto.
2. En GitHub, crea un repositorio nuevo. Para que Actions pueda compilarlo sin límites de minutos, puedes hacerlo público; no subas contraseñas ni archivos personales.
3. Sube **el contenido de la carpeta `la_falsa_alucinacion`**, conservando la estructura de carpetas. Es importante que `.github/workflows/build.yml` quede exactamente en esa ruta dentro del repositorio, junto a `build.gradle` y `src`. Para una compilación en la nube sin cargos por minutos, GitHub indica que los runners estándar en repositorios públicos son gratuitos: https://docs.github.com/en/billing/concepts/product-billing/github-actions
4. Confirma los archivos en la rama `main` o `master`. Abre la pestaña **Actions** y entra en la ejecución llamada **Build La Falsa Alucinacion (Forge 1.20.1)**.
5. Si termina en verde, busca **Artifacts** al final de esa ejecución y descarga `la-falsa-alucinacion-forge-1.20.1`. Dentro del ZIP descargado está `la_falsa_alucinacion-1.0.0.jar`.
6. Copia el `.jar` a `%appdata%\.minecraft\mods` y abre Minecraft con **Forge 1.20.1**.

La primera ejecución descarga las librerías de Minecraft/Forge y puede tardar unos minutos. Si aparece una cruz roja, abre la ejecución para ver el error: eso significa que la compilación no terminó y todavía no hay un JAR funcional.

## Compilar localmente (opcional)

Si prefieres compilar en Windows, instala un **JDK 17**, extrae la carpeta y ejecuta `build.bat` con conexión a internet. El resultado, si compila correctamente, estará en `build\libs\la_falsa_alucinacion-1.0.0.jar`.

## Qué incluye

- El incidente puede comenzar durante la noche del **quinto día** de una partida normal.
- Guarda una copia NBT del inventario y la ubicación originales para intentar devolverte al despertar.
- Dimensión de terreno plano corrompido: suelo de diorita, bosque de robles alterado y ausencia de estructuras/animales normales.
- Cofres con objetos extraños y la Gema de la Libertad.
- Acechador original con modelo animado: se mueve rápido y desaparece al mirarlo de frente.
- Cofre Errante con animación de patas y partículas violetas.
- Fases visuales roja, azul, blanca y verde, distorsión de pantalla, niebla alterada, hambre oculta y bloqueo de ESC durante el episodio.
- Criaturas hostiles mutadas, cabañas corruptas que ascienden y descienden en la fase verde, meteoritos, barrera metálica y secuencia de falso despertar.
- Texturas nuevas con una paleta de obsidiana, turquesa brillante y violeta glitcheado, siguiendo la referencia visual compartida.
- No se escriben pistas ni bromas en el chat.

## Controles y objetivo

Explora y encuentra la gema. Sus partículas te guían hacia la barrera. Fabrica el Pico Glitch con gema + palo + telaraña, rompe la barrera y usa el objeto glitcheado que aparece tras ella para activar el desenlace.

## Compatibilidad

Este proyecto aún requiere una prueba real dentro de Minecraft. Una compilación en verde comprueba que se generó el JAR, pero no garantiza por sí sola que toda la historia funcione perfectamente con otros mods. Haz una copia de seguridad del mundo antes de probar mods de terror experimentales.

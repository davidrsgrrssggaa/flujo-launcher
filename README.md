# Flujo

Launcher para Android (móvil) que parte del texto: hora, cinco favoritas en letra grande y un buscador. Kotlin + Jetpack Compose, Android 8.0 o superior.

## Obtener el APK

**Opción A: Android Studio**
1. Abre esta carpeta con Android Studio (versión Koala o posterior) y espera a que termine la sincronización.
2. Menú `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
3. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.

**Opción B: GitHub, sin instalar nada**
1. Sube esta carpeta a un repositorio nuevo de GitHub (rama `main`).
2. Entra en la pestaña `Actions`, abre "Compilar APK" y pulsa `Run workflow` (también se lanza solo con cada push).
3. Al terminar, descarga `flujo-apk` desde la sección Artifacts de la ejecución y descomprime el zip.

## Instalar

1. Copia `app-debug.apk` al móvil y ábrelo (permite instalar desde esa fuente si Android lo pide).
2. Pulsa el botón de inicio y elige Flujo como app de inicio, o ve a `Ajustes > Apps > Apps predeterminadas > Inicio`. Desde Flujo también puedes abrir ese ajuste en `Ajustes > Launcher predeterminado`.

## Gestos

| Gesto | Resultado |
| --- | --- |
| Deslizar arriba | Abre el cajón de apps |
| Tocar el buscador | Abre el cajón con el teclado; Intro abre el único resultado |
| Mantener pulsado en el inicio | Abre los ajustes |
| Doble toque | Bloquea la pantalla (requiere activar el servicio de accesibilidad de Flujo) |
| Mantener una app | Fijar o quitar de favoritas, ocultar, información, desinstalar |
| Botón de inicio dentro del launcher | Vuelve al inicio |

## Estructura

- `MainActivity.kt`: carga las apps, escucha instalaciones y desinstalaciones.
- `data/AppRepository.kt`: lista de apps, lanzamiento y favoritas por defecto.
- `data/LauncherPrefs.kt`: ajustes, favoritas, apps ocultas y contador de uso.
- `ui/`: `HomeScreen`, `DrawerScreen` (con selector de letras), `SettingsScreen` y el tema.
- `LockService.kt`: servicio de accesibilidad que solo ejecuta "bloquear pantalla".

## Pendiente

- El punto de notificaciones de la maqueta no está implementado: requiere un `NotificationListenerService`.
- La tipografía usa la fuente del sistema. Para usar Outfit, añade el archivo a `res/font/` y úsalo en `FontFamily`.
- Este proyecto se escribió sin poder compilarlo en el entorno donde se generó. Si Gradle señala algún error, revisa primero las versiones en `build.gradle.kts`.

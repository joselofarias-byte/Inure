# Inure — JoseloFarias fork

Edición independiente de Inure mantenida por **JoseloFarias**, reconstruida sobre upstream **build107.2.4** y distribuida con paquete propio.

## Identidad

- Aplicación: **Inure JoseloFarias**
- Paquete: `app.simple.inure.joselofarias`
- Flavor: `joselofarias`
- Base upstream: `Hamza417/Inure build107.2.4`
- Licencia: GPLv3; se conservan atribuciones y avisos del proyecto original.

## Diferencias mantenidas

- Edición FOSS feature-complete, sin trial, compras, Unlocker ni verificación comercial.
- Reutiliza la superficie GitHub/FOSS de upstream, incluyendo Debloat y VirusTotal.
- Búsqueda de APK limitada a Downloads y carpetas elegidas por el usuario.
- Terminal con fila adicional ESC / TAB / CTRL / ALT / SHIFT / flechas.
- Identidad visual y paquete separados del upstream.
- CI específico para `assembleJoselofariasDebug`.

## Compilar

```bash
git clone https://github.com/joselofarias-byte/Inure.git
cd Inure
./gradlew :app:assembleJoselofariasDebug
```

También se incluye `build-joselofarias.sh` para el flujo Termux/Proot usado por este fork.

## Estado

El mantenimiento toma upstream como base y reaplica únicamente el delta propio verificable, evitando conservar código comercial o divergencias sin utilidad.

## Origen y licencia

Proyecto original: **Inure App Manager**, por **Hamza417**. Consulte `LICENSE`, `FORK_NOTICE.md` y el historial Git para atribución y cambios.

---
**Mantenimiento del fork:** JoseloFarias

# Inure JoseloFarias fork notice

Upstream project: Inure App Manager, build107.2.4
Upstream license: GNU General Public License version 3
Modified flavor: `joselofarias`
Application ID: `app.simple.inure.joselofarias`
Display name: `Inure JoseloFarias`

## Functional changes

- The fork is rebuilt on upstream build107.2.4.
- The independent `joselofarias` flavor reuses the upstream `src/github` FOSS feature surface without duplicating it.
- Commercial trial, purchase, Unlocker and external licence verification paths are removed from the fork build.
- Full-version feature gates resolve as enabled for this independent FOSS flavor.
- The upstream Play flavor and `src/play` source tree are removed.
- APK discovery is limited to Downloads plus user-selected folders.
- The terminal adds an on-screen extra-key row.
- The launcher/package identity and fork-owned visual resources remain distinct.

Redistribution of binaries or modified source must comply with GPLv3, include corresponding source, preserve upstream notices, and identify the build as modified.

# Horse Ranch

**▶ Play now: https://danmcooper-ops.github.io/horse-ranch/** — works on
iPhone, Android and any PC browser, nothing to install.

A standalone 3D horse-riding game written entirely in Java (libGDX). One codebase,
two targets:

- **Desktop app** (macOS/Windows/Linux) via the LWJGL3 backend
- **Browser build** (plays on iPhone, Android and any PC browser) via TeaVM —
  no plugins, just a static web page

Explore a low-poly ranch on horseback, work through the gaits from walk to
gallop, and ride the show-jumping course against the clock. Your best total
time is saved between sessions.

## Controls

| Action | Desktop | Phone / tablet |
|---|---|---|
| Steer | ← / → (or A / D) | joystick left/right |
| Gait up (walk → trot → canter → gallop) | ↑ (or W) | flick joystick up |
| Gait down / stop | ↓ (or S) | flick joystick down |
| Jump | Space | JUMP button |
| Teleport to course start (debug) | T | — |

The touch joystick and JUMP button appear automatically on touch devices
(and on any browser window narrower than ~800dp).

## The jumping course

Ride through the **green flags** east of the spawn trail to start the timer,
then clear the seven gates in order — the **yellow** rail is always your next
gate, cleared gates turn blue. Jumping clean requires being airborne and above
rail height; a grounded or low crossing scores a fault (+4 s). After gate 7,
race back through the flags to stop the clock. Lowest time + penalties wins;
your best is remembered.

## Requirements

- JDK 17 (`brew install openjdk@17` on macOS)
- No other tooling — the Gradle wrapper is checked in

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17   # macOS/Homebrew example
```

## Run the desktop app

```bash
./gradlew lwjgl3:run
```

## Build & serve the browser version

```bash
./gradlew teavm:buildWeb
python3 -m http.server 8087 --directory teavm/build/dist/webapp
```

Open `http://localhost:8087` on your PC, or `http://<your-Mac-LAN-IP>:8087`
from a phone on the same Wi-Fi. The `teavm/build/dist/webapp/` folder is fully
static — it can be hosted as-is on GitHub Pages or any static host.

## Project layout

```
core/    all game code (world, horse model + gait animation, course, HUD, input)
lwjgl3/  desktop launcher (handles macOS -XstartOnFirstThread automatically)
teavm/   web launcher + TeaVM build tool (patches index.html for mobile)
assets/  bitmap font used by the HUD
```

All 3D models are built procedurally in code with libGDX `ModelBuilder` —
there are no external art assets. The horse is a node-hierarchy model whose
legs, neck, tail and rider are animated per-gait (4-beat walk, diagonal-pair
trot, rocking canter/gallop) with pure math.

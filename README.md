# Wallshove

A maze-chase game on a large scrolling maze. Collect embers to light the exit gate, shove sandstone blocks to cut off or flatten the hunters, and reach the gate.

## Play

- **Browser:** https://sypros.github.io/wallshove/ (on Android Chrome: menu → *Add to Home screen* for a full-screen icon)
- **Android app:** download `Wallshove.apk` from the [latest release](../../releases/tag/latest) and install it once (allow "install unknown apps" for your browser).

The app opens the live game from GitHub Pages, so every push here reaches the phone without reinstalling. With no connection it plays the copy bundled in the APK. Reinstall only when the app shell itself changes (`android/`).

## Controls

- Keyboard: arrows or WASD to move, P or Esc to pause.
- Touch: drag anywhere on the maze in the direction you want to walk, or use the pad. Android back button pauses.

## Layout

| Path | What |
| --- | --- |
| `game/wallshove.html` | The whole game: one HTML fragment with its CSS and JS. Edit this. |
| `web/` | Web app manifest, offline service worker and icons. |
| `tools/build_web.py` | Wraps the game into `site/index.html` and copies it into the APK assets. |
| `android/` | The WebView shell app. |
| `.github/workflows/build.yml` | On every push: publish `site/` to the `gh-pages` branch and the APK to the `latest` release. |

Local preview: `python3 tools/build_web.py && python3 -m http.server -d site`.

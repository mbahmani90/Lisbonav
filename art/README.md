# App icon

`app-icon.svg` is the source of the app icon: the map's bus marker (`BusMarkerStyle`) on a
108×108 adaptive-icon canvas, showing the visible inner 72×72.

- **Android:** `androidApp/src/main/res/drawable/ic_launcher_foreground.xml` and
  `ic_launcher_background.xml` (adaptive icon, also used as the monochrome themed icon).
  The `mipmap-*/ic_launcher*.png` files are fallbacks for API 24–25.
- **iOS:** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png` (no transparency).

Regenerate the PNGs after changing the SVG (needs `rsvg-convert`, e.g. `brew install librsvg`):

```bash
rsvg-convert -w 1024 -h 1024 -b "#FFDD00" art/app-icon.svg \
  -o iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png
```

The legacy Android PNGs use the same drawing clipped to a rounded square (`ic_launcher`)
or a circle (`ic_launcher_round`) at 48/72/96/144/192 px.

# Diagrams

- `nfc-calypso-architecture.svg`: the Navegante card reader (app layers, `:calypso-nfc` SDK,
  APDUs over NFC, build steps). Shown in the main README.

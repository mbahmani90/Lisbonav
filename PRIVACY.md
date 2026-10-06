# Lisbonav privacy policy

Effective date: 6 October 2026

Lisbonav is an independent app by Majid Bahmani that shows Carris Metropolitana buses on a map and
reads Lisbon transit cards over NFC. It has no accounts, no ads and doesn't ask for your location.
This page explains what data the app handles and why.

## Data the app never collects

- **Your transit card.** The card is read on your phone over NFC and shown on screen only. Its
  number, holder data, passes, balance and trips are not stored by the app and are never sent
  anywhere. The app never writes to the card.
- **Your location.** The app doesn't request location permission. The map shows buses, not you.
- **What you type in the search bar.** Line searches stay on the phone; at most the fact that a
  line was found or not is counted (see below).

## Usage statistics (only if you allow them)

On first launch the app asks whether it may share usage statistics. Until you tap **Allow**,
nothing is collected. If you tap **Don't allow**, nothing is collected.

If you allow it, the app uses **Google Analytics for Firebase** (Google LLC / Google Ireland Ltd.)
to count how the app is used:

| What | Example |
|---|---|
| Screens opened | "map", "card" |
| Feature outcomes | a line search found buses or not; a card read succeeded or why it failed (e.g. NFC off); the bus map couldn't load (no connection or service error) |
| Technical data collected by Firebase | an app instance ID created for this installation, device model, OS version, app version, language, approximate country/city derived by Google from the network address, session times |

- No advertising ID is collected and no data is used for ads or ad personalisation.
- The data is pseudonymous: it isn't linked to your name, email or card.
- It is used only to understand which features are used and what goes wrong, to improve the app.
- Google processes it on behalf of the developer; see
  [How Google uses information from apps](https://policies.google.com/technologies/partner-sites)
  and [Firebase privacy](https://firebase.google.com/support/privacy).
- Retention: Google Analytics keeps event data for at most 14 months (the property's data
  retention setting), then deletes it.

**Changing your answer:** the app doesn't have a settings screen yet. To be asked again, clear the
app's data (Android: Settings → Apps → Lisbonav → Storage → Clear data) or reinstall it, then choose
again. To have collected data deleted, contact the developer (below).

## Services the app talks to

- **Carris Metropolitana open data API** (`api.carrismetropolitana.pt`) for bus positions.
- **Google Maps** (Android) or **Apple Maps** (iOS) for the map tiles.

Like any internet request, these services receive your network address and standard request
information under their own privacy policies. The app sends them no personal data.

## Children

The app isn't directed at children under 13 and doesn't knowingly collect their data.

## Changes and contact

Changes to this policy are published on this page with a new effective date.

Questions or deletion requests: open an issue at
[github.com/mbahmani90/Lisbonav/issues](https://github.com/mbahmani90/Lisbonav/issues), or use the
developer email shown on the app's Google Play page.

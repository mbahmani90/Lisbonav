# Google Play: listing and forms

Everything the Play Console asks for when publishing Lisbonav, kept in the repo so the store page and
the code stay in sync. Pasted into the console by hand; the release workflow only uploads the app.

## Store listing (Grow users → Store presence → Main store listing)

| Field | Source | Limit |
|---|---|---|
| App name | [`en-US/title.txt`](en-US/title.txt) | 30 |
| Short description | [`en-US/short-description.txt`](en-US/short-description.txt) | 80 |
| Full description | [`en-US/full-description.txt`](en-US/full-description.txt) | 4000 |
| App icon | [`art/play/icon-512.png`](../../art/play/icon-512.png) (512 × 512, no alpha) | |
| Feature graphic | [`art/play/feature-graphic.jpg`](../../art/play/feature-graphic.jpg) (1024 × 500, from `feature-graphic.svg`) | |
| Phone screenshots | [`art/play/screenshots/`](../../art/play/screenshots/) (status and navigation bars cropped: Play allows at most 2:1) | 2–8 |
| Category | Maps & Navigation | |
| Contact email | the developer email (shown publicly) | |
| Privacy policy | `https://github.com/mbahmani90/Lisbonav/blob/main/PRIVACY.md` | |

Release notes for each upload: [`distribution/whatsnew/whatsnew-en-US`](../whatsnew/whatsnew-en-US)
(≤ 500 characters), read by the release workflow.

## App content (Policy and programs → App content)

| Declaration | Answer |
|---|---|
| Privacy policy | URL above |
| Ads | No ads |
| App access | All functionality is available without special access (no login) |
| Content rating | IARC questionnaire: category *Reference, News, or Educational* / utility; no violence, sexual content, language, controlled substances, gambling; no user-generated content or chat; no purchases |
| Target audience | 18 and over (not designed for children) |
| News app | No |
| Government app | No |
| Financial features | None |
| Health | No |
| Advertising ID | No: the app doesn't use it (`AD_ID` permission removed in the manifest) |

## Data safety

**Does the app collect or share user data?** Yes (usage statistics, after opt-in).
**Encrypted in transit?** Yes. **Can users request deletion?** Yes (contact in `PRIVACY.md`).

| Data type | Collected | Shared | Optional | Purposes |
|---|---|---|---|---|
| App activity → **App interactions** (screens, feature events) | Yes | No | **Yes** (consent dialog) | Analytics |
| Device or other IDs (Firebase app instance ID) | Yes | No | **Yes** | Analytics |
| Location | No | | | the app has no location permission; Google derives an approximate area from the network address, which Play counts as part of the analytics above |
| Personal info, financial info, messages, photos, contacts | No | | | |

- "Shared" means sent to a third party for its own use. Google Analytics for Firebase processes the
  data for the developer (service provider), which Play doesn't count as sharing.
- **Card data isn't "collected"**: it is read and shown on the device only and never leaves it.
- Check Firebase's current guidance for this form before submitting:
  <https://firebase.google.com/docs/android/play-data-disclosure>.

## Notes

- **Not affiliated:** the description ends with the disclaimer; keep it. Don't use Carris
  Metropolitana, TML or Navegante logos in the screenshots or graphics.
- **Maps key:** after the first upload, add the **app signing key SHA-1** (Test and release → App
  integrity → App signing) to the Maps key's Android restrictions, or the map is grey for Play users.
- Full walk-through, tracks, testing requirement and automation: playbook doc 38.

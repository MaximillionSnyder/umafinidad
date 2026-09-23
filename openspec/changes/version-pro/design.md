## Context

Uma Afinidad is an offline Android app (Kotlin + Compose) with no accounts, no network permission and no analytics. It ships as a free APK/AAB from GitHub Releases and CI builds it on every push. Any monetization scheme has to survive that: no Play Billing dependency (the app is not distributed through Play), no server to validate against, and no way to break the free experience.

## Goals / Non-Goals

**Goals**

- Gate exactly three existing features behind a Pro license, with the free app otherwise untouched.
- Activate offline, in one field, with immediate and clear feedback.
- Keep the code path testable in JVM unit tests (pure logic, no Android framework) and reproducible by a script the maintainer runs to issue codes.
- Keep all 9 locales complete.

**Non-Goals**

- Preventing determined piracy (an APK can always be patched), device limits, subscriptions, refunds, accounts or telemetry.
- Changing the free tier, adding ads, or gating anything that exists today outside the three named features.

## Decisions

### License format: `UMA-XXXX-XXXX-XXXX` with a checksum, not a signature

The body is 8 characters from a 32-symbol alphabet without ambiguous glyphs (`23456789ABCDEFGHJKLMNPQRSTUVWXYZ`, so no `0/O`, `1/I/L`). The last 4 characters are a 20-bit checksum of the body: FNV-1a 32-bit followed by the murmur3 finalizer, then 4 groups of 5 bits mapped into the same alphabet.

- *Why not asymmetric signatures*: they would need a key pair, a crypto dependency and a much longer code for a hobby app whose APK is public anyway.
- *Why not Play Billing*: the app is distributed from GitHub Releases; adding Play Services would break the offline/zero-dependency promise and cannot be exercised in this environment.
- *Consequence*: ~1 in a million random codes validates. Accepted: a valid code is a "thank you" token, not a security boundary.
- *Why the alphabet excludes ambiguous characters*: codes get dictated, copied from a chat or typed on a phone; excluding `0/O` and `1/I/L` removes the most common transcription errors, and `normalizar()` additionally tolerates lowercase, spaces and missing dashes.

### Single source of truth for the gated features

`enum class FuncionPro { BURBUJA_REDIMENSIONAR, RANKING_PADRES, ARBOLES_GUARDADOS }` with `requierePro = true` and `funcionDisponible(funcion, esPro)` is the only list of what Pro unlocks. The Pro screen, the Ajustes locks and the overlay read from it, so a future feature that becomes free only changes one flag.

### Gate at the UI *and* at the action

Each gate is enforced twice: the UI stops offering the action (no handle, locked mode, lock on the save button) and the action itself refuses (`AppViewModel.guardarArbol` returns `false`, `BurbujaService.redimensionarPanel`/`guardarTamanoPanel` return early, the ranking skips `modelo.rankingPadres()`). The second check costs nothing and keeps a stale composition from writing Pro-only state.

Skipping the `rankingPadres()` computation for free users is also a performance win: it is the heaviest calculation in the app and it now only runs for licensed users.

### Stored manual panel size is ignored without a license

`prefs.panelAnchoDp/panelAltoDp` are only honored when `pro.esPro` is true, so a device that had a resized panel before deactivating Pro falls back to the automatic fraction instead of keeping a Pro-only layout. The stored values are not erased: reactivating restores the previous size.

### Already-saved lineages stay readable

Saving is Pro; reading and deleting are not. Grandfathering existing data avoids both a migration and the feeling that the app took something away.

### Deactivation is allowed and reversible

The Pro screen can deactivate the license on the device (useful to test the free tier). The same code reactivates it, and `activar()` returns `YA_ACTIVO` when it is already on.

## Migration Plan

No migration: a fresh install is free, existing installs keep their saved lineages and simply see the locks. The `pro_estado` preferences file is new and independent from `ui_prefs` and `arboles_guardados`.

## Open Questions

- Whether to publish the codes through a store (Ko-fi/Gumroad) or hand them out per contribution — the Pro screen's "how do I get a code?" dialog points at the repository, so this can be decided later without a code change.

# CarBridge Patches

Patches that put an app's full interface on the Android Auto car screen while parked.
A patch bundle for use with [Morphe](https://github.com/MorpheApp) Manager, made by the
[CarBridge](https://github.com/jmwerk/CarBridge) project. Not affiliated with or endorsed by Morphe.

Unlike the CarBridge app, these patches keep the app's package name, so they work together with
the other patches you apply (for example, branding and icon changes keep working).

## How to use these patches

1. In Morphe Manager, add this patch source: tap the button below, or add the URL
   `https://github.com/jmwerk/carbridge-patches` under **Patch sources**.

   <a href="https://morphe.software/add-source?github=jmwerk/carbridge-patches">
     <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe Manager" height="48">
   </a>

2. Patch an app as usual and enable **Show in Android Auto** (it is off by default).
3. Install the patched app. Android Auto lists sideloaded apps only with developer mode on:
   Android Auto settings → tap *Version* 10× → ⋮ → Developer settings → enable **Unknown sources**.

## Patches list

<!-- PATCHES_START EXPANDED -->

### Universal (any app)

- **Show in Android Auto**: lists the app in the Android Auto launcher and shows its full
  interface on the car screen while parked. It changes only `AndroidManifest.xml` and adds
  `res/xml/carbridge_automotive_app_desc.xml`:
  - `<application>`: `android:appCategory="game"`, meta-data `distractionOptimized=true`,
    `androidx.car.app.minCarApiLevel=7` and, if missing, `com.google.android.gms.car.application`
  - permissions `androidx.car.app.ACCESS_SURFACE` and `androidx.car.app.MAP_TEMPLATES`
  - every MAIN/LAUNCHER activity and activity-alias: categories `CAR_LAUNCHER`,
    `androidx.car.app.category.NAVIGATION` and `APP_MAPS`, meta-data `distractionOptimized=true`,
    `android:resizeableActivity="true"`
  - removes media-browser and car-app-service intent filters, so Android Auto shows the app
    instead of its template UI

<!-- PATCHES_END -->

## Building locally

The Gradle plugin comes from GitHub Packages, so Gradle needs a GitHub token with the
`read:packages` scope, as `gpr.user`/`gpr.key` in `~/.gradle/gradle.properties` or as
`GITHUB_ACTOR`/`GITHUB_TOKEN`:

```bash
GITHUB_ACTOR=<user> GITHUB_TOKEN=<token> ./gradlew buildAndroid
```

The bundle is written to `patches/build/libs/patches-*.mpp`. Apply it with
[Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) or add it as a local source in
Morphe Manager.

## Releasing

Releases are made by `.github/workflows/release.yml` (semantic-release) from conventional commits:
`feat:` and `fix:` on `dev` make pre-releases, merging `dev` into `main` makes a stable release.
Do not create releases or edit `patches-list.json`, `patches-bundle.json` or `CHANGELOG.md` by hand.

## License

CarBridge Patches are licensed under the [GNU General Public License v3.0](LICENSE).
"Morphe" is used only to describe compatibility; see [NOTICE](NOTICE).

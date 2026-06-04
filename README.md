# Simplest Notes

<img src="art/logo.svg" width="96" alt="Simplest Notes logo">

A single text box. Open it, write, leave. Whatever is there is what you'll see next time.

## Design

- One `EditText` filling the screen; no buttons, no list, no titles.
- The text is stored in `SharedPreferences` (`notes`, key `text`) and written with `commit()` in
  `onPause()`, so it is on disk before the process can be torn down.
- Built in code with framework widgets only (no layout inflation, no support libraries) for a fast
  cold start.

## Build & install

Needs [`just`](https://github.com/casey/just) and `adb`.

```sh
just android        # build the release APK and install it on every connected phone
```

The release build is signed with the debug key so it installs as-is
(`app/build/outputs/apk/release/app-release.apk`).

## Releases

Pushing a tag starting with `v` (for example `v1.2`) builds the release APK on GitHub Actions and
publishes it as an asset on that tag's release.

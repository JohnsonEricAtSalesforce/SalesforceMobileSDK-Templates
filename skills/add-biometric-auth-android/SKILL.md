---
name: add-biometric-auth-android
description: Add biometric authentication (fingerprint / face / iris) to an existing Android Kotlin app that already has Salesforce Mobile SDK integrated. Prompts the user to opt in after login and presents the lock screen on return from background.
---

# Add Biometric Authentication to an Android Kotlin App

This skill adds Salesforce Mobile SDK biometric authentication to an existing Android Kotlin app that already has the Mobile SDK integrated via the `add-mobile-sdk-android` skill.

## What This Skill Does

1. Adds the `androidx.biometric` dependency to `app/build.gradle.kts`
2. Adds a post-login opt-in prompt to `MainActivity.kt` via `onPostResume()`

The SDK handles all locking, unlocking, and the native biometric prompt (fingerprint / face / iris) automatically once the user opts in. No additional lifecycle code is needed — the SDK wires foreground/background transitions internally when `MobileSyncSDKManager.initNative()` (or a subclass manager) is called.

## What the SDK Does Automatically

- Locks the app after the admin-configured inactivity timeout
- Presents the OS `BiometricPrompt` when the app returns from background while locked
- Falls back to username/password on the Salesforce login screen if biometric fails
- Shows a native "Use Biometrics" button on the login screen (can be suppressed via `enableNativeBiometricLoginButton(false)`)

## Prerequisites

The app must already have the Mobile SDK integrated. **Check `MainApplication.kt` for `MobileSyncSDKManager.initNative()` (or `SmartStoreSDKManager`) and that `bootconfig.xml` exists.** If not, run `add-mobile-sdk-android` first.

The `BiometricAuthenticationManager.enabled` property is `true` only when the Salesforce org's connected app has `ENABLE_BIOMETRIC_AUTHENTICATION` set. In development you may wire the code and test the build — the opt-in dialog will silently not appear if the feature is not configured in the org.

---

## Step 1: Add the androidx.biometric Dependency

In `app/build.gradle.kts`, add the AndroidX Biometric library. This is needed to call `BiometricManager.canAuthenticate()` to check device capability before prompting.

```kotlin
dependencies {
    implementation("com.salesforce.mobilesdk:MobileSync:13.2.0")
    implementation("androidx.biometric:biometric:1.1.0")
}
```

Sync Gradle after editing.

---

## Step 2: Update MainActivity.kt

Add `onPostResume()` to check device biometric capability and present the SDK opt-in dialog if needed.

**Add these imports** (at the top of `MainActivity.kt`):

```kotlin
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
```

**Add `onPostResume()`** (after `onResume(client: RestClient?)`):

```kotlin
override fun onPostResume() {
    super.onPostResume()

    val deviceHasBiometrics = BiometricManager.from(this).canAuthenticate(
        BIOMETRIC_STRONG or BIOMETRIC_WEAK
    ) == BiometricManager.BIOMETRIC_SUCCESS

    MobileSyncSDKManager.getInstance().biometricAuthenticationManager?.run {
        if (enabled && deviceHasBiometrics && !hasBiometricOptedIn()) {
            presentOptInDialog(supportFragmentManager)
        }
    }
}
```

> **`onPostResume()`** fires after `onResume()` and after the activity window is fully visible — the correct lifecycle point to present a dialog. Using `onResume()` or `onResume(client)` can cause the dialog to appear before the window is attached, leading to a crash.

> **`biometricAuthenticationManager`** is a nullable property on `SalesforceSDKManager` (and all subclasses). It is non-null after a user is authenticated. The `?.run { }` block safely no-ops when no user is logged in.

> **`supportFragmentManager`** requires the activity to extend `FragmentActivity` (which `SalesforceActivity` does). If your activity extends plain `Activity`, switch to `AppCompatActivity` or `SalesforceActivity`.

---

## Step 3: Replace SmartStoreSDKManager reference if needed

If your app uses `SmartStoreSDKManager` instead of `MobileSyncSDKManager`, use the same accessor — it is inherited from `SalesforceSDKManager`:

```kotlin
import com.salesforce.androidsdk.smartstore.app.SmartStoreSDKManager

SmartStoreSDKManager.getInstance().biometricAuthenticationManager?.run {
    if (enabled && deviceHasBiometrics && !hasBiometricOptedIn()) {
        presentOptInDialog(supportFragmentManager)
    }
}
```

---

## Step 4: Build and Verify

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

### Runtime verification (emulator or device)

1. Launch the app — Salesforce login screen appears.
2. Log in — the SDK opt-in dialog appears: **"Use biometrics to unlock?"** with Enable / Use Password buttons.
3. Tap **Enable** — biometric authentication is now active for this user.
4. Background the app, wait for the configured timeout (or call `MobileSyncSDKManager.getInstance().biometricAuthenticationManager?.lock()` to force-lock), then foreground — the OS `BiometricPrompt` appears.
5. Authenticate successfully — the app unlocks without re-entering credentials.

> **Emulator note:** Fingerprint can be enrolled in the emulator via **Extended controls → Fingerprint**. Use `adb -e emu finger touch 1` to simulate a fingerprint scan. The opt-in dialog will still appear even without enrolled biometrics — only the actual scan step requires enrollment.

---

## Checklist

- [ ] `androidx.biometric:biometric:1.1.0` added to `app/build.gradle.kts`
- [ ] `BiometricManager` imports added to `MainActivity.kt`
- [ ] `onPostResume()` added with device capability check and `presentOptInDialog()` call
- [ ] Project builds without errors
- [ ] Opt-in dialog appears after first login (requires `ENABLE_BIOMETRIC_AUTHENTICATION` on connected app, or feature is enabled)
- [ ] Biometric prompt appears after app returns from background (enrolled emulator / device)

---

## Optional: Suppress the Native Login Screen Button

By default the SDK shows a **Use Biometrics** button on the Salesforce login screen. To hide it:

```kotlin
MobileSyncSDKManager.getInstance().biometricAuthenticationManager
    ?.enableNativeBiometricLoginButton(false)
```

Call this once after `initNative()` in `MainApplication.onCreate()`.

---

## Troubleshooting

**Opt-in dialog never appears**
`enabled` is `false` — the connected app in your org does not have `ENABLE_BIOMETRIC_AUTHENTICATION` set. The dialog correctly does not appear. Set the custom attribute on the connected app or temporarily remove the `enabled &&` check during development.

**`IllegalStateException`: Can not perform this action after onSaveInstanceState**
The dialog is being presented too early in the lifecycle. Ensure you are calling `presentOptInDialog` from `onPostResume()`, not `onResume()`.

**`Unresolved reference: BiometricManager`**
The `androidx.biometric:biometric` dependency is missing or Gradle sync didn't complete.

**Biometric prompt does not appear after backgrounding**
The inactivity timeout has not elapsed. Call `MobileSyncSDKManager.getInstance().biometricAuthenticationManager?.lock()` to force-lock immediately for testing.

**`biometricAuthenticationManager` is null**
No user is currently authenticated. This is expected before login. The `?.run { }` safe-call handles this correctly.

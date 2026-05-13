---
name: add-biometric-auth-ios
description: Add biometric authentication (Face ID / Touch ID) to an existing iOS Swift app that already has Salesforce Mobile SDK integrated. Prompts the user to opt in after login and presents the lock screen on return from background.
---

# Add Biometric Authentication to an iOS Swift App

This skill adds Salesforce Mobile SDK biometric authentication to an existing iOS Swift app that already has `SalesforceSDKCore` integrated via the `add-mobile-sdk-ios` skill.

## What This Skill Does

1. Adds `NSFaceIDUsageDescription` to `Info.plist` (required by App Store / iOS for Face ID access)
2. Adds a `LocalAuthentication` import and a post-login opt-in prompt to `SceneDelegate.swift`

The SDK handles all locking, unlocking, and the native biometric prompt automatically once the user opts in. No additional background/foreground lifecycle code is needed in app code — the SDK wires that up internally when `SalesforceManager.initializeSDK()` (or a subclass) is called.

## What the SDK Does Automatically

- Locks the app after the admin-configured inactivity timeout
- Presents the OS biometric prompt (Face ID / Touch ID) when the app returns from background while locked
- Falls back to username/password on the Salesforce login screen if biometric fails
- Shows a native "Use Biometrics" button on the login screen (can be suppressed via `enableNativeBiometricLoginButton(enabled: false)`)

## Prerequisites

The app must already have the Mobile SDK integrated. **Check `AppDelegate.swift` for `SalesforceManager.initializeSDK()` (or `SmartStoreSDKManager` / `MobileSyncSDKManager`) and that `bootconfig.plist` exists.** If not, run `add-mobile-sdk-ios` first.

Before starting, confirm:
- **App target name** (e.g. `MyApp`)

The `BiometricAuthenticationManager.enabled` property is `true` only when the Salesforce org's connected app has `ENABLE_BIOMETRIC_AUTHENTICATION` set. In development you may set `enabled` to always be `true` or just wire the code and test the build — the opt-in dialog will silently do nothing if the feature is not configured in the org.

---

## Step 1: Add NSFaceIDUsageDescription to Info.plist

iOS requires a usage description string before any app can use Face ID. Without it the app crashes on Face ID devices at the moment the biometric prompt is shown.

Add to the app target's `Info.plist`:

```xml
<key>NSFaceIDUsageDescription</key>
<string>Use Face ID to unlock the app.</string>
```

If the project uses `xcodegen` and a `project.yml`, add it under the target's `info.properties` block:

```yaml
NSFaceIDUsageDescription: Use Face ID to unlock the app.
```

Then regenerate: `xcodegen generate`.

---

## Step 2: Update SceneDelegate.swift

Add `LocalAuthentication` import and call `presentOptInDialog` after login inside `setupRootViewController()`.

**Before:**
```swift
import UIKit
import SalesforceSDKCore   // or SmartStore / MobileSync

// ...

func setupRootViewController() {
    // ... your post-login UI setup
}
```

**After:**
```swift
import UIKit
import LocalAuthentication
import SalesforceSDKCore   // or SmartStore / MobileSync — keep whichever is already there

// ...

func setupRootViewController() {
    // Prompt for biometric opt-in after login if the feature is enabled
    // and the user hasn't opted in yet.
    let laContext = LAContext()
    var laError: NSError?
    let deviceHasBiometrics = laContext.canEvaluatePolicy(
        .deviceOwnerAuthenticationWithBiometrics,
        error: &laError
    )

    let bioManager = SalesforceManager.shared.biometricAuthenticationManager()
    if bioManager.enabled && deviceHasBiometrics && !bioManager.hasBiometricOptedIn() {
        if let vc = window?.rootViewController {
            bioManager.presentOptInDialog(viewController: vc)
        }
    }

    // ... your post-login UI setup (keep existing code below this block)
}
```

> **`SalesforceManager.shared.biometricAuthenticationManager()`** returns the biometric authentication manager instance that conforms to the `BiometricAuthenticationManager` protocol. It lives in `SalesforceSDKCore`. No additional import beyond `SalesforceSDKCore` (or its subframework) is needed.

> **When to call `presentOptInDialog`**: Call it every time `setupRootViewController()` runs. The `hasBiometricOptedIn()` guard ensures it only shows once per user. If you already show it and they dismiss without choosing, it will reappear next session — that is correct behaviour.

---

## Step 3: Build and Verify

```bash
# CocoaPods
xcodebuild -workspace <AppName>.xcworkspace \
  -scheme <AppName> \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,id=<SimulatorUDID>' \
  build
```

Expected: `** BUILD SUCCEEDED **`

### Runtime verification (device or simulator)

1. Launch the app — Salesforce login screen appears.
2. Log in — the SDK opt-in dialog appears: **"Use Biometrics to unlock?"** with Enable / Skip buttons.
3. Tap **Enable** — Face ID / Touch ID is now active for this user.
4. Background the app, wait for the configured timeout (or call `BiometricAuthenticationManagerInternal.shared.lock()` to force-lock), then foreground — the OS biometric prompt appears.
5. Authenticate successfully — the app unlocks without re-entering credentials.

> **Simulator note:** Face ID is available in the simulator via **Features → Face ID → Enrolled**. Touch ID is available on older simulators. Biometric prompts can be triggered via **Features → Face ID → Matching Face** (or Trigger Face ID). The opt-in dialog will still appear and can be tapped even on simulators without biometric hardware — only the actual biometric scan step will be skipped.

---

## Checklist

- [ ] `NSFaceIDUsageDescription` added to `Info.plist`
- [ ] `import LocalAuthentication` added to `SceneDelegate.swift`
- [ ] `SalesforceManager.shared.biometricAuthenticationManager()` opt-in check added in `setupRootViewController()`
- [ ] Project builds without errors
- [ ] Opt-in dialog appears after first login
- [ ] Biometric prompt appears after app returns from background (device / enrolled simulator)

---

## Optional: Suppress the Native Login Screen Button

By default the SDK shows a **Use Biometrics** button on the Salesforce login screen. To hide it (e.g. you want to control the UX entirely from your own UI):

```swift
SalesforceManager.shared.biometricAuthenticationManager().enableNativeBiometricLoginButton(enabled: false)
```

Call this once during app startup, before the login screen is shown — `AppDelegate.init()` is a good place.

---

## Troubleshooting

**Build error: `Value of type 'SalesforceSDKManager' has no member 'biometricAuthenticationManager'`**
`SalesforceSDKCore` is not linked or the import is missing. Verify `pod install` completed and you opened `.xcworkspace`.

**Opt-in dialog never appears**
`bioManager.enabled` is `false` — the connected app in your org does not have `ENABLE_BIOMETRIC_AUTHENTICATION` set to `true`. The dialog correctly does not appear. Set the custom attribute on the connected app or temporarily bypass the `enabled` check during development.

**`NSFaceIDUsageDescription` crash**
The key is missing from `Info.plist`. This crash happens at the OS level when Face ID is triggered — add the key as described in Step 1.

**Biometric prompt does not appear after backgrounding**
The inactivity timeout has not elapsed. Either wait for the configured timeout, or call `SalesforceManager.shared.biometricAuthenticationManager().lock()` programmatically to force-lock the app immediately for testing.

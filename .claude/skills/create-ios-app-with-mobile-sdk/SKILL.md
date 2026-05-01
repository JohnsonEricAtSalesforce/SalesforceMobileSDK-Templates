---
name: create-ios-app-with-mobile-sdk
description: Create a new iOS Swift application from scratch using xcodegen, then integrate Salesforce Mobile SDK authentication into it.
---

# Create an iOS App with Salesforce Mobile SDK

This skill creates a new iOS Swift application from scratch using `xcodegen`, then integrates the Salesforce Mobile SDK by invoking the `add-mobile-sdk-ios` skill.

## Prerequisites

Ensure `xcodegen` is installed:

```bash
brew install xcodegen
```

Before starting, ask the user for:
- **App name** (e.g. `MyApp`) — used as the Xcode target name, directory name, and bundle name
- **Bundle ID** (e.g. `com.mycompany.myapp`)
- **Output directory** (where to create the project, e.g. `~/Projects`)
- **Dependency manager**: CocoaPods or Swift Package Manager
- **Consumer Key**, **Callback URL**, **Login host** — passed through to the `add-mobile-sdk-ios` skill

---

## Step 1: Create the Project Directory and Source Files

```bash
mkdir -p <OutputDir>/<AppName>/<AppName>
cd <OutputDir>/<AppName>
```

Create `<AppName>/AppDelegate.swift`:

```swift
import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication,
                     configurationForConnecting connectingSceneSession: UISceneSession,
                     options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication,
                     didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {}

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        return true
    }
}
```

Create `<AppName>/SceneDelegate.swift`:

```swift
import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene,
               willConnectTo session: UISceneSession,
               options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }
        window = UIWindow(frame: windowScene.coordinateSpace.bounds)
        window?.windowScene = windowScene
        window?.rootViewController = UIViewController()
        window?.makeKeyAndVisible()
    }

    func sceneDidDisconnect(_ scene: UIScene) {}
    func sceneDidBecomeActive(_ scene: UIScene) {}
    func sceneWillResignActive(_ scene: UIScene) {}
    func sceneWillEnterForeground(_ scene: UIScene) {}
    func sceneDidEnterBackground(_ scene: UIScene) {}
}
```

---

## Step 2: Create project.yml

Create `project.yml` in the project root (`<OutputDir>/<AppName>/`):

```yaml
name: <AppName>
options:
  bundleIdPrefix: <BundleIdPrefix>   # everything before the last component, e.g. com.mycompany
  deploymentTarget:
    iOS: "18.0"
settings:
  PRODUCT_BUNDLE_IDENTIFIER: <BundleID>
targets:
  <AppName>:
    type: application
    platform: iOS
    sources:
      - <AppName>
    info:
      path: <AppName>/Info.plist
      properties:
        UIApplicationSceneManifest:
          UIApplicationSupportsMultipleScenes: false
          UISceneConfigurations:
            UIWindowSceneSessionRoleApplication:
              - UISceneConfigurationName: Default Configuration
                UISceneDelegateClassName: $(PRODUCT_MODULE_NAME).SceneDelegate
```

> **Note:** `UILaunchStoryboardName` is intentionally omitted. Apps created with this skill use a programmatic window setup and do not require a launch storyboard.

---

## Step 3: Generate the Xcode Project

```bash
cd <OutputDir>/<AppName>
xcodegen generate
```

xcodegen will create `<AppName>.xcodeproj` and `<AppName>/Info.plist`. Verify both are present before continuing.

---

## Step 4: Add Salesforce Mobile SDK

Invoke the `add-mobile-sdk-ios` skill now, passing the values collected in the prerequisites.

The skill will:
- Add the `SalesforceSDKCore` dependency (CocoaPods or SPM)
- Update `AppDelegate.swift` with `SalesforceManager.initializeSDK()`
- Update `SceneDelegate.swift` with `AuthHelper.loginIfRequired` and user-change notifications
- Create `bootconfig.plist`
- Add `SFDCOAuthLoginHost` to `Info.plist`

For the CocoaPods path, after the skill creates the `Podfile`, run:

```bash
pod install
```

Then open `<AppName>.xcworkspace` (not `.xcodeproj`).

For the SPM path, the skill gives instructions to add the package in Xcode — no extra command needed.

---

## Step 5: Build and Verify

```bash
# CocoaPods
xcodebuild -workspace <AppName>.xcworkspace \
  -scheme <AppName> \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' \
  build

# SPM (no workspace)
xcodebuild -project <AppName>.xcodeproj \
  -scheme <AppName> \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' \
  build
```

Expected: `** BUILD SUCCEEDED **`

Run on the simulator — the Salesforce login screen should appear on first launch.

---

## Checklist

- [ ] Project directory created with `<AppName>` subdirectory for sources
- [ ] `AppDelegate.swift` and `SceneDelegate.swift` created
- [ ] `project.yml` created with correct bundle ID and deployment target
- [ ] `xcodegen generate` completed without errors
- [ ] `add-mobile-sdk-ios` skill applied
- [ ] `pod install` run (CocoaPods path) or SPM package added in Xcode (SPM path)
- [ ] Project builds without errors
- [ ] Salesforce login screen appears on first launch

---

## Troubleshooting

**`xcodegen: command not found`**
Run `brew install xcodegen`.

**`xcodegen generate` fails with "No project spec found"**
Ensure `project.yml` is in the current directory when running the command.

**Build fails after `pod install`**
Open `.xcworkspace`, not `.xcodeproj`. CocoaPods requires the workspace.

For all other issues, refer to the troubleshooting section of the `add-mobile-sdk-ios` skill.

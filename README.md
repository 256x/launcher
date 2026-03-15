# Literal Launcher

A text-based launcher.

<p align="center">
  <img src="https://github.com/user-attachments/assets/3759bf42-1032-4588-a52e-f55dd4f8bb91" width="180" />
  <img src="https://github.com/user-attachments/assets/276bf507-6d17-4ac2-821e-b7befc1674f1" width="180" />
  <img src="https://github.com/user-attachments/assets/2cdc0876-c3fe-40ca-ae5f-70e1ee792f32" width="180" />
  <img src="https://github.com/user-attachments/assets/0131ca25-8a78-46bf-b771-c86913dad800" width="180" />
</p>

[User Guide](./docs/USER_GUIDE.md)


## Features
- **App Slots**: Assign apps to 10 customizable screen positions.
- **Customization**: Change font (built-in Scope One support) and UI scale.
- **App Chest**: Hide rarely used apps from the main list.
- **Renaming**: Custom display names for any app.
- **Gestures**: Double-tap for quick action, swipe for drawer.

## Why no search?
Literal Launcher is designed for intentionality, not convenience.
A search bar encourages mindless app-opening; Literal Launcher doesn't.
Instead, use the Renaming feature to curate your own priority list —
prefix an app with 'a_' to bring it to the top, or 'z_' to push it down.
It's a small friction that makes you more deliberate about what you launch.

## The "Chest": Not for Hiding, but for Archiving
Most launchers have a "Hide Apps" feature — but to use a hidden app,
you have to dig into settings just to unhide it first.

Literal Launcher introduces the **Chest**: a secondary space for your "Tier 2" apps.

- **Direct Launch**: Apps in the Chest are launchable directly, without moving them back.
- **Zero Clutter**: Your main list stays strictly for "Tier 1" essentials.
- **The No-Search Logic**: Curate your main list with Renaming, archive the rest in the Chest,
  and your active app count stays low. When your environment is this organized,
  a search bar becomes redundant.

Literal Launcher isn't about finding apps faster. It's about needing to find them less.

## Credits
- **Inspired by [μ launcher](https://github.com/jrpie/launcher) by jrpie.**
  As a huge fan of μ's philosophy, I wanted to build my own version from scratch using Jetpack Compose. 
  Literal Launcher was born out of my personal need for integrated **Date and Battery displays**—features I felt were missing—and my desire to challenge myself by crafting the ultimate minimalist environment.

## Development

Developed in Android Studio with Gemini as a reference throughout the process — all design decisions and the overall concept are my own.


## Build
- Kotlin / Jetpack Compose
- Target: Android 8.0+


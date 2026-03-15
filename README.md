# Literal Launcher

A minimalist text-based Android launcher.

https://github.com/user-attachments/assets/67d867d7-9682-4604-bb5b-9173ec510364

<p align="center">
  <img src="https://github.com/user-attachments/assets/3759bf42-1032-4588-a52e-f55dd4f8bb91" width="180" />
  <img src="https://github.com/user-attachments/assets/276bf507-6d17-4ac2-821e-b7befc1674f1" width="180" />
  <img src="https://github.com/user-attachments/assets/2cdc0876-c3fe-40ca-ae5f-70e1ee792f32" width="180" />
  <img src="https://github.com/user-attachments/assets/0131ca25-8a78-46bf-b771-c86913dad800" width="180" />
</p>



[User Guide](./docs/USER_GUIDE.md)


## Features
- **App Slots**: Assign apps to 10 fixed launch positions on the screen.
- **Customization**: Change font (built-in Scope One support) and UI scale.
- **App Chest**: Archive rarely used apps in a secondary, directly-launchable space.
- **Renaming**: Custom display names for any app.

## Why no search?
Literal Launcher is designed for intentionality, not convenience.
A search bar encourages mindless app-opening; Literal Launcher doesn't.
Instead, use the Renaming feature to curate your own priority list —
prefix an app with 'a_' to bring it to the top, or 'z_' to push it down.
It's a small amount of friction that makes you more deliberate about what you launch.
## Why gestures are minimal?
Gestures require muscle memory and are often hidden. Literal Launcher is built 
on a single principle: **Tap what you see**.
Every element on your screen is a direct target — the clock, the date, the 
battery level, and six positional slots. That's 10 launch targets, always one tap away — with zero learning curve.
The bottom center opens your notification panel.
No complex swipes. No hidden shortcuts. Just intentional taps.

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

## Who is this for?

This launcher may appeal to people who:
- want a distraction-free phone
- prefer fixed tap targets over search
- like minimalist interfaces

## Credits
- **Inspired by [μ launcher](https://github.com/jrpie/launcher) by jrpie.**
  As a huge fan of μ's philosophy, I wanted to build my own version from scratch using Jetpack Compose. 
  Literal Launcher was born out of my personal need for integrated **Date and Battery displays**—features I felt were missing—and my desire to challenge myself by crafting the ultimate minimalist environment.

## Development

Built with Kotlin and Jetpack Compose, developed in Android Studio with Gemini as a reference throughout the process.

## Build
- Kotlin / Jetpack Compose
- Target: Android 8.0+


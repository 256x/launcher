# Literal Launcher

Life within five meters.

A minimalist text-based Android launcher.

<p>
  <a href="https://github.com/256x/launcher/releases/latest"><img src="https://img.shields.io/github/v/release/256x/launcher?label=GitHub%20Release"></a>&nbsp;<a href="https://apt.izzysoft.de/packages/fumi.day.literallauncher"><img src="https://img.shields.io/badge/IzzyOnDroid-download-brightgreen"></a>&nbsp;<img src="https://img.shields.io/badge/Android-9%2B-blue">&nbsp;<img src="https://img.shields.io/badge/license-MIT-lightgrey">
</p>

<p>
<img width="180" alt="Home Screen" src="https://github.com/user-attachments/assets/5ec650ee-a9e6-411e-89ad-eae68a83c2b4" />
<img width="180" alt="Apps Screen" src="https://github.com/user-attachments/assets/4eecf4d1-c967-4947-835f-39ebd1e55aa2" />
<img width="180" alt="Chest Screen" src="https://github.com/user-attachments/assets/028113b5-a9bc-44c1-aac9-cf063efca5db" />
<img width="180" alt="Settings Screen" src="https://github.com/user-attachments/assets/d65ed335-c826-445d-b4d9-a85c0a660670" />
</p>

[User Guide](./docs/USER_GUIDE.md)

## Why?

Humans can hold roughly 10 things in working memory.

Most launchers ignore this. They give you grids of icons, folders, widgets, search bars — and call it productivity.

But if your phone has 10 apps you actually use every day, you don't need any of that.

You just need those 10, always one tap away.

## The Idea

**Assign your essentials to slots. Everything else fades into the background.**

10 fixed positions. No search. No icons. Just text.

Icons make your eyes wander. Text makes you read and decide.
Swipe up reveals your full app list — your Tier 1, alphabetically sorted.
Rarely-used apps go into the Chest: out of sight, but still directly launchable.

## Features

- **App Slots**: Assign apps to 10 fixed launch positions on the screen
- **Text-only list**: No icons. App names only, in your choice of font
- **Chest**: A secondary space for Tier 2 apps — hidden but directly launchable
- **Renaming**: Custom display names for any app
- **Customize**: Font, UI scale, drawer alignment for left-hand use

## Why no icons?

Icons cause visual noise. Your eyes scan the grid looking for the right shape instead of reading what you want.

Text forces a moment of intention. That's a feature, not a limitation.

## Why no search?

If you need to search for an app, it means your list is too long.

Use Renaming to curate your Tier 1 list — prefix with `a_` to bring an app to the top, `z_` to push it down. Keep it short enough that search becomes unnecessary.

## The Chest: not hidden, just backgrounded

Most launchers let you hide apps — but hidden apps require a trip to settings to unhide before you can launch them.

The Chest works differently. Apps in the Chest are removed from your main list, but remain **directly launchable** from the Chest at any time. No restore step needed.

Use it for apps you open once a month. They stay out of your way without becoming inaccessible.

## Who is this for?

This launcher may appeal to people who:

- want a distraction-free phone
- prefer fixed tap targets over search
- like minimalist interfaces
- find icon grids visually overwhelming

## Philosophy

Literal Launcher is not a launcher that does less.

It's a different model of what a phone's home screen should be.

If a feature requires learning gestures or browsing menus, it probably doesn't belong here.

## Credits

Inspired by [μ launcher](https://github.com/jrpie/launcher) by jrpie — a launcher built around intentional, gesture-based navigation. Literal Launcher takes a different approach: tap targets instead of gestures, text instead of icons, and date/battery display built in.

## Development

- Kotlin / Jetpack Compose
- Target: Android 9.0+
- No Google APIs. No tracking.

## License

MIT

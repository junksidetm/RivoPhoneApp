<div align="center">

<img src="https://raw.githubusercontent.com/user-grinch/RivoPhoneApp/main/app/src/main/res/drawable/logo.png" width="96" height="96" alt="Rivo Logo">

# Rivo

A clean, open-source dialer and contacts app for Android, built with Jetpack Compose.

[![GitHub Main](https://img.shields.io/badge/GitHub-Main-181717?style=flat-square&logo=github&logoColor=white)](https://github.com/junksidetm/RivoPhoneApp)
[![Codeberg Mirror](https://img.shields.io/badge/Codeberg-Mirror-2185d0?style=flat-square&logo=codeberg&logoColor=white)](https://codeberg.org/mrdarksidetm/RivoPhoneApp)
[![GitLab Mirror](https://img.shields.io/badge/GitLab-Mirror-fc6d26?style=flat-square&logo=gitlab&logoColor=white)](https://gitlab.com/mrdarksidetm/RivoPhoneApp)
[![Upstream Source](https://img.shields.io/badge/Upstream-user--grinch%2FRivoPhoneApp-blue?style=flat-square&logo=github)](https://github.com/user-grinch/RivoPhoneApp)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-2563EB.svg?style=flat-square)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android-10B981.svg?style=flat-square&logo=android)](https://www.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-6366F1.svg?style=flat-square)](https://developer.android.com/jetpack/compose)
[![Crowdin](https://img.shields.io/badge/Localization-Crowdin-0EA5E9?logo=crowdin&style=flat-square)](https://crowdin.com/project/rivophone)

<br>

<div align="center">
  <a href="https://github.com/junksidetm/RivoPhoneApp/releases/download/v2.2.412/RivoPhone-v2.2.412.apk" target="_blank" rel="noopener noreferrer">
    <img src="https://raw.githubusercontent.com/junksidetm/assests/d8774837b8c8658389ea37193a77a9a100414bc5/Images/badges/SVG%20-%20Version/Android%20Direct%20Link%20Frame.svg" alt="Direct Link" width="210">
  </a>
</div>

<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/{%22id%22:%22com.mrdarksidetm.rivo%22,%22url%22:%22https://github.com/junksidetm/RivoPhoneApp%22,%22author%22:%22junksidetm%22,%22name%22:%22RivoPhoneApp%22}">
  <img src="https://raw.githubusercontent.com/ImranR98/Obtainium/b1c8ac6f2ab08497189721a788a5763e28ff64cd/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="40">
</a>
&nbsp;
<a href="https://github.com/junksidetm/RivoPhoneApp/releases">
  <img src="https://user-images.githubusercontent.com/69304392/148696068-0cfea65d-b18f-4685-82b5-329a330b1c0d.png" alt="Download APK from GitHub" height="40">
</a>

<br>

[Releases](https://github.com/junksidetm/RivoPhoneApp/releases) &bull; [GitLab](https://gitlab.com/mrdarksidetm/RivoPhoneApp) &bull; [Codeberg](https://codeberg.org/mrdarksidetm/RivoPhoneApp) &bull; [Upstream Source](https://github.com/user-grinch/RivoPhoneApp)

</div>

---

## Features

- **Calling Cards & Contact Posters**: Full-screen high-resolution call backgrounds synced automatically from Google Phone & Google Contacts. Uses a non-blocking multi-tier bridge with instant native `ContactsContract.DisplayPhoto` (2560x2560) priority and direct elevated Shizuku shell process fallback (`Shizuku.newProcess`) without screen freezes.
- **Liquid Glass & Frosted Blur Engine**: Hardware-accelerated Snell's Law AGSL refraction lens shader (`RuntimeShader` on Android 13+) and dual-branch hardware blur compositing with interactive live preview and specular rim reflections.
- **Google Sans Flex Variable Typography**: High-fidelity variable font engine supporting real-time adjustments for Grade (`GRAD`), Weight (`wght`), Width (`wdth`), Roundness (`ROND`), Optical Size (`opsz`), and Slant (`slnt`).
- **Smart Contrast & Gradient Avatars**: Dynamically calculated WCAG-compliant text contrast adapting between dark and crisp white text based on container luminance, plus dual-tone harmonious gradient contact avatars and customizable squircle/polygon shapes.
- **T9 Search & Speed Dial**: Quick contact lookup by name or number right on the keypad, plus 1–9 speed dial shortcuts.
- **Dual SIM Support**: Outbound SIM selector, per-contact preferred SIM memory, and carrier tags.
- **Call Recording via Shizuku**: Internal 2-way call audio capture via Shizuku ADB permissions, without needing root or accessibility services. Standard microphone recording fallback included.
- **In-Call Screen**: Audio routing (earpiece, speaker, Bluetooth, wired headset), hold, mute, in-call dialpad, and call notes.
- **Call Notifications**: Android-native heads-up notifications with answer, decline, and speaker toggles.
- **Private Contacts Vault**: Keep specific contacts, their call history, and notifications locked behind biometrics or device PIN.
- **Contact Management**: Edit multiple numbers, emails, and addresses per contact with custom labels, contact deduplication, and local or cloud account storage.
- **Fake Incoming Call**: Simulate an incoming call with custom caller name, number, ringtone, and timer.
- **Blocklist & Spam Shield**: Block spam numbers directly from call logs or contact details.
- **Customization**: Material 3 Expressive theming, configurable dialpad layouts, avatar shapes, call log grouping, and app-level biometric lock.

## Screenshots

<p align="center">
  <img src="https://raw.githubusercontent.com/user-grinch/RivoPhoneApp/main/images/1.png" width="280" alt="Recents">
  <img src="https://raw.githubusercontent.com/user-grinch/RivoPhoneApp/main/images/2.png" width="280" alt="Dialpad">
  <img src="https://raw.githubusercontent.com/user-grinch/RivoPhoneApp/main/images/3.png" width="280" alt="Contact Details">
</p>
<p align="center">
  <img src="https://raw.githubusercontent.com/user-grinch/RivoPhoneApp/main/images/4.png" width="280" alt="Settings">
  <img src="https://raw.githubusercontent.com/user-grinch/RivoPhoneApp/main/images/5.png" width="280" alt="Call Screen">
  <img src="https://raw.githubusercontent.com/user-grinch/RivoPhoneApp/main/images/6.png" width="280" alt="Private Contacts">
</p>

## Downloads

- **Obtainium**: One-click install & updates via [Obtainium link](https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/{%22id%22:%22com.mrdarksidetm.rivo%22,%22url%22:%22https://github.com/junksidetm/RivoPhoneApp%22,%22author%22:%22junksidetm%22,%22name%22:%22RivoPhoneApp%22})
- **GitHub Releases**: Download production release APKs on [GitHub Releases](https://github.com/junksidetm/RivoPhoneApp/releases)
- **Source Mirrors**:
  - **Main (GitHub)**: [github.com/junksidetm/RivoPhoneApp](https://github.com/junksidetm/RivoPhoneApp)
  - **Mirror (Codeberg)**: [codeberg.org/mrdarksidetm/RivoPhoneApp](https://codeberg.org/mrdarksidetm/RivoPhoneApp)
  - **Mirror (GitLab)**: [gitlab.com/mrdarksidetm/RivoPhoneApp](https://gitlab.com/mrdarksidetm/RivoPhoneApp)
  - **Upstream Source**: [github.com/user-grinch/RivoPhoneApp](https://github.com/user-grinch/RivoPhoneApp)

### Note on Updating with Obtainium ("Different Certificate" Warning)
If you previously installed an older build or the upstream `user-grinch` version and Obtainium shows:
> *"The downloaded apk is signed with a different certificate then installed app. The install was skipped"*

This is standard Android security enforcement (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`) when updating between different signing keys:
1. **One-Time Fix:** In Obtainium, go to **Rivo Phone App** settings > enable **Allow reinstalling with different certificate** (or uninstall the old app version once).
2. Install the new release.
3. Every future update will share the permanent production release signing certificate and update automatically without warnings.

## Contributing

- **Issues & Bugs**: Report problems or feature suggestions via [GitHub Issues](https://github.com/user-grinch/RivoPhoneApp/issues).
- **Translations**: Help translate Rivo on [Crowdin](https://crowdin.com/project/rivophone).
- **Chat**: Join our [Discord server](https://discord.gg/NtEvU3726e).

## License

GNU General Public License v3.0 ([GPL-3.0](LICENSE)).

---

<div align="center">
<a href="https://github.com/junksidetm/junksidetm.github.io">
  <img src="https://raw.githubusercontent.com/junksidetm/assests/981a029b9f59b8ed581bbee9be318c86da52dcca/Images/Codium/Codium%20Banner/SVG/Codium%20-%20Banner%20Black.svg" width="360"></a>
<br><br>
<sub>
<p>This repository is a part of `"Codeium"`. A part of Darkside Studio.
</p>
</sub>
<p><b><sub>© 2026 Abhijeet Yadav.  All rights reserved. All logos are Copyright Law </sub></b></p>

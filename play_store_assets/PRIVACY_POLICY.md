# Privacy Policy for NenoKisia

**Last Updated**: October 19, 2026  
**Application**: NenoKisia (`com.rank.lexi`)  
**Developer**: Dr-Rank1  

This Privacy Policy describes how NenoKisia ("we", "us", or "our") handles user information when you download, install, or play the NenoKisia mobile application.

---

## 1. Summary (Privacy-First Architecture)

NenoKisia is designed from the ground up to be an **offline-first, privacy-respecting** game. 
- We **do not** sell or lease your personal information.
- We **do not** require user registration, email addresses, or account logins.
- Game progress, stats, and streaks stay directly on your device.

---

## 2. Information We Do Not Collect Directly

NenoKisia does **NOT** collect:
- Names, email addresses, phone numbers, or contact lists.
- Precise GPS geolocation data.
- Personal biometric or health data.
- Payment or financial account details.

---

## 3. Data Stored Locally on Your Device

All game progress and configurations are stored exclusively on your device using a local Android Room SQLite database and encrypted Android DataStore preferences:
- **Gameplay Records**: Solved words, attempts distribution, won dates, and streak counts.
- **Campaign & Level Progress**: World progression, star ratings, and boss battle records.
- **RPG & Quest Data**: XP points, lexicographer ranks, and completed daily quest milestones.
- **User Preferences**: Selected board color palette, display mode (System, Light, Dark), audio/haptic toggles, and gyroscope tilt parallax settings.

**You own your data.** Clearing the app data in Android Settings or uninstalling the app permanently deletes all stored data from your device.

---

## 4. Network and Internet Usage

NenoKisia functions 100% offline using its pre-bundled 15,287-word lexicon. 

The Android `INTERNET` permission (`android.permission.INTERNET`) is utilized solely for:
- **Optional Word Definitions**: If you choose to look up the dictionary definition of a solved word, the app queries the public Free Dictionary API. This request contains only the word itself (e.g., `api.dictionaryapi.dev/api/v2/entries/en/crane`). No cookies, user IDs, or device metadata are attached to these requests.
- **Ad Delivery**: Loading privacy-compliant ads to support game development.

---

## 5. Third-Party Services and Advertising

NenoKisia uses Start.io as a third-party ad network to display non-intrusive advertisements (banners, rewarded video for in-game powerups, and capped interstitials). 
- Start.io may process device technical data (such as IP address, approximate coarse location, and advertising ID) in accordance with its privacy policy to serve contextually relevant and family-safe ads.
- You can review Start.io's Privacy Policy at: [https://www.start.io/policy/privacy-policy/](https://www.start.io/policy/privacy-policy/)
- NenoKisia does not integrate invasive behavioral analytics trackers (no Facebook/Meta SDK, no invasive cross-app trackers).

---

## 6. Children's Privacy (COPPA & Family Policy Compliance)

NenoKisia is committed to family safety and complies with the **Children's Online Privacy Protection Act (COPPA)** and the **Google Play Families Policy**. Any ads displayed are configured for general audience and child-appropriate safety guidelines.

---

## 7. Security

Your game data is protected by the standard Android application sandbox, ensuring that no other applications on your device can access NenoKisia's internal SQLite database or settings.

---

## 8. Changes to This Privacy Policy

If we ever update this Privacy Policy, the revised version will be committed directly to our public open-source repository with an updated date.

---

## 9. Contact Us

If you have any questions or feedback regarding this Privacy Policy, please open an issue on the official GitHub repository:
- **Repository**: [https://github.com/Dr-Rank1/Wordle](https://github.com/Dr-Rank1/Wordle)
- **Direct Link for Google Play Console**: [https://raw.githubusercontent.com/Dr-Rank1/Wordle/main/PRIVACY_POLICY.md](https://raw.githubusercontent.com/Dr-Rank1/Wordle/main/PRIVACY_POLICY.md)

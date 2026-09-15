# Privacy Policy for LexiGuess

**Last Updated**: September 15, 2026  
**Application**: LexiGuess (`com.rank.lexi`)  
**Developer**: Dr-Rank1  

This Privacy Policy describes how LexiGuess ("we", "us", or "our") handles user information when you download, install, or play the LexiGuess mobile application.

---

## 1. Summary (Privacy-First Architecture)

LexiGuess is designed from the ground up to be an **offline-first, privacy-respecting** game. 
- We **do not** collect, store, or transmit any Personally Identifiable Information (PII).
- We **do not** use tracking SDKs, advertising networks, or user behavioral analytics.
- We **do not** require user registration, email addresses, or account logins.

---

## 2. Information We Do Not Collect

LexiGuess does **NOT** collect:
- Names, email addresses, phone numbers, or contact lists.
- Precise or coarse geolocation data.
- Advertising identifiers (GAID, IDFA).
- Hardware identifiers (IMEI, MAC addresses).
- Payment or financial details (the app is completely free with no in-app purchases).

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

LexiGuess functions 100% offline using its pre-bundled 15,287-word lexicon. 

The Android `INTERNET` permission (`android.permission.INTERNET`) is utilized solely for:
- **Optional Word Definitions**: If you choose to look up the dictionary definition of a solved word, the app queries the public Free Dictionary API. This request contains only the word itself (e.g., `api.dictionaryapi.dev/api/v2/entries/en/crane`). No cookies, user IDs, or device metadata are attached to these requests.

---

## 5. Third-Party Services and Analytics

LexiGuess contains **zero third-party tracking or advertising SDKs**:
- No Google Firebase Analytics or Crashlytics
- No Facebook / Meta SDK
- No AdMob, Unity Ads, or third-party ad networks
- No cross-app tracking cookies or fingerprinting

---

## 6. Children's Privacy (COPPA & Family Policy Compliance)

Because LexiGuess does not collect any personal information from any user, it fully complies with the **Children's Online Privacy Protection Act (COPPA)** and the **Google Play Families Policy**. Children of all ages can safely enjoy LexiGuess without data collection risks.

---

## 7. Security

Your game data is protected by the standard Android application sandbox, ensuring that no other applications on your device can access LexiGuess's internal SQLite database or settings.

---

## 8. Changes to This Privacy Policy

If we ever update this Privacy Policy, the revised version will be committed directly to our public open-source repository with an updated date.

---

## 9. Contact Us

If you have any questions or feedback regarding this Privacy Policy, please open an issue on the official GitHub repository:
- **Repository**: [https://github.com/Dr-Rank1/Wordle](https://github.com/Dr-Rank1/Wordle)
- **Direct Link for Google Play Console**: [https://raw.githubusercontent.com/Dr-Rank1/Wordle/main/PRIVACY_POLICY.md](https://raw.githubusercontent.com/Dr-Rank1/Wordle/main/PRIVACY_POLICY.md)

<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/6970c77d-652b-4552-aa45-1e4eeb6b0b21

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.


## Live API + MySQL

The Android repository is connected to the PHP API at `https://nexuscom.page.gd/khelobdapp-api`.
The API stores the repository state in the new `if0_40253952_khelobdapp` database.
Upload the bundled `khelobdapp-api` folder to the old site's root without overwriting existing files,
then set the new database password in `khelobdapp-api/config.php`. Import the bundled `database.sql`
into the new database. The Android APK never contains the MySQL password.


## Build target
Application ID: `com.khelobd.esports`
App: `KHELO BD`

This project uses the HTTPS KHELO BD API.

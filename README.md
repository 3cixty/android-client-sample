Welcome!
=====================

This repository shows:

1. How to call `3cixty OAuth app` to get a `3cixty access token` and to revoke an existing `3cixty token`. 
2. How to call the `3cixty platform` to register to `Goflow platform` and to retrieve the `Goflow` credentials.


Getting Started
================

1. Install the [application] (https://play.google.com/store/apps/details?id=com.threecixty.auth&hl=en).

2. `git clone https://github.com/3cixty/android-client-sample.git`.

3. Open [Android Studio](https://developer.android.com/sdk/installing/studio.html).

4. Import the `android-client-sample` project (be sure to pick the `settings.gradle` file in the directory where you cloned this repo).

5. Replace the **line 58**  `oauthIntent.putExtra("app_key", "26798921-d2bb-43d5-bf95-c4e0deae3af0");` of the `MainActivity.java` class (`https://github.com/3cixty/android-client-sample/blob/master/app/src/main/java/com/threecixty/oauthsample/MainActivity.java`) with your `appkey`.


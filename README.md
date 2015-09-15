Welcome!
=====================

This repository shows:

1. How to call `3cixty OAuth app` to get a `3cixty access token` and to revoke an existing `3cixty token`. 
2. How to call the `3cixty platform` to register to `Goflow platform` and to retrieve the `Goflow` credentials.


Getting Started
================

1. `git clone https://github.com/3cixty/android-client-sample.git`.

2. Open [Android Studio](https://developer.android.com/sdk/installing/studio.html).

3. Import the `android-client-sample` project (be sure to pick the `settings.gradle` file in the directory where you cloned this repo).

4. Replace the **line 65**  `oauthIntent.putExtra("app_key", "26798921-d2bb-43d5-bf95-c4e0deae3af0");` of the `MainActivity.java` class (`https://github.com/3cixty/android-client-sample/blob/master/app/src/main/java/com/threecixty/oauthsample/MainActivity.java`) with your `appkey`.

5. Make sure that your Android package name associated with your key to sign application so that 3cixty platform is able to get the list of friends from a Google access token. More information in detail can be found at https://developers.google.com/drive/android/auth. Here is some extracted information:

 ```
 create credentials appropriate to your project in the Google Developers Console:

    Open the Credentials page.
    Follow these steps if your application needs to submit authorized requests:

        Click Add credentials > OAuth 2.0 client ID.
        Select Android.
        In the Package name field, enter your Android app's package name.
        Paste the SHA1 fingerprint into the form where requested.
        Click Create.
 ```


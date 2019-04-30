
# react-native-ad-consent

Uses the Google Mobile Ads Consent SDK to ask users for GDPR compliant ad consent. Supports iOS and Android.

## Getting started
```sh
$ yarn add react-native-ad-consent
```

or

```sh
$ npm install react-native-ad-consent --save
```

### Mostly automatic installation

```sh
$ react-native link react-native-ad-consent
```

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-ad-consent` and add `RNAdConsent.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNAdConsent.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import de.bnass.RNAdConsent.RNAdConsentPackage;` to the imports at the top of the file
  - Add `new RNAdConsentPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
    ```
    include ':react-native-ad-consent'
    project(':react-native-ad-consent').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-ad-consent/android')
    ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
    ```
    compile project(':react-native-ad-consent')
    ```

### Additional Steps


#### iOS

##### Dependencies With Pods

```diff
+     pod 'PersonalizedAdConsent'
+     pod 'Google-Mobile-Ads-SDK'
```

##### Dependencies Without Pods

Follow the instructions for the [Google Mobile Ads SDK](https://developers.google.com/admob/ios/quick-start#import_the_mobile_ads_sdk) and the [Google Mobile Ads Consent SDK](https://developers.google.com/admob/ios/eu-consent#import_the_consent_sdk).

##### Setup

Add the following key to your project's `Info.plist`:
```diff
+               <key>GADIsAdManagerApp</key>
+               <true/>
              </dict>
            </plist>
```

Add the following initalization code to your project's `AppDelegate.h`:
```diff
 #import <React/RCTBridgeDelegate.h>
 #import <UIKit/UIKit.h>
+#import <GoogleMobileAds/GoogleMobileAds.h>
```

Add the following initalization code to your project's `AppDelegate.m`. Replace the sample AdMob App ID with your ID:
```diff
(BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
+  [GADMobileAds configureWithApplicationID:@"ca-app-pub-3940256099942544~3347511713"];

```

## Usage

### Get ad providers and set consent
```javascript
import RNAdConsent from 'react-native-ad-consent'

// always request the status of a user's consent first
const consentStatus = await RNAdConsent.requestConsentInfoUpdate({
  publisherId: "pub-1234567890",
})

if (consentStatus === RNAdConsent.UNKNOWN) {
  const isRequestLocationInEeaOrUnknown = await RNAdConsent.isRequestLocationInEeaOrUnknown()

  if (isRequestLocationInEeaOrUnknown) {
    const adProviders = await RNAdConsent.getAdProviders()
    
    // do stuff with ad providers, e.g. show a custom consent modal
    
    await RNAdConsent.setConsentStatus(RNAdConsent.PERSONALIZED)
  }
}
```

### Show Google's consent modal and set consent accordingly
```javascript
import RNAdConsent from 'react-native-ad-consent'

// always request the status of a user's consent first
const consentStatus = await RNAdConsent.requestConsentInfoUpdate({
  publisherId: "pub-1234567890",
})

if (consentStatus === RNAdConsent.UNKNOWN) {
  const formResponse = await RNAdConsent.showGoogleConsentForm({
    privacyUrl: "https://your-privacy-link.com",
    shouldOfferAdFree: true,
  })
  
  if (formResponse === RNAdConsent.PREFERS_AD_FREE) {
    // do stuff
  } else {
    await RNAdConsent.setConsentStatus(formResponse)
  }
}
```

## API

### Constants

| Name									| Value									|
|-----------------------|-----------------------|
| NON_PERSONALIZED			| "non_personalized"		|
| PERSONALIZED					| "personalized"				|
| UNKNOWN								| "unknown"							|
| PREFERS_AD_FREE				| "prefers_ad_free"			|

### Methods

#### `addTestDevice(deviceId: string): Promise<boolean>`

Adds a test device ID. See how to get your ID for [ios](https://developers.google.com/admob/ios/test-ads#add_your_test_device) and [Android](https://developers.google.com/admob/android/test-ads#add_your_test_device).

>The Consent SDK has different behaviors depending on the value of [...] isRequestLocationInEeaOrUnknown(). For example, the consent form fails to load if the user is not located in the EEA. To enable easier testing of your app both inside and outside the EEA, the Consent SDK supports debug options that you can set prior to calling any other methods in the Consent SDK. _[source](https://developers.google.com/admob/android/eu-consent#testing)_

#### `getAdProviders(): Promise<AdProviderItem[]>`

```
type AdProviderItem = {
  id: string,
  name: string,
  privacyPolicyUrl: string,
}
```

Returns a list of the ad technology providers associated with the publisher IDs used in your app.

#### `isRequestLocationInEeaOrUnknown(): Promise<boolean>`

Returns a boolean indicating if the user's consent is needed.

#### `requestConsentInfoUpdate(config: ConsentInfoConfig): Promise<ConsentStatus>`

```
type ConsentInfoConfig = {
  publisherId: string,
}

type ConsentStatus = "non_personalized" | "personalized" | "unknown"
```

Returns the user's consent status, needs to be called once before any other method is called.

#### `setConsentStatus(status: string): Promise<boolean>`

Sets the user's consent choice.

#### `setTagForUnderAgeOfConsent(isUnderAgeOfConsent: boolean): Promise<boolean>`

Sets a flag indicating wether the user is under the age of consent.

#### `showGoogleConsentForm(config: ConsentFormObject): Promise<FormResponse>`

```
type ConsentFormObject = {
  privacyPolicyUrl: string,
  shouldOfferAdFree: boolean,
}

type FormResponse = "non_personalized" | "personalized" | "unknown" | "prefers_ad_free"
```

Shows a Google-rendered consent form. Returns the user's choice as a string.

>You should review the consent text carefully: what appears by default is a message that might be appropriate if you use Google to monetize your app; but we cannot provide legal advice on the consent text that is appropriate for you. _[source](https://developers.google.com/admob/android/eu-consent#google_rendered_consent_form)_


# react-native-ad-consent

Google's User Messaging Platform (UMP SDK) for React Native.

## Getting started

```sh
$ yarn add react-native-ad-consent
```

or

```sh
$ npm install react-native-ad-consent
```

### Additional Steps (iOS)

Make sure that you have your App ID and the NSUserTrackingUsageDescription in your project's `Info.plist` as shown [here](https://developers.google.com/ad-manager/ump/ios/quick-start).
```diff
+               <key>GADApplicationIdentifier</key>
+               <string>ca-app-pub-3940256099942544~3347511713</string>
+               <key>NSUserTrackingUsageDescription</key>
+               <string>This identifier will be used to deliver personalized ads to you.</string>
              </dict>
            </plist>
```

### Additional Steps (Android)

Make sure you have your App ID in your project's `AndroidManifest.xml`:
```diff
+             <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="ca-app-pub-3940256099942544~3347511713"/>
            </application>
```

## Usage

```javascript
import { UMP } from 'react-native-ad-consent'

const {
  consentStatus,
  isConsentFormAvailable,
  isRequestLocationInEeaOrUnknown,
} = await UMP.requestConsentInfoUpdate()

if (
  isRequestLocationInEeaOrUnknown &&
  isConsentFormAvailable &&
  consentStatus === UMP.CONSENT_STATUS.REQUIRED
) {
  const { consentStatus } = await UMP.showConsentForm()
}
```

### Testing

```javascript
const {
  consentStatus,
  isConsentFormAvailable,
  isRequestLocationInEeaOrUnknown,
} = await UMP.requestConsentInfoUpdate({
   debugGeography: UMP.DEBUG_GEOGRAPHY.EEA,
   testDeviceIds: ['TEST-DEVICE-HASHED-ID'],
})
```

>The UMP SDK provides a simple way to test your app's behavior as though the device was located in the EEA or UK using the debugGeography property of type UMPDebugGeography on UMPDebugSettings.
>You will need to provide your test device's hashed ID in your app's debug settings to use the debug functionality. If you call requestConsentUpdateWithParameters without setting this value, your app will log the required ID hash when run.
>The UMP SDK provides a simple way to test your app's behavior as though the device was located in the EEA or UK using the debugGeography property of type UMPDebugGeography on UMPDebugSettings. _[source](https://developers.google.com/admob/ump/ios/quick-start#testing)_

## API

#### `requestConsentInfoUpdate(config?: ConsentInfoConfig): Promise<ConsentInfoUpdate>`

```
type ConsentInfoConfig = {
  debugGeography: number,
  testDeviceIds: string[],
}

type ConsentInfoUpdate = {
  consentStatus: number,
  isConsentFormAvailable: boolean,
  isRequestLocationInEeaOrUnknown: boolean,
}
```

Returns the consent information.

#### `showConsentForm(): Promise<ConsentFormResponse>`

```
type ConsentFormResponse = {
  consentStatus: number,
}
```

Shows the consent form and returns the updated consentStatus on close.

#### `reset(): void`

Resets the consent state.


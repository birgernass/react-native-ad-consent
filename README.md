
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

Make sure you have your App ID in your project's `Info.plist`:
```diff
+               <key>GADApplicationIdentifier</key>
+               <string>ca-app-pub-3940256099942544~3347511713</string>
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
  consentType,
  isConsentFormAvailable,
  isRequestLocationInEeaOrUnknown,
} = await UMP.requestConsentInfoUpdate()

if (
  isRequestLocationInEeaOrUnknown &&
  isConsentFormAvailable &&
  consentStatus === UMP.CONSENT_STATUS.REQUIRED
) {
  const { consentStatus, consentType } = await UMP.showConsentForm()
}
```

### Testing

```javascript
const {
  consentStatus,
  consentType,
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

### Known Issues

* The consentType is currently always UNKNOWN (0)

## API

### Constants

| Name													| Value													|
|-------------------------------|-------------------------------|
| CONSENT_STATUS.OBTAINED				| 3															|
| CONSENT_STATUS.NOT_REQUIRED		| 2															|
| CONSENT_STATUS.REQUIRED				| 1															|
| CONSENT_STATUS.UNKNOWN				| 0															|
| CONSENT_TYPE.NON_PERSONALIZED	| 2															|
| CONSENT_TYPE.PERSONALIZED			| 1															|
| CONSENT_TYPE.UNKNOWN					| 0															|
| DEBUG_GEOGRAPHY.NOT_EEA				| 2															|
| DEBUG_GEOGRAPHY.EEA			      | 1															|
| DEBUG_GEOGRAPHY.DISABLED    	| 0															|

### Methods

#### `requestConsentInfoUpdate(config?: ConsentInfoConfig): Promise<ConsentInfoUpdate>`

```
type ConsentInfoConfig = {
  debugGeography: number,
  testDeviceIds: Array<String>,
}

type ConsentInfoUpdate = {
  consentStatus: number,
  consentType: number,
  isConsentFormAvailable: boolean,
  isRequestLocationInEeaOrUnknown: boolean,
}
```

Returns the consent information.

#### `showConsentForm(): Promise<ConsentFormResponse>`

```
type ConsentFormResponse = {
  consentStatus: number,
  consentType: number,
}
```

Shows the consent form and returns the updated consentStatus and consentType on close.

#### `reset(): void`

Resets the consent state.


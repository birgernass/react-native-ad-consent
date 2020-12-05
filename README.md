
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

### Known Issues

* The consentType is currently always UNKNOWN (0)

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

### Methods

#### `requestConsentInfoUpdate(): Promise<ConsentInfoUpdate>`

```
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


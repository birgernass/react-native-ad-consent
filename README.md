> **Recommendation:** The code of this repository was merged into [react-native-google-mobile-ads](https://github.com/invertase/react-native-google-mobile-ads), a better maintained and more feature-rich library. Consider using it instead of this library.

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
  const { canRequestAds, consentStatus } = await UMP.showConsentForm()
}
```

### Testing

```javascript
const {
  canRequestAds,
  consentStatus,
  isConsentFormAvailable,
  isRequestLocationInEeaOrUnknown,
  privacyOptionsRequirementStatus,
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
  tagForUnderAgeOfConsent: boolean,
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


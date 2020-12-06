import { Platform, NativeModules } from 'react-native' // eslint-disable-line

const {
  UMP_CONSENT_STATUS,
  UMP_CONSENT_TYPE,
  UMP_DEBUG_GEOGRAPHY,
  UMP_requestConsentInfoUpdate,
  UMP_reset,
  UMP_showConsentForm,
} = NativeModules.RNAdConsent

export const UMP = {
  CONSENT_STATUS: UMP_CONSENT_STATUS,
  CONSENT_TYPE: UMP_CONSENT_TYPE,
  DEBUG_GEOGRAPHY: UMP_DEBUG_GEOGRAPHY,
  requestConsentInfoUpdate: config =>
    UMP_requestConsentInfoUpdate({
      debugGeography: UMP_DEBUG_GEOGRAPHY.DISABLED,
      testDeviceIds: [],
      ...config,
    }),
  reset: UMP_reset,
  showConsentForm: UMP_showConsentForm,
}

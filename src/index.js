import { NativeModules } from 'react-native' // eslint-disable-line

const {
  UMP_CONSENT_STATUS,
  UMP_CONSENT_TYPE,
  UMP_requestConsentInfoUpdate,
  UMP_reset,
  UMP_showConsentForm,
} = NativeModules.RNAdConsent

export const UMP = {
  CONSENT_STATUS: UMP_CONSENT_STATUS,
  CONSENT_TYPE: UMP_CONSENT_TYPE,
  requestConsentInfoUpdate: UMP_requestConsentInfoUpdate,
  reset: UMP_reset,
  showConsentForm: UMP_showConsentForm,
}

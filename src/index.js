import { NativeModules } from 'react-native' // eslint-disable-line

const {
  CONSENT_STATUS,
  CONSENT_TYPE,
  UMP_requestConsentInfoUpdate,
  UMP_reset,
  UMP_showConsentForm,
  ...RNAdConsent
} = NativeModules.RNAdConsent

if (typeof RNAdConsent === 'undefined') {
  throw new Error(
    'RNAdConsent not installed correctly. Did you forget to link it?'
  )
}

export const UMP = {
  CONSENT_STATUS,
  CONSENT_TYPE,
  requestConsentInfoUpdate: UMP_requestConsentInfoUpdate,
  reset: UMP_reset,
  showConsentForm: UMP_showConsentForm,
}

export default RNAdConsent

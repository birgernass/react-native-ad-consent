import { NativeModules } from 'react-native'

const { RNAdConsent } = NativeModules

if (typeof RNAdConsent === 'undefined') {
  throw new Error(
    'RNAdConsent not installed correctly. Did you forget to link it?'
  )
}

export default RNAdConsent

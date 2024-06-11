package de.bnass.RNAdConsent;

import androidx.annotation.Nullable;

import java.util.Map;
import java.util.HashMap;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import javax.annotation.Nonnull;

public class RNAdConsentModule extends ReactContextBaseJavaModule {
    private static final String E_ACTIVITY_NOT_AVAILABLE = "E_ACTIVITY_NOT_AVAILABLE";
    private static final String E_ACTIVITY_NOT_AVAILABLE_MSG = "Activity is not available.";

    private ConsentInformation consentInformation;

    public RNAdConsentModule(ReactApplicationContext reactContext) {
        super(reactContext);
        consentInformation = UserMessagingPlatform.getConsentInformation(reactContext);
    }

    @Override
    public String getName() {
        return "RNAdConsent";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        final Map<String, Object> UMP_CONSENT_STATUS = new HashMap<>();
        UMP_CONSENT_STATUS.put("NOT_REQUIRED", ConsentInformation.ConsentStatus.NOT_REQUIRED);
        UMP_CONSENT_STATUS.put("OBTAINED", ConsentInformation.ConsentStatus.OBTAINED);
        UMP_CONSENT_STATUS.put("REQUIRED", ConsentInformation.ConsentStatus.REQUIRED);
        UMP_CONSENT_STATUS.put("UNKNOWN", ConsentInformation.ConsentStatus.UNKNOWN);
        constants.put("UMP_CONSENT_STATUS", UMP_CONSENT_STATUS);

        final Map<String, Object> UMP_DEBUG_GEOGRAPHY = new HashMap<>();
        UMP_DEBUG_GEOGRAPHY.put("DISABLED", ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED);
        UMP_DEBUG_GEOGRAPHY.put("EEA", ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA);
        UMP_DEBUG_GEOGRAPHY.put("NOT_EEA", ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA);
        constants.put("UMP_DEBUG_GEOGRAPHY", UMP_DEBUG_GEOGRAPHY);

        final Map<String, Object> UMP_ERROR_CODE = new HashMap<>();
        UMP_ERROR_CODE.put("E_ACTIVITY_NOT_AVAILABLE", E_ACTIVITY_NOT_AVAILABLE);
        constants.put("UMP_ERROR_CODE", UMP_ERROR_CODE);

        final Map<String, Object> UMP_PRIVACY_OPTIONS_REQUIREMENT_STATUS = new HashMap<>();
        UMP_PRIVACY_OPTIONS_REQUIREMENT_STATUS.put("NOT_REQUIRED", ConsentInformation.PrivacyOptionsRequirementStatus.NOT_REQUIRED.ordinal());
        UMP_PRIVACY_OPTIONS_REQUIREMENT_STATUS.put("REQUIRED", ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED.ordinal());
        UMP_PRIVACY_OPTIONS_REQUIREMENT_STATUS.put("UNKNOWN", ConsentInformation.PrivacyOptionsRequirementStatus.UNKNOWN.ordinal());
        constants.put("UMP_PRIVACY_OPTIONS_REQUIREMENT_STATUS", UMP_PRIVACY_OPTIONS_REQUIREMENT_STATUS);

        return constants;
    }

    @ReactMethod
    public void UMP_requestConsentInfoUpdate(@Nonnull final ReadableMap config, final Promise promise) {
        try {
            ConsentRequestParameters.Builder paramsBuilder = new ConsentRequestParameters.Builder();
            ConsentDebugSettings.Builder debugSettingsBuilder =
                    new ConsentDebugSettings.Builder(getReactApplicationContext().getApplicationContext());

            if (config.hasKey("testDeviceIds")) {
                ReadableArray testDeviceIds = config.getArray("testDeviceIds");

                for (int i = 0; i < testDeviceIds.size(); i++) {
                    debugSettingsBuilder.addTestDeviceHashedId(testDeviceIds.getString(i));
                }
            }

            if (config.hasKey("debugGeography")) {
                debugSettingsBuilder.setDebugGeography(config.getInt("debugGeography"));
            }

            paramsBuilder.setConsentDebugSettings(debugSettingsBuilder.build());

            if (config.hasKey("tagForUnderAgeOfConsent")) {
                paramsBuilder.setTagForUnderAgeOfConsent(config.getBoolean("tagForUnderAgeOfConsent"));
            }

            ConsentRequestParameters consentRequestParameters = paramsBuilder.build();

            if (getCurrentActivity() == null) {
                promise.reject(E_ACTIVITY_NOT_AVAILABLE, E_ACTIVITY_NOT_AVAILABLE_MSG);
                return;
            }

            consentInformation.requestConsentInfoUpdate(
                    getCurrentActivity(),
                    consentRequestParameters,
                    new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                        @Override
                        public void onConsentInfoUpdateSuccess() {
                            int consentStatus = consentInformation.getConsentStatus();
                            boolean isRequestLocationInEeaOrUnknown = consentStatus != ConsentInformation.ConsentStatus.NOT_REQUIRED;

                            WritableMap payload = Arguments.createMap();
                            payload.putBoolean("canRequestAds", consentInformation.canRequestAds());
                            payload.putInt("consentStatus", consentStatus);
                            payload.putBoolean("isConsentFormAvailable", consentInformation.isConsentFormAvailable());
                            payload.putBoolean("isRequestLocationInEeaOrUnknown", isRequestLocationInEeaOrUnknown);
                            payload.putInt("privacyOptionsRequirementStatus", consentInformation.getPrivacyOptionsRequirementStatus().ordinal());

                            promise.resolve(payload);
                        }
                    },
                    new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                        @Override
                        public void onConsentInfoUpdateFailure(FormError formError) {
                            promise.reject("" + formError.getErrorCode(), formError.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void UMP_showConsentForm(final Promise promise) {
        try {
            if (getCurrentActivity() == null) {
                promise.reject(E_ACTIVITY_NOT_AVAILABLE, E_ACTIVITY_NOT_AVAILABLE_MSG);
                return;
            }

            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UserMessagingPlatform.loadConsentForm(
                            getReactApplicationContext().getApplicationContext(),
                            new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                                @Override
                                public void onConsentFormLoadSuccess(com.google.android.ump.ConsentForm consentForm) {
                                    consentForm.show(
                                            getCurrentActivity(),
                                            new com.google.android.ump.ConsentForm.OnConsentFormDismissedListener() {
                                                @Override
                                                public void onConsentFormDismissed(@Nullable FormError formError) {
                                                    if (formError != null) {
                                                        promise.reject("" + formError.getErrorCode(), formError.getMessage());
                                                    } else {
                                                        WritableMap payload = Arguments.createMap();
                                                        payload.putBoolean("canRequestAds", consentInformation.canRequestAds());
                                                        payload.putInt("consentStatus", consentInformation.getConsentStatus());
                                                        payload.putInt("privacyOptionsRequirementStatus", consentInformation.getPrivacyOptionsRequirementStatus().ordinal());

                                                        promise.resolve(payload);
                                                    }
                                                }
                                            });
                                }
                            },
                            new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
                                @Override
                                public void onConsentFormLoadFailure(FormError formError) {
                                    promise.reject("" + formError.getErrorCode(), formError.getMessage());
                                }
                            }
                    );
                }
            });
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void UMP_reset() {
        consentInformation.reset();
    }
}

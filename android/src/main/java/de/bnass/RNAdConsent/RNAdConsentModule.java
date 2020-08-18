package de.bnass.RNAdConsent;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import com.google.ads.consent.*;

import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

public class RNAdConsentModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNAdConsent";

    private final ReactApplicationContext reactContext;

    private static final String NON_PERSONALIZED = "non_personalized";
    private static final String PERSONALIZED = "personalized";
    private static final String PREFERS_AD_FREE = "prefers_ad_free";
    private static final String UNKNOWN = "unknown";

    public RNAdConsentModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNAdConsent";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        constants.put("NON_PERSONALIZED", NON_PERSONALIZED);
        constants.put("PERSONALIZED", PERSONALIZED);
        constants.put("PREFERS_AD_FREE", PREFERS_AD_FREE);
        constants.put("UNKNOWN", UNKNOWN);

        final Map<String, Object> consentStatus = new HashMap<>();
        consentStatus.put("NOT_REQUIRED", com.google.android.ump.ConsentInformation.ConsentStatus.NOT_REQUIRED);
        consentStatus.put("OBTAINED", com.google.android.ump.ConsentInformation.ConsentStatus.OBTAINED);
        consentStatus.put("REQUIRED", com.google.android.ump.ConsentInformation.ConsentStatus.REQUIRED);
        consentStatus.put("UNKNOWN", com.google.android.ump.ConsentInformation.ConsentStatus.UNKNOWN);
        constants.put("CONSENT_STATUS", consentStatus);

        final Map<String, Object> consentType = new HashMap<>();
        consentType.put("NON_PERSONALIZED", com.google.android.ump.ConsentInformation.ConsentType.NON_PERSONALIZED);
        consentType.put("PERSONALIZED", com.google.android.ump.ConsentInformation.ConsentType.PERSONALIZED);
        consentType.put("UNKNOWN", com.google.android.ump.ConsentInformation.ConsentType.UNKNOWN);
        constants.put("CONSENT_TYPE", consentType);

        return constants;
    }

    /**
     * Consent Library
     */
    private ConsentForm form;

    private void showConsentForm() {
        form.show();
    }

    @ReactMethod
    public void isRequestLocationInEeaOrUnknown(Promise promise) {
        try {
            promise.resolve(ConsentInformation.getInstance(reactContext).isRequestLocationInEeaOrUnknown());
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void setTagForUnderAgeOfConsent(boolean isUnderAgeOfConsent, Promise promise) {
        try {
            ConsentInformation.getInstance(reactContext).setTagForUnderAgeOfConsent(isUnderAgeOfConsent);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getAdProviders(Promise promise) {
        try {
            List<AdProvider> adProviders = ConsentInformation.getInstance(reactContext).getAdProviders();

            WritableArray adProvidersArray = Arguments.createArray();

            for (int i = 0; i < adProviders.size(); i++) {
                AdProvider adProvider = adProviders.get(i);
                WritableMap adProviderMap = Arguments.createMap();
                adProviderMap.putString("id", adProvider.getId());
                adProviderMap.putString("name", adProvider.getName());
                adProviderMap.putString("privacyPolicyUrl", adProvider.getPrivacyPolicyUrlString());
                adProvidersArray.pushMap(adProviderMap);
            }
            promise.resolve(adProvidersArray);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void setConsentStatus(String status, Promise promise) {
        try {
            ConsentStatus consentStatus;
            if (status.equals(NON_PERSONALIZED)) {
                consentStatus = ConsentStatus.NON_PERSONALIZED;
            } else if (status.equals(PERSONALIZED)) {
                consentStatus = ConsentStatus.PERSONALIZED;
            } else {
                consentStatus = ConsentStatus.UNKNOWN;
            }
            ConsentInformation.getInstance(reactContext).setConsentStatus(consentStatus);

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void addTestDevice(String deviceId, Promise promise) {
        try {
            ConsentInformation.getInstance(reactContext).addTestDevice(deviceId);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void requestConsentInfoUpdate(final ReadableMap config, final Promise promise) {
        try {
            String[] publisherIds = {config.getString("publisherId")};
            ConsentInformation.getInstance(reactContext).requestConsentInfoUpdate(publisherIds,
                    new ConsentInfoUpdateListener() {
                        @Override
                        public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                            if (consentStatus.equals(ConsentStatus.PERSONALIZED)) {
                                promise.resolve(PERSONALIZED);
                            } else if (consentStatus.equals(ConsentStatus.NON_PERSONALIZED)) {
                                promise.resolve(NON_PERSONALIZED);
                            } else {
                                promise.resolve(UNKNOWN);
                            }
                        }

                        @Override
                        public void onFailedToUpdateConsentInfo(String errorDescription) {
                            promise.reject(errorDescription);
                        }
                    });
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void showGoogleConsentForm(final ReadableMap config, final Promise promise) {
        try {
            String privacyUrlString = config.getString("privacyPolicyUrl");
            URL privacyUrl;
            try {
                privacyUrl = new URL(privacyUrlString);
            } catch (MalformedURLException e) {
                promise.reject(e);
                return;
            }

            Boolean shouldOfferAdFree = config.getBoolean("shouldOfferAdFree");

            ConsentFormListener listener = new ConsentFormListener() {
                @Override
                public void onConsentFormLoaded() {
                    showConsentForm();
                }

                @Override
                public void onConsentFormOpened() {
                }

                @Override
                public void onConsentFormClosed(ConsentStatus consentStatus,
                                                Boolean userPrefersAdFree) {
                    if (userPrefersAdFree) {
                        promise.resolve(PREFERS_AD_FREE);
                    } else {
                        if (consentStatus.equals(ConsentStatus.PERSONALIZED)) {
                            promise.resolve(PERSONALIZED);
                        } else if (consentStatus.equals(ConsentStatus.NON_PERSONALIZED)) {
                            promise.resolve(NON_PERSONALIZED);
                        } else {
                            promise.resolve(UNKNOWN);
                        }
                    }
                }

                @Override
                public void onConsentFormError(String errorDescription) {
                    promise.reject(errorDescription);
                }
            };

            if (shouldOfferAdFree) {
                form = new ConsentForm.Builder(getCurrentActivity(), privacyUrl)
                        .withListener(listener)
                        .withAdFreeOption()
                        .withNonPersonalizedAdsOption()
                        .withPersonalizedAdsOption()
                        .build();
            } else {
                form = new ConsentForm.Builder(getCurrentActivity(), privacyUrl)
                        .withListener(listener)
                        .withNonPersonalizedAdsOption()
                        .withPersonalizedAdsOption()
                        .build();
            }

            form.load();
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * User Messaging Platform
     */
    private com.google.android.ump.ConsentInformation consentInformation;

    @ReactMethod
    public void UMP_requestConsentInfoUpdate(final Promise promise) {
        try {
            ConsentRequestParameters consentRequestParameters = new ConsentRequestParameters.Builder().build();

            consentInformation = UserMessagingPlatform.getConsentInformation(reactContext.getApplicationContext());

            consentInformation.requestConsentInfoUpdate(
                    reactContext.getCurrentActivity(),
                    consentRequestParameters,
                    new com.google.android.ump.ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                        @Override
                        public void onConsentInfoUpdateSuccess() {
                            int consentStatus = consentInformation.getConsentStatus();
                            int consentType = consentInformation.getConsentType();
                            boolean isConsentFormAvailable = consentInformation.isConsentFormAvailable();
                            boolean isRequestLocationInEeaOrUnknown = consentStatus != com.google.android.ump.ConsentInformation.ConsentStatus.NOT_REQUIRED;

                            Log.d(TAG, "[UMP requestConsentInfoUpdate] consentStatus: " + consentStatus + " consentType: " + consentType + " isConsentFormAvailable: " + isConsentFormAvailable + " isRequestLocationInEeaOrUnknown: " + isRequestLocationInEeaOrUnknown);

                            WritableMap payload = Arguments.createMap();
                            payload.putInt("consentStatus", consentStatus);
                            payload.putInt("consentType", consentType);
                            payload.putBoolean("isConsentFormAvailable", isConsentFormAvailable);
                            payload.putBoolean("isRequestLocationInEeaOrUnknown", isRequestLocationInEeaOrUnknown);

                            promise.resolve(payload);
                        }
                    },
                    new com.google.android.ump.ConsentInformation.OnConsentInfoUpdateFailureListener() {
                        @Override
                        public void onConsentInfoUpdateFailure(FormError formError) {

                            Log.d(TAG, "[UMP requestConsentInfoUpdate] error: " + formError.getMessage());
                            promise.reject("" + formError.getErrorCode(), formError.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.d(TAG, "[UMP requestConsentInfoUpdate] error: " + e.getMessage());
            promise.reject(e);
        }
    }

    @ReactMethod
    public void UMP_showConsentForm(final Promise promise) {
        try {
            final Activity activity = reactContext.getCurrentActivity();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UserMessagingPlatform.loadConsentForm(
                            reactContext.getApplicationContext(),
                            new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                                @Override
                                public void onConsentFormLoadSuccess(com.google.android.ump.ConsentForm consentForm) {
                                    consentForm.show(
                                            activity,
                                            new com.google.android.ump.ConsentForm.OnConsentFormDismissedListener() {
                                                @Override
                                                public void onConsentFormDismissed(@Nullable FormError formError) {
                                                    if (formError != null) {
                                                        Log.d(TAG, "[UMP showConsentForm] error: " + formError.getMessage());
                                                        promise.reject("" + formError.getErrorCode(), formError.getMessage());
                                                    } else {
                                                        int consentStatus = consentInformation.getConsentStatus();
                                                        int consentType = consentInformation.getConsentType();

                                                        Log.d(TAG, "[UMP show] consentStatus: " + consentStatus);

                                                        WritableMap payload = Arguments.createMap();
                                                        payload.putInt("consentStatus", consentStatus);
                                                        payload.putInt("consentType", consentType);

                                                        promise.resolve(payload);
                                                    }
                                                }
                                            });
                                }
                            },
                            new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
                                @Override
                                public void onConsentFormLoadFailure(FormError formError) {
                                    Log.d(TAG, "[UMP showConsentForm] error: " + formError.getMessage());
                                    promise.reject("" + formError.getErrorCode(), formError.getMessage());
                                }
                            }
                    );
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "[UMP showConsentForm] error: " + e.getMessage());
            promise.reject(e);
        }
    }

    @ReactMethod
    public void UMP_reset() {
        try {
            consentInformation.reset();
        } catch (Exception e) {
            Log.d(TAG, "[UMP reset] error: " + e.getMessage());
        }
    }
}

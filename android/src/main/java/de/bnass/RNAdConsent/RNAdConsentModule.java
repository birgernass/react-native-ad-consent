package de.bnass.RNAdConsent;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Map;
import java.util.HashMap;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

public class RNAdConsentModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNAdConsent";

    private final ReactApplicationContext reactContext;

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

        final Map<String, Object> UMP_consentStatus = new HashMap<>();
        UMP_consentStatus.put("NOT_REQUIRED", com.google.android.ump.ConsentInformation.ConsentStatus.NOT_REQUIRED);
        UMP_consentStatus.put("OBTAINED", com.google.android.ump.ConsentInformation.ConsentStatus.OBTAINED);
        UMP_consentStatus.put("REQUIRED", com.google.android.ump.ConsentInformation.ConsentStatus.REQUIRED);
        UMP_consentStatus.put("UNKNOWN", com.google.android.ump.ConsentInformation.ConsentStatus.UNKNOWN);
        constants.put("UMP_CONSENT_STATUS", UMP_consentStatus);

        final Map<String, Object> UMP_consentType = new HashMap<>();
        UMP_consentType.put("NON_PERSONALIZED", com.google.android.ump.ConsentInformation.ConsentType.NON_PERSONALIZED);
        UMP_consentType.put("PERSONALIZED", com.google.android.ump.ConsentInformation.ConsentType.PERSONALIZED);
        UMP_consentType.put("UNKNOWN", com.google.android.ump.ConsentInformation.ConsentType.UNKNOWN);
        constants.put("UMP_CONSENT_TYPE", UMP_consentType);

        return constants;
    }

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

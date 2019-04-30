
package de.bnass.RNAdConsent;

import android.os.Bundle;

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
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;

public class RNAdConsentModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private static final String NON_PERSONALIZED = "non_personalized";
    private static final String PERSONALIZED = "personalized";
    private static final String PREFERS_AD_FREE = "prefers_ad_free";
    private static final String UNKNOWN = "unknown";

    private ConsentForm form;

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
        constants.put("UNKNOWN", UNKNOWN);
        return constants;
    }

    private void showConsentForm() {
        form.show();
    }

    private void setConsent(ConsentStatus status) {
        Bundle extras = new Bundle();
        extras.putString("npa", status.equals(ConsentStatus.NON_PERSONALIZED) ? "1" : "0");
        AdRequest request = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
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
            setConsent(consentStatus);
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
}

#import "RNAdConsent.h"
#import "RCTBridgeModule.h"
#if __has_include(<React/RCTConvert.h>)
#import <React/RCTConvert.h>
#elif __has_include("RCTConvert.h")
#import "RCTConvert.h"
#else
#import "React/RCTConvert.h" // Required when used as a Pod in a Swift project
#endif
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <PersonalizedAdConsent/PersonalizedAdConsent.h>

@implementation RNAdConsent

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

RCT_EXPORT_MODULE()

NSString *const NON_PERSONALIZED = @"non_personalized";
NSString *const PERSONALIZED = @"personalized";
NSString *const PREFERS_AD_FREE = @"prefers_ad_free";
NSString *const UNKNOWN_STATUS = @"unknown";

- (NSDictionary *)constantsToExport {
  return @{
    @"NON_PERSONALIZED" : NON_PERSONALIZED,
    @"PERSONALIZED" : PERSONALIZED,
    @"PREFERS_AD_FREE" : PREFERS_AD_FREE,
    @"UNKNOWN" : UNKNOWN_STATUS,
  };
}

RCT_EXPORT_METHOD(isRequestLocationInEeaOrUnknown
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  @try {
    resolve([NSNumber numberWithBool:PACConsentInformation.sharedInstance
                                         .requestLocationInEEAOrUnknown]);
  } @catch (NSError *error) {
    reject(@"isRequestLocationInEeaOrUnknown_error", error.localizedDescription,
           error);
  }
}

RCT_EXPORT_METHOD(setTagForUnderAgeOfConsent
                  : (BOOL)isUnderAgeOfConsent resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  @try {
    PACConsentInformation.sharedInstance.tagForUnderAgeOfConsent =
        isUnderAgeOfConsent;
    resolve(@(YES));
  } @catch (NSError *error) {
    reject(@"setTagForUnderAgeOfConsent_error", error.localizedDescription,
           error);
  }
}

RCT_EXPORT_METHOD(getAdProviders
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  @try {
    NSArray *adProviders = PACConsentInformation.sharedInstance.adProviders;
    NSMutableArray *adProvidersArray = [NSMutableArray array];
    NSInteger adProvidersLength = [adProviders count];

    for (int i = 0; i < adProvidersLength; i++) {
      PACAdProvider *adProvider = adProviders[i];
      NSDictionary *d = @{
        @"identifier" : [adProvider identifier],
        @"name" : [adProvider name],
        @"privacyPolicyUrl" : [adProvider privacyPolicyURL].absoluteString
      };
      [adProvidersArray addObject:d];
    }

    resolve(adProvidersArray);
  } @catch (NSError *error) {
    reject(@"getAdProviders_error", error.localizedDescription, error);
  }
}

RCT_EXPORT_METHOD(setConsentStatus
                  : (NSString *)status resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  @try {
    if ([status isEqualToString:PERSONALIZED]) {
      PACConsentInformation.sharedInstance.consentStatus = PACConsentStatusPersonalized;
    } else if ([status isEqualToString:NON_PERSONALIZED]) {
      PACConsentInformation.sharedInstance.consentStatus = PACConsentStatusNonPersonalized;
    } else {
      PACConsentInformation.sharedInstance.consentStatus = PACConsentStatusUnknown;
    }

    resolve(@(YES));
  } @catch (NSError *error) {
    reject(@"setConsentStatus_error", error.localizedDescription, error);
  }
}

RCT_EXPORT_METHOD(addTestDevice
                  : (NSString *)deviceId resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  @try {
    PACConsentInformation.sharedInstance.debugIdentifiers = @[ deviceId ];
    resolve(@(YES));
  } @catch (NSError *error) {
    reject(@"addTestDevice_error", error.localizedDescription, error);
  }
}

RCT_EXPORT_METHOD(requestConsentInfoUpdate
                  : (NSDictionary *)options resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  @try {
    [PACConsentInformation.sharedInstance
        requestConsentInfoUpdateForPublisherIdentifiers:@[
          [RCTConvert NSString:options[@"publisherId"]]
        ]
                                      completionHandler:^(
                                          NSError *_Nullable error) {
                                        if (error) {
                                          reject(
                                              @"requestConsentInfoUpdate_error",
                                              error.localizedDescription,
                                              error);
                                        } else {
                                          PACConsentStatus status =
                                              PACConsentInformation
                                                  .sharedInstance.consentStatus;
                                          if (status ==
                                              PACConsentStatusPersonalized) {
                                            resolve(PERSONALIZED);
                                          } else if (
                                              status ==
                                              PACConsentStatusNonPersonalized) {
                                            resolve(NON_PERSONALIZED);
                                          } else {
                                            resolve(UNKNOWN_STATUS);
                                          }
                                        }
                                      }];
  } @catch (NSError *error) {
    reject(@"requestConsentInfoUpdate_error", error.localizedDescription,
           error);
  }
}

RCT_EXPORT_METHOD(showGoogleConsentForm
                  : (NSDictionary *)options resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  @try {
    NSString *privacyUrlString =
        [RCTConvert NSString:options[@"privacyPolicyUrl"]];
    NSURL *privacyUrl = [NSURL URLWithString:privacyUrlString];
    PACConsentForm *form =
        [[PACConsentForm alloc] initWithApplicationPrivacyPolicyURL:privacyUrl];

    BOOL shouldOfferAdFree = [RCTConvert BOOL:options[@"shouldOfferAdFree"]];

    form.shouldOfferAdFree = shouldOfferAdFree;
    form.shouldOfferNonPersonalizedAds = YES;
    form.shouldOfferPersonalizedAds = YES;

    [form loadWithCompletionHandler:^(NSError *_Nullable error) {
      NSLog(@"Load complete. Error: %@", error);
      if (error) {
        reject(@"requestConsentInfoUpdate_error", error.localizedDescription,
               error);
      } else {
        [form presentFromViewController:[UIApplication sharedApplication]
                                            .delegate.window.rootViewController
                      dismissCompletion:^(NSError *_Nullable error,
                                          BOOL userPrefersAdFree) {
                        if (error) {
                          reject(@"requestConsentInfoUpdate_error",
                                 error.localizedDescription, error);
                        } else {
                          if (userPrefersAdFree) {
                            resolve(PREFERS_AD_FREE);
                          } else {

                            PACConsentStatus status =
                                PACConsentInformation.sharedInstance
                                    .consentStatus;
                            if (status == PACConsentStatusPersonalized) {
                              resolve(PERSONALIZED);
                            } else if (status ==
                                       PACConsentStatusNonPersonalized) {
                              resolve(NON_PERSONALIZED);
                            } else {
                              resolve(UNKNOWN_STATUS);
                            }
                          }
                        }
                      }];
      }
    }];
  } @catch (NSError *error) {
    reject(@"requestConsentInfoUpdate_error", error.localizedDescription,
           error);
  }
}

@end

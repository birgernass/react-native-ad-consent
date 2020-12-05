#import "RNAdConsent.h"
#import "RCTBridgeModule.h"
#if __has_include(<React/RCTConvert.h>)
#import <React/RCTConvert.h>
#elif __has_include("RCTConvert.h")
#import "RCTConvert.h"
#else
#import "React/RCTConvert.h" // Required when used as a Pod in a Swift project
#endif
#include <UserMessagingPlatform/UserMessagingPlatform.h>

@implementation RNAdConsent

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

RCT_EXPORT_MODULE()

- (NSDictionary *)constantsToExport {
    return @{
        @"UMP_CONSENT_STATUS" : @{
                @"NOT_REQUIRED" : @(UMPConsentStatusNotRequired),
                @"OBTAINED" : @(UMPConsentStatusObtained),
                @"REQUIRED" : @(UMPConsentStatusRequired),
                @"UNKNOWN" : @(UMPConsentStatusUnknown)
        },
        @"UMP_CONSENT_TYPE" : @{
                @"NON_PERSONALIZED" : @(UMPConsentTypeNonPersonalized),
                @"PERSONALIZED" : @(UMPConsentTypePersonalized),
                @"UNKNOWN" : @(UMPConsentTypeUnknown)
        }
    };
}

RCT_EXPORT_METHOD(UMP_requestConsentInfoUpdate
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
    @try {
        UMPRequestParameters *parameters = [[UMPRequestParameters alloc] init];
        
        [UMPConsentInformation.sharedInstance
         requestConsentInfoUpdateWithParameters:parameters
         completionHandler:^(NSError *_Nullable error) {
            if (error) {
                NSLog(@"RNAdConsent [UMP requestConsentInfoUpdate] error: %@", error.localizedDescription);
                reject(@"requestConsentInfoUpdate_error", error.localizedDescription, error);
            } else {
                bool isConsentFormAvailable = UMPConsentInformation.sharedInstance.formStatus == UMPFormStatusAvailable;
                bool isRequestLocationInEeaOrUnknown = UMPConsentInformation.sharedInstance.consentStatus != UMPConsentStatusNotRequired;
                
                NSLog(@"RNAdConsent [UMP requestConsentInfoUpdate] formStatus: %ld consentStatus: %ld consentType: %ld isConsentFormAvailable: %d isRequestLocationInEeaOrUnknown: %d", (long)UMPConsentInformation.sharedInstance.formStatus, (long)UMPConsentInformation.sharedInstance.consentStatus, (long)UMPConsentInformation.sharedInstance.consentType, isConsentFormAvailable, isRequestLocationInEeaOrUnknown);
                
                NSDictionary *payload = @{
                    @"consentStatus":@(UMPConsentInformation.sharedInstance.consentStatus),
                    @"consentType":@(UMPConsentInformation.sharedInstance.consentType),
                    @"isConsentFormAvailable": @(isConsentFormAvailable),
                    @"isRequestLocationInEeaOrUnknown": @(isRequestLocationInEeaOrUnknown)
                };
                
                resolve(payload);
            }
        }];
    } @catch (NSError *error) {
        NSLog(@"RNAdConsent [UMP requestConsentInfoUpdate] error: %@", error.localizedDescription);
        reject(@"requestConsentInfoUpdate_error", error.localizedDescription,
               error);
    }
}

RCT_EXPORT_METHOD(UMP_showConsentForm
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
    @try {
        [UMPConsentForm loadWithCompletionHandler:^(UMPConsentForm *form,
                                                    NSError *loadError) {
            if (loadError) {
                NSLog(@"RNAdConsent [UMP showConsentForm] error: %@", loadError.localizedDescription);
                reject(@"showConsentForm_error", loadError.localizedDescription, loadError);
            } else {
                [form
                 presentFromViewController:[UIApplication sharedApplication].delegate.window.rootViewController
                 completionHandler:^(NSError *_Nullable dismissError) {
                    if (dismissError) {
                        NSLog(@"RNAdConsent [UMP showConsentForm] error: %@", dismissError.localizedDescription);
                        reject(@"showConsentForm_error", dismissError.localizedDescription, dismissError);
                    } else {
                        NSLog(@"RNAdConsent [UMP showConsentForm] consentStatus: %ld consentType: %ld", (long)UMPConsentInformation.sharedInstance.consentStatus, (long)UMPConsentInformation.sharedInstance.consentType);
                        NSDictionary *payload = @{
                            @"consentStatus":@(UMPConsentInformation.sharedInstance.consentStatus),
                            @"consentType":@(UMPConsentInformation.sharedInstance.consentType)
                        };
                        resolve(payload);
                    }
                }];
            }
        }];
    } @catch (NSError *error) {
        NSLog(@"RNAdConsent [UMP showConsentForm] error: %@", error.localizedDescription);
        reject(@"showConsentForm_error", error.localizedDescription, error);
    }
}

RCT_EXPORT_METHOD(UMP_reset) {
    @try {
        [UMPConsentInformation.sharedInstance reset];
    } @catch (NSError *error) {
        NSLog(@"RNAdConsent [UMP reset] error: %@", error.localizedDescription);
    }
}

@end

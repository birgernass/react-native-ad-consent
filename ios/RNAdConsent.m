#import <React/RCTUtils.h>

#import <React/RCTConvert.h>
#include <UserMessagingPlatform/UserMessagingPlatform.h>
#import "RCTBridgeModule.h"
#import "RNAdConsent.h"

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
        @"UMP_DEBUG_GEOGRAPHY" : @{
                @"DISABLED" : @(UMPDebugGeographyDisabled),
                @"EEA" : @(UMPDebugGeographyEEA),
                @"NOT_EEA" : @(UMPDebugGeographyNotEEA)
        }
    };
}

RCT_EXPORT_METHOD(UMP_requestConsentInfoUpdate
                  : (NSDictionary *)options
                  : (RCTPromiseResolveBlock)resolve
                  : (RCTPromiseRejectBlock)reject) {
    @try {
        UMPRequestParameters *parameters = [[UMPRequestParameters alloc] init];
        UMPDebugSettings *debugSettings = [[UMPDebugSettings alloc] init];

        debugSettings.geography = [options[@"debugGeography"] integerValue] ?: UMPDebugGeographyDisabled;
        debugSettings.testDeviceIdentifiers =
            [options valueForKeyPath:@"testDeviceIds"] ?: [[NSMutableArray alloc] init];

        parameters.debugSettings = debugSettings;
        parameters.tagForUnderAgeOfConsent = [options[@"tagForUnderAgeOfConsent"] boolValue] ?: FALSE;

        [UMPConsentInformation.sharedInstance
         requestConsentInfoUpdateWithParameters:parameters
         completionHandler:^(NSError *_Nullable error) {
            if (error) {
                reject(@"requestConsentInfoUpdate_error", error.localizedDescription, error);
            } else {
                bool isConsentFormAvailable = UMPConsentInformation.sharedInstance.formStatus == UMPFormStatusAvailable;
                bool isRequestLocationInEeaOrUnknown = UMPConsentInformation.sharedInstance.consentStatus != UMPConsentStatusNotRequired;

                resolve(@{
                    @"consentStatus":@(UMPConsentInformation.sharedInstance.consentStatus),
                    @"isConsentFormAvailable": @(isConsentFormAvailable),
                    @"isRequestLocationInEeaOrUnknown": @(isRequestLocationInEeaOrUnknown)
                });
            }
        }];
    } @catch (NSError *error) {
        reject(@"requestConsentInfoUpdate_error", error.localizedDescription,
               error);
    }
}

RCT_EXPORT_METHOD(UMP_showConsentForm
                  : (RCTPromiseResolveBlock)resolve
                  : (RCTPromiseRejectBlock)reject) {
    @try {
        [UMPConsentForm loadWithCompletionHandler:^(UMPConsentForm *form,
                                                    NSError *loadError) {
            if (loadError) {
                reject(@"showConsentForm_error", loadError.localizedDescription, loadError);
            } else {
                [form
                 presentFromViewController:[UIApplication sharedApplication].delegate.window.rootViewController
                 completionHandler:^(NSError *_Nullable dismissError) {
                    if (dismissError) {
                        reject(@"showConsentForm_error", dismissError.localizedDescription, dismissError);
                    } else {
                        resolve(@{
                            @"consentStatus":@(UMPConsentInformation.sharedInstance.consentStatus)
                        });
                    }
                }];
            }
        }];
    } @catch (NSError *error) {
        reject(@"showConsentForm_error", error.localizedDescription, error);
    }
}

RCT_EXPORT_METHOD(UMP_reset) {
    [UMPConsentInformation.sharedInstance reset];
}

@end

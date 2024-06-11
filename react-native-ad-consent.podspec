require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-ad-consent"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-ad-consent
                   DESC
  s.homepage     = "https://github.com/birgernass/react-native-ad-consent"
  s.license      = { :type => "MIT", :file => "LICENSE" }
  s.authors      = "Birger Naß"
  s.platforms    = { :ios => "12.0" }
  s.source       = { :git => "https://github.com/birgernass/react-native-ad-consent.git", :tag => "#{s.version}" }

  s.frameworks   = "AppTrackingTransparency"
  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency "GoogleUserMessagingPlatform", "2.4.0"
end

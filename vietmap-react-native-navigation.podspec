require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

# TargetsToChangeToDynamic = ['MapboxMobileEvents']
TargetsToChangeToDynamic = []

$RNMBNAV = Object.new

def $RNMBNAV.post_install(installer)
  installer.pod_targets.each do |pod|
    if TargetsToChangeToDynamic.include?(pod.name)
      if pod.send(:build_type) != Pod::BuildType.dynamic_framework
        pod.instance_variable_set(:@build_type,Pod::BuildType.dynamic_framework)
        puts "* Changed #{pod.name} to `#{pod.send(:build_type)}`"
        fail "Unable to change build_type" unless mobile_events_target.send(:build_type) == Pod::BuildType.dynamic_framework
      end
    end
  end
end

def $RNMBNAV.pre_install(installer)
  installer.aggregate_targets.each do |target|
    target.pod_targets.select { |p| TargetsToChangeToDynamic.include?(p.name) }.each do |mobile_events_target|
      mobile_events_target.instance_variable_set(:@build_type,Pod::BuildType.dynamic_framework)
      puts "* Changed #{mobile_events_target.name} to #{mobile_events_target.send(:build_type)}"
      fail "Unable to change build_type" unless mobile_events_target.send(:build_type) == Pod::BuildType.dynamic_framework
    end
  end
end

Pod::Spec.new do |s|
  s.name         = "vietmap-react-native-navigation"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  VietMap turn-by-turn routing for React Native.
                   DESC
  s.homepage     = "https://github.com/vietmap-company/vietmap-react-native-navigation"
  s.license    = { :type => "BSD-3-Clause", :file => "LICENSE" }
  s.authors      = { "VietMap" => "maps-api.support@vietmap.vn" }
  s.platforms    = { :ios => "13.4" }
  s.source       = { :git => "https://github.com/vietmap-company/vietmap-react-native-navigation.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React-Core"
  s.dependency 'VietMapNavigation', '3.3.0'
  s.dependency 'VietMapCoreNavigation', '3.1.0'
  s.dependency 'VietmapTrackingSDK', '1.2.2'
  s.swift_version = '5.0'
end


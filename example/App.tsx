/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect} from 'react';
import {SafeAreaView, useColorScheme, View, Text, TouchableOpacity, StyleSheet, Alert} from 'react-native';
// import NavigationComponent from './NavigationComponent';
// import VMNavigation from './VMNavigationComponent';
import VietMapNavigationScreen from './Navigation/VietMapNavigation';
import {PermissionsAndroid} from 'react-native';
import { VietMapNavigationModule } from '../src/native_modules';

const Colors = {
  lighter: '#F3F3F3',
  darker: '#222222',
};

const App = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
    flex: 1,
  };

  useEffect(() => {
    const requestLocationPermission = async () => {
      try {
        await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          {
            title: 'Location Permission',
            message: 'VietMap needs access to your location',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          },
          
          
        );
      } catch (err) {
        console.warn(err);
      }
    };

    requestLocationPermission();
  }, []);

  return (
    <SafeAreaView style={backgroundStyle}>
      <VietMapNavigationScreen />
    </SafeAreaView>
  );
};

export default App;

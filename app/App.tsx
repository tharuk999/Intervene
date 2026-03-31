
// Install dependencies:
//   npx expo install @react-navigation/native @react-navigation/native-stack
//   npx expo install react-native-screens react-native-safe-area-context
//   npx expo install @expo/vector-icons
//   npx expo install @react-native-community/slider

import './global.css';
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import HomeScreen from './(tabs)/index';
import InterventionsChoiceScreen from './InterventionsChoiceScreen';
import InterventionSetupScreen from './InterventionsSetupScreen';
import SettingsScreen from '../components/SettingsScreen';

export type RootStackParamList = {
  Home: undefined;
  Interventions: undefined;
  InterventionSetup: { app: { id: string; name: string } };
  Settings: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="Home"
        screenOptions={{ headerShown: false, animation: 'slide_from_right' }}
      >
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen name="Interventions" component={InterventionsChoiceScreen} />
        <Stack.Screen name="InterventionSetup" component={InterventionSetupScreen} />
        <Stack.Screen name="Settings" component={SettingsScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
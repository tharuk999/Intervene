import React, { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  Switch,
  StatusBar,
} from 'react-native';
import Slider from '@react-native-community/slider';
import { Ionicons, Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { NavBar } from './navbar';

export default function SettingsScreen({ navigation }: any) {
  const [darkMode, setDarkMode] = useState(false);
  const [fontSize, setFontSize] = useState(0.5);

  return (
    <View className="flex-1 bg-[#F2F0EA]">
      <StatusBar barStyle="dark-content" backgroundColor="#F2F0EA" />

      {/* Header */}
      <View className="flex-row items-center px-5 pt-12 pb-2">
        <MaterialCommunityIcons name="leaf" size={20} color="#2D4A2D" />
        <Text className="text-[#2D4A2D] text-base font-semibold tracking-tight ml-2">
          Pause & Intent
        </Text>
      </View>

      <ScrollView showsVerticalScrollIndicator={false} className="flex-1 px-5">
        {/* Appearance Section */}
        <View className="mt-6 mb-2 flex-row items-baseline justify-between">
          <Text className="text-[#1C1C1C] text-2xl font-bold">Appearance</Text>
          <Text className="text-[#9A9A9A] text-xs tracking-widest">VISUALS</Text>
        </View>

        <View className="bg-white rounded-3xl overflow-hidden mb-6">
          {/* Dark Mode */}
          <View className="flex-row items-center px-4 py-4 border-b border-[#F0EDE7]">
            <View className="w-10 h-10 bg-[#D6E4F7] rounded-full items-center justify-center mr-3">
              <Ionicons name="moon" size={20} color="#2C5F8A" />
            </View>
            <View className="flex-1">
              <Text className="text-[#1C1C1C] text-base font-semibold">Dark Mode</Text>
              <Text className="text-[#9A9A9A] text-xs">Reduce eye strain at night</Text>
            </View>
            <Switch
              value={darkMode}
              onValueChange={setDarkMode}
              trackColor={{ false: '#D0D0D0', true: '#2D4A2D' }}
              thumbColor="white"
            />
          </View>

          {/* Font Size */}
          <View className="px-4 py-4 border-b border-[#F0EDE7]">
            <View className="flex-row items-center mb-3">
              <View className="w-10 h-10 bg-[#F5E6D0] rounded-full items-center justify-center mr-3">
                <MaterialCommunityIcons name="format-size" size={20} color="#8B5E2D" />
              </View>
              <View className="flex-1">
                <Text className="text-[#1C1C1C] text-base font-semibold">Font Size</Text>
              </View>
              <View className="bg-[#E8F0E8] px-3 py-1 rounded-full">
                <Text className="text-[#2D4A2D] text-xs font-semibold tracking-wide">MEDIUM</Text>
              </View>
            </View>
            <Slider
              value={fontSize}
              onValueChange={setFontSize}
              minimumValue={0}
              maximumValue={1}
              minimumTrackTintColor="#2D4A2D"
              maximumTrackTintColor="#D0D0D0"
              thumbTintColor="#2D4A2D"
              style={{ height: 28, marginHorizontal: 4 }}
            />
            <View className="flex-row justify-between mt-1 px-1">
              <Text className="text-[#9A9A9A] text-xs">A</Text>
              <Text className="text-[#9A9A9A] text-sm">A</Text>
              <Text className="text-[#9A9A9A] text-base">A</Text>
            </View>
          </View>

          {/* Font Style */}
          <View className="flex-row items-center px-4 py-4">
            <View className="w-10 h-10 bg-[#E8EDE8] rounded-full items-center justify-center mr-3">
              <Text className="text-[#2D4A2D] text-base font-bold">A</Text>
            </View>
            <View className="flex-1">
              <Text className="text-[#1C1C1C] text-base font-semibold">Font Style</Text>
              <Text className="text-[#9A9A9A] text-xs">Choose your reading voice</Text>
            </View>
            <View className="bg-[#EBEBEB] px-3 py-2 rounded-xl">
              <Text className="text-[#1C1C1C] text-xs font-medium italic">Editorial (Manrope)</Text>
            </View>
          </View>
        </View>

        {/* Preferences Section */}
        <View className="mb-2 flex-row items-baseline justify-between">
          <Text className="text-[#1C1C1C] text-2xl font-bold">Preferences</Text>
          <TouchableOpacity>
            <Feather name="sliders" size={18} color="#9A9A9A" />
          </TouchableOpacity>
        </View>

        <View className="flex-row gap-3 mb-6">
          {/* Mindful Alerts */}
          <View className="flex-1 bg-[#D6E4F7] rounded-3xl p-5 aspect-square justify-between">
            <Ionicons name="notifications" size={32} color="#2C5F8A" />
            <View>
              <Text className="text-[#1C2B4A] text-base font-bold mt-4">Mindful Alerts</Text>
              <Text className="text-[#2C5F8A] text-xs tracking-widest mt-0.5">ENABLED</Text>
            </View>
          </View>

          {/* Focus Blocks */}
          <View className="flex-1 bg-[#F5E6D0] rounded-3xl p-5 aspect-square justify-between">
            <Ionicons name="timer" size={32} color="#7A4A1A" />
            <View>
              <Text className="text-[#3D1C00] text-base font-bold mt-4">Focus Blocks</Text>
              <Text className="text-[#8B5E2D] text-xs tracking-widest mt-0.5">4 SESSIONS TODAY</Text>
            </View>
          </View>
        </View>

        {/* Privacy & Safety */}
        <Text className="text-[#1C1C1C] text-2xl font-bold mb-3">Privacy & Safety</Text>
        <View className="bg-[#EBEBEB] rounded-2xl mb-8">
          <TouchableOpacity className="flex-row items-center px-4 py-4">
            <View className="w-8 h-8 items-center justify-center mr-3">
              <Ionicons name="lock-closed" size={20} color="#5A5A5A" />
            </View>
            <Text className="flex-1 text-[#1C1C1C] text-base font-medium">Privacy Settings</Text>
            <Ionicons name="chevron-forward" size={18} color="#C0C0C0" />
          </TouchableOpacity>
        </View>

        {/* Version */}
        <Text className="text-center text-[#B0B0B0] text-xs tracking-widest mb-8">
          PAUSE & INTENT V2.4.0
        </Text>

        <View className="h-20" />
      </ScrollView>

      {/* Bottom Nav */}
      {/* <View className="absolute bottom-0 left-0 right-0 bg-[#F2F0EA] border-t border-[#E0DDD7] flex-row items-center justify-around px-6 py-3 pb-6">
        <TouchableOpacity
          className="items-center"
          onPress={() => navigation?.navigate('Home')}
        >
          <Ionicons name="home-outline" size={22} color="#9A9A9A" />
          <Text className="text-[#9A9A9A] text-xs mt-1 tracking-widest">HOME</Text>
        </TouchableOpacity>
        <TouchableOpacity
          className="items-center"
          onPress={() => navigation?.navigate('Interventions')}
        >
          <Feather name="activity" size={22} color="#9A9A9A" />
          <Text className="text-[#9A9A9A] text-xs mt-1 tracking-widest">INTERVENTIONS</Text>
        </TouchableOpacity>
        <TouchableOpacity className="items-center">
          <View className="bg-[#2D4A2D] rounded-full px-4 py-2 flex-row items-center gap-1">
            <Ionicons name="settings" size={16} color="white" />
            <Text className="text-white text-xs font-semibold tracking-widest ml-1">SETTINGS</Text>
          </View>
        </TouchableOpacity>
      </View> */}
      <NavBar/>
    </View>
  );
}
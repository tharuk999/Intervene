import React, { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StatusBar,
} from 'react-native';
import Slider from '@react-native-community/slider';
import { Ionicons, Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { NavBar } from '@/components/navbar';

interface InterventionItem {
  id: string;
  icon: string;
  iconLib: 'ionicons' | 'material';
  iconColor: string;
  title: string;
  description: string;
  enabled: boolean;
  settingLabel: string;
  settingValue: string;
  settingValueColor?: string;
  sliderValue: number;
  sliderMin: number;
  sliderMax: number;
  showPlusMinus?: boolean;
  highlighted?: boolean;
}

const INITIAL_INTERVENTIONS: InterventionItem[] = [
  {
    id: 'tap_delay',
    icon: 'hand-left-outline',
    iconLib: 'ionicons',
    iconColor: '#2D4A2D',
    title: 'Tap Delay',
    description: 'Adds a slight hesitation before processing a tap.',
    enabled: true,
    settingLabel: 'INTENSITY',
    settingValue: '0.5s',
    sliderValue: 0.3,
    sliderMin: 0,
    sliderMax: 1,
  },
  {
    id: 'swipe_decel',
    icon: 'gesture-swipe-down',
    iconLib: 'material',
    iconColor: '#5A5A5A',
    title: 'Swipe Deceleration',
    description: 'Increases friction when scrolling rapidly.',
    enabled: false,
    settingLabel: 'FRICTION LEVEL',
    settingValue: 'Medium',
    sliderValue: 0.5,
    sliderMin: 0,
    sliderMax: 1,
  },
  {
    id: 'tap_shift',
    icon: 'navigate-outline',
    iconLib: 'ionicons',
    iconColor: '#2D4A2D',
    title: 'Tap Shift',
    description: 'Slightly offsets the registered tap location.',
    enabled: true,
    settingLabel: 'OFFSET VARIANCE',
    settingValue: '12px',
    sliderValue: 0.45,
    sliderMin: 0,
    sliderMax: 1,
  },
  {
    id: 'multi_finger',
    icon: 'hand-right-outline',
    iconLib: 'ionicons',
    iconColor: '#3D5C3D',
    title: 'Multi-Finger Swipe',
    description: 'Requires two fingers to scroll through feeds.',
    enabled: false,
    settingLabel: '',
    settingValue: '',
    sliderValue: 0,
    sliderMin: 0,
    sliderMax: 1,
    highlighted: true,
  },
  {
    id: 'tap_prolong',
    icon: 'finger-print-outline',
    iconLib: 'ionicons',
    iconColor: '#5A5A5A',
    title: 'Tap Prolong',
    description: 'Requires holding for a set duration before registering.',
    enabled: false,
    settingLabel: 'HOLD DURATION',
    settingValue: '1.0s',
    sliderValue: 0.5,
    sliderMin: 0,
    sliderMax: 1,
    showPlusMinus: true,
  },
  {
    id: 'swipe_delay',
    icon: 'timer-outline',
    iconLib: 'ionicons',
    iconColor: '#C0392B',
    title: 'Swipe Delay',
    description: 'Introduces a pause between consecutive swipes.',
    enabled: false,
    settingLabel: 'INTER-SCROLL WAIT',
    settingValue: '2.5s',
    settingValueColor: '#E74C3C',
    sliderValue: 0.7,
    sliderMin: 0,
    sliderMax: 1,
  },
];

export default function InterventionSetupScreen({ navigation }: any) {
  const [interventions, setInterventions] = useState<InterventionItem[]>(INITIAL_INTERVENTIONS);

  const toggleIntervention = (id: string) => {
    setInterventions((prev: InterventionItem[]) =>
      prev.map((i: InterventionItem) =>
        i.id === id ? { ...i, enabled: !i.enabled } : i
      )
    );
  };

  const activeCount = interventions.filter((i: InterventionItem) => i.enabled).length;

  return (
    <View className="flex-1 bg-[#F2F0EA]">
      <StatusBar barStyle="dark-content" backgroundColor="#F2F0EA" />

      {/* Header */}
      <View className="flex-row items-center justify-between px-5 pt-12 pb-2">
        <View className="flex-row items-center gap-2">
          <MaterialCommunityIcons name="leaf" size={20} color="#2D4A2D" />
          <Text className="text-[#2D4A2D] text-base font-semibold tracking-tight">
            Pause & Intent
          </Text>
        </View>
        <View className="flex-row gap-4">
          <TouchableOpacity>
            <Feather name="search" size={20} color="#2D4A2D" />
          </TouchableOpacity>
          <TouchableOpacity>
            <Ionicons name="person-circle-outline" size={22} color="#2D4A2D" />
          </TouchableOpacity>
        </View>
      </View>

      <ScrollView showsVerticalScrollIndicator={false} className="flex-1 px-5">
        {/* App Header */}
        <View className="flex-row items-center justify-between mt-4 mb-5">
          <View className="flex-row items-center gap-3">
            <View className="w-12 h-12 bg-[#1C1C1C] rounded-xl items-center justify-center">
              <Ionicons name="phone-portrait-outline" size={22} color="white" />
            </View>
            <View>
              <Text className="text-[#8A8A8A] text-xs tracking-widest">ACTIVE APPLICATION</Text>
              <Text className="text-[#1C1C1C] text-2xl font-bold">Instagram</Text>
            </View>
          </View>
          <Text className="text-[#8A8A8A] text-xs tracking-wide">Global Active</Text>
        </View>

        {/* Interventions */}
        <View className="gap-3">
          {interventions.map((item: InterventionItem) => (
            <View
              key={item.id}
              className={`rounded-2xl p-4 ${item.highlighted ? 'bg-[#E8EDE8]' : 'bg-white'}`}
            >
              {/* Row: Icon + Title + Toggle */}
              <View className="flex-row items-start justify-between mb-1">
                <View className="flex-row items-center gap-3 flex-1">
                  <View className="w-8 h-8 items-center justify-center">
                    <Ionicons
                      name={item.icon as any}
                      size={22}
                      color={item.iconColor}
                    />
                  </View>
                  <View className="flex-1">
                    <Text className="text-[#1C1C1C] text-base font-semibold">{item.title}</Text>
                    <Text className="text-[#8A8A8A] text-xs mt-0.5 leading-relaxed">
                      {item.description}
                    </Text>
                  </View>
                </View>

                {/* Toggle */}
                <TouchableOpacity
                  className={`w-12 h-7 rounded-full ml-2 mt-0.5 items-center justify-center ${
                    item.enabled ? 'bg-[#2D4A2D]' : 'bg-[#D0D0D0]'
                  }`}
                  onPress={() => toggleIntervention(item.id)}
                  activeOpacity={0.8}
                >
                  <View
                    className={`w-5 h-5 bg-white rounded-full shadow ${
                      item.enabled ? 'translate-x-2.5' : '-translate-x-2.5'
                    }`}
                  />
                </TouchableOpacity>
              </View>

              {/* Slider Section */}
              {item.settingLabel !== '' && (
                <View className="mt-3 pl-11">
                  <View className="flex-row items-center justify-between mb-1">
                    <Text className="text-[#9A9A9A] text-xs tracking-widest">{item.settingLabel}</Text>
                    <Text
                      className="text-xs font-medium"
                      style={{ color: item.settingValueColor ?? '#9A9A9A' }}
                    >
                      {item.settingValue}
                    </Text>
                  </View>

                  {item.showPlusMinus ? (
                    <View className="flex-row items-center gap-2">
                      <TouchableOpacity className="w-6 h-6 border border-[#C0C0C0] rounded-full items-center justify-center">
                        <Ionicons name="remove" size={14} color="#555" />
                      </TouchableOpacity>
                      <View className="flex-1">
                        <Slider
                          value={item.sliderValue}
                          minimumValue={item.sliderMin}
                          maximumValue={item.sliderMax}
                          minimumTrackTintColor="#2D4A2D"
                          maximumTrackTintColor="#D0D0D0"
                          thumbTintColor="#2D4A2D"
                          style={{ height: 24 }}
                        />
                      </View>
                      <TouchableOpacity className="w-6 h-6 border border-[#C0C0C0] rounded-full items-center justify-center">
                        <Ionicons name="add" size={14} color="#555" />
                      </TouchableOpacity>
                    </View>
                  ) : (
                    <Slider
                      value={item.sliderValue}
                      minimumValue={item.sliderMin}
                      maximumValue={item.sliderMax}
                      minimumTrackTintColor="#2D4A2D"
                      maximumTrackTintColor="#D0D0D0"
                      thumbTintColor="#2D4A2D"
                      style={{ height: 24 }}
                    />
                  )}
                </View>
              )}
            </View>
          ))}
        </View>

        {/* Intention Summary */}
        <View className="bg-white rounded-3xl p-5 mt-5 mb-8">
          <Text className="text-[#1C1C1C] text-xl font-bold mb-4">Intention Summary</Text>
          <View className="flex-row justify-between mb-2">
            <Text className="text-[#6B6B6B] text-sm">Active Interventions</Text>
            <Text className="text-[#1C1C1C] text-sm font-semibold">{activeCount} Active</Text>
          </View>
          <View className="flex-row justify-between mb-5">
            <Text className="text-[#6B6B6B] text-sm">Friction Coefficient</Text>
            <Text className="text-[#2D7A2D] text-sm font-semibold">High (0.82)</Text>
          </View>
          <TouchableOpacity className="bg-[#2D4A2D] rounded-full py-4 items-center">
            <Text className="text-white text-base font-semibold">Update Application Profile</Text>
          </TouchableOpacity>
        </View>

        <View className="h-20" />
      </ScrollView>

      {/* Bottom Nav */}
      {/* <View className="absolute bottom-0 left-0 right-0 bg-[#F2F0EA] border-t border-[#E0DDD7] flex-row items-center justify-around px-6 py-3 pb-6">
        <TouchableOpacity
          className="items-center"
          onPress={() => router.push('/')}
        >
          <Ionicons name="home-outline" size={22} color="#9A9A9A" />
          <Text className="text-[#9A9A9A] text-xs mt-1 tracking-widest">HOME</Text>
        </TouchableOpacity>
        <TouchableOpacity className="items-center">
          <View className="bg-[#2D4A2D] rounded-full px-4 py-2">
            <Text className="text-white text-xs font-semibold tracking-widest">SETUP</Text>
          </View>
        </TouchableOpacity>
        <TouchableOpacity
          className="items-center"
          onPress={() => router.push('/settings')}
        >
          <Ionicons name="settings-outline" size={22} color="#9A9A9A" />
          <Text className="text-[#9A9A9A] text-xs mt-1 tracking-widest">SETTINGS</Text>
        </TouchableOpacity>
      </View> */}
      <NavBar/>
    </View>
  );
}
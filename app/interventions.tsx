import React, { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  TextInput,
  StatusBar,
} from 'react-native';
import { router } from 'expo-router';
import { Ionicons, Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { NavBar } from '@/components/navbar';

const APPS = [
  { id: 'instagram', name: 'Instagram', activeCount: 3, hasInterventions: true, bgColor: '#E8431A', iconName: 'camera' },
  { id: 'tiktok', name: 'TikTok', activeCount: 1, hasInterventions: true, bgColor: '#1C1C1C', iconName: 'musical-notes' },
  { id: 'twitter', name: 'Twitter / X', activeCount: 0, hasInterventions: false, bgColor: '#D0D0D0', iconName: 'close' },
  { id: 'reddit', name: 'Reddit', activeCount: 0, hasInterventions: false, bgColor: '#D0D0D0', iconName: 'chatbubbles' },
  { id: 'youtube', name: 'YouTube', activeCount: 2, hasInterventions: true, bgColor: '#FF0000', iconName: 'play' },
];

export default function InterventionsScreen() {
  const [search, setSearch] = useState('');
  const filtered = APPS.filter(a => a.name.toLowerCase().includes(search.toLowerCase()));

  return (
    <View className="flex-1 bg-[#F2F0EA]">
      <StatusBar barStyle="dark-content" backgroundColor="#F2F0EA" />

      {/* Header */}
      <View className="flex-row items-center justify-between px-5 pt-12 pb-2">
        <View className="flex-row items-center gap-2">
          <MaterialCommunityIcons name="leaf" size={20} color="#2D4A2D" />
          <Text className="text-[#2D4A2D] text-base font-semibold tracking-tight">Pause & Intent</Text>
        </View>
        <View className="flex-row gap-4">
          <TouchableOpacity><Feather name="search" size={20} color="#2D4A2D" /></TouchableOpacity>
          <TouchableOpacity><Ionicons name="person-circle-outline" size={22} color="#2D4A2D" /></TouchableOpacity>
        </View>
      </View>

      <ScrollView showsVerticalScrollIndicator={false} className="flex-1 px-5">
        <View className="mt-6 mb-2">
          <View className="flex-row items-baseline justify-between">
            <Text className="text-[#1C1C1C] text-3xl font-bold">Select Application</Text>
            <Text className="text-[#9A9A9A] text-xs tracking-widest">DIRECTORY</Text>
          </View>
          <Text className="text-[#6B6B6B] text-sm mt-1 leading-relaxed">
            Choose the spaces where you wish to{'\n'}cultivate more intentional digital presence.
          </Text>
        </View>

        <View className="bg-[#E8E5DF] rounded-2xl flex-row items-center px-4 py-3 mt-5 mb-6">
          <Feather name="sliders" size={18} color="#9A9A9A" />
          <TextInput
            value={search}
            onChangeText={setSearch}
            placeholder="Filter your applications..."
            placeholderTextColor="#9A9A9A"
            className="flex-1 ml-3 text-[#1C1C1C] text-sm"
          />
        </View>

        <View className="gap-3">
          {filtered.map(app => (
            <TouchableOpacity
              key={app.id}
              className={`rounded-2xl px-4 py-4 flex-row items-center ${app.hasInterventions ? 'bg-white' : 'bg-[#F0EDE7]'}`}
              onPress={() => app.hasInterventions && router.push('/intervention-setup')}
            >
              <View className="w-12 h-12 rounded-xl items-center justify-center mr-4" style={{ backgroundColor: app.bgColor }}>
                <Ionicons name={app.iconName as any} size={22} color="white" />
              </View>
              <View className="flex-1">
                <Text className={`text-base font-semibold ${app.hasInterventions ? 'text-[#1C1C1C]' : 'text-[#9A9A9A]'}`}>
                  {app.name}
                </Text>
                {app.hasInterventions ? (
                  <View className="flex-row items-center gap-1 mt-0.5">
                    <View className="w-1.5 h-1.5 rounded-full bg-[#2D4A2D]" />
                    <Text className="text-[#6B6B6B] text-xs">
                      {app.activeCount} Active Intervention{app.activeCount !== 1 ? 's' : ''}
                    </Text>
                  </View>
                ) : (
                  <Text className="text-[#B0B0B0] text-xs mt-0.5">No Friction applied</Text>
                )}
              </View>
              {app.hasInterventions ? (
                <Ionicons name="chevron-forward" size={18} color="#C0C0C0" />
              ) : (
                <View className="w-7 h-7 rounded-full border border-[#C0C0C0] items-center justify-center">
                  <Ionicons name="add" size={16} color="#9A9A9A" />
                </View>
              )}
            </TouchableOpacity>
          ))}
        </View>

        <View className="bg-[#E8E5DF] rounded-3xl p-5 mt-6 mb-32 items-center">
          <Text className="text-[#6B6B6B] text-sm mb-3">Can't find an application in the list?</Text>
          <TouchableOpacity className="bg-[#2D4A2D] rounded-full py-3 px-6">
            <Text className="text-white text-sm font-medium">Register Manual Intervention</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>

      {/* Bottom Nav */}
      {/* <View className="absolute bottom-0 left-0 right-0 bg-[#F2F0EA] border-t border-[#E0DDD7] flex-row items-center justify-around px-6 py-3 pb-6">
        <TouchableOpacity className="items-center" onPress={() => router.push('/')}>
          <Ionicons name="home-outline" size={22} color="#9A9A9A" />
          <Text className="text-[#9A9A9A] text-xs mt-1 tracking-widest">HOME</Text>
        </TouchableOpacity>
        <TouchableOpacity className="items-center">
          <View className="bg-[#2D4A2D] rounded-full px-4 py-2 flex-row items-center gap-1">
            <Ionicons name="grid" size={18} color="white" />
            <Text className="text-white text-xs font-semibold tracking-widest ml-1">INTERVENTIONS</Text>
          </View>
        </TouchableOpacity>
        <TouchableOpacity className="items-center" onPress={() => router.push('/settings')}>
          <Ionicons name="settings-outline" size={22} color="#9A9A9A" />
          <Text className="text-[#9A9A9A] text-xs mt-1 tracking-widest">SETTINGS</Text>
        </TouchableOpacity>
      </View> */}
      <NavBar/>
    </View>
  );
}
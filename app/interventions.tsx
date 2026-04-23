import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  TextInput,
  StatusBar,
  ActivityIndicator,
} from 'react-native';
import { router } from 'expo-router';
import { Ionicons, Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { NavBar } from '@/components/navbar';
import { AppProfile } from '@/hooks/useInterveneApi';

const BASE_URL = 'http://10.0.2.2:8080/api';

/** Map well-known package names to a colour and icon; fall back to a neutral default. */
function appMeta(packageName: string): { bgColor: string; iconName: string } {
  if (packageName.includes('instagram')) return { bgColor: '#E8431A', iconName: 'camera' };
  if (packageName.includes('tiktok'))    return { bgColor: '#1C1C1C', iconName: 'musical-notes' };
  if (packageName.includes('twitter') || packageName.includes('x.com'))
    return { bgColor: '#1DA1F2', iconName: 'close' };
  if (packageName.includes('reddit'))    return { bgColor: '#FF4500', iconName: 'chatbubbles' };
  if (packageName.includes('youtube'))   return { bgColor: '#FF0000', iconName: 'play' };
  return { bgColor: '#6B6B6B', iconName: 'apps' };
}

export default function InterventionsScreen() {
  const [search, setSearch] = useState('');
  const [profiles, setProfiles] = useState<AppProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await fetch(`${BASE_URL}/profiles`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data: AppProfile[] = await res.json();
        setProfiles(data);
      } catch (e: any) {
        setFetchError(e.message ?? 'Cannot reach backend');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const filtered = profiles.filter(p =>
      p.appName.toLowerCase().includes(search.toLowerCase()) ||
      p.packageName.toLowerCase().includes(search.toLowerCase())
  );

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

          {/* Loading / error states */}
          {loading && (
              <ActivityIndicator color="#2D4A2D" style={{ marginTop: 32 }} />
          )}
          {fetchError && (
              <Text className="text-[#C0392B] text-sm text-center mt-8">{fetchError}</Text>
          )}

          <View className="gap-3">
            {filtered.map(profile => {
              const activeCount = profile.interventions.filter(i => i.enabled).length;
              const hasInterventions = activeCount > 0;
              const { bgColor, iconName } = appMeta(profile.packageName);

              return (
                  <TouchableOpacity
                      key={profile.packageName}
                      className={`rounded-2xl px-4 py-4 flex-row items-center ${hasInterventions ? 'bg-white' : 'bg-[#F0EDE7]'}`}
                      onPress={() => router.push({
                        pathname: '/intervention-setup',
                        params: { packageName: profile.packageName },
                      })}
                  >
                    <View className="w-12 h-12 rounded-xl items-center justify-center mr-4" style={{ backgroundColor: bgColor }}>
                      <Ionicons name={iconName as any} size={22} color="white" />
                    </View>
                    <View className="flex-1">
                      <Text className={`text-base font-semibold ${hasInterventions ? 'text-[#1C1C1C]' : 'text-[#9A9A9A]'}`}>
                        {profile.appName}
                      </Text>
                      {hasInterventions ? (
                          <View className="flex-row items-center gap-1 mt-0.5">
                            <View className="w-1.5 h-1.5 rounded-full bg-[#2D4A2D]" />
                            <Text className="text-[#6B6B6B] text-xs">
                              {activeCount} Active Intervention{activeCount !== 1 ? 's' : ''}
                            </Text>
                          </View>
                      ) : (
                          <Text className="text-[#B0B0B0] text-xs mt-0.5">No Friction applied</Text>
                      )}
                    </View>
                    <Ionicons name="chevron-forward" size={18} color="#C0C0C0" />
                  </TouchableOpacity>
              );
            })}
          </View>

          <View className="bg-[#E8E5DF] rounded-3xl p-5 mt-6 mb-32 items-center">
            <Text className="text-[#6B6B6B] text-sm mb-3">Can't find an application in the list?</Text>
            <TouchableOpacity className="bg-[#2D4A2D] rounded-full py-3 px-6">
              <Text className="text-white text-sm font-medium">Register Manual Intervention</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>

        <NavBar />
      </View>
  );
}
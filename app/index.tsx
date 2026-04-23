import React, { useEffect } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    StatusBar,
    ActivityIndicator,
} from 'react-native';
import { router } from 'expo-router';
import { Ionicons, Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { NavBar } from '@/components/navbar';
import { useInterveneApi } from '@/hooks/useInterveneApi';

/** Format milliseconds → "2h 14m" */
function formatMs(ms: number): string {
    const h = Math.floor(ms / 3_600_000);
    const m = Math.floor((ms % 3_600_000) / 60_000);
    if (h === 0) return `${m}m`;
    if (m === 0) return `${h}h`;
    return `${h}h ${m}m`;
}

export default function HomeScreen() {
    const { dashboard, loading, error, fetchDashboard } = useInterveneApi();

    // Refresh every 60s while screen is mounted
    useEffect(() => {
        const id = setInterval(fetchDashboard, 60_000);
        return () => clearInterval(id);
    }, [fetchDashboard]);

    const screenTime   = dashboard?.totalScreenTimeMs    ?? 0;
    const resisted     = dashboard?.resistedUrges         ?? 0;
    const frictionApps = dashboard?.activeFrictionApps    ?? 0;
    const pctChange    = dashboard?.percentChangeVsYesterday ?? 0;
    const pctLabel     = pctChange <= 0
        ? `${Math.abs(pctChange)}% vs yesterday`
        : `+${pctChange}% vs yesterday`;
    const pctColor     = pctChange <= 0 ? '#2D4A2D' : '#C0392B';

    return (
        <View className="flex-1 bg-[#F2F0EA]">
            <StatusBar barStyle="dark-content" backgroundColor="#F2F0EA" />

            {/* Header */}
            <View className="flex-row items-center justify-between px-5 pt-12 pb-2">
                <View className="flex-row items-center gap-2">
                    <MaterialCommunityIcons name="leaf" size={20} color="#2D4A2D" />
                    <Text className="text-[#2D4A2D] text-base font-semibold tracking-tight">
                        Intervene
                    </Text>
                </View>
                <TouchableOpacity onPress={fetchDashboard}>
                    <Feather name="refresh-cw" size={20} color="#2D4A2D" />
                </TouchableOpacity>
            </View>

            <ScrollView showsVerticalScrollIndicator={false} className="flex-1">
                {/* Welcome */}
                <View className="px-5 pt-6 pb-4 flex-row items-start justify-between">
                    <View className="flex-1">
                        <Text className="text-[#1C1C1C] text-4xl font-light leading-tight">
                            Welcome back{'\n'}to your{' '}
                            <Text className="italic font-light">quiet</Text>
                        </Text>
                        <Text className="text-[#1C1C1C] text-4xl font-light">space.</Text>
                    </View>
                    <View className="items-end pt-2">
                        <Text className="text-[#6B6B6B] text-xs tracking-widest">DAY 14</Text>
                        <Text className="text-[#6B6B6B] text-xs tracking-wide">Mindful</Text>
                        <Text className="text-[#6B6B6B] text-xs tracking-wide">Streak</Text>
                    </View>
                </View>

                {/* Hero Image Card */}
                <View className="mx-5 rounded-2xl overflow-hidden h-44">
                    <View className="absolute inset-0 bg-[#C4A882]" />
                    <View className="absolute bottom-0 left-0 right-0 h-24 bg-[#8B6914] opacity-40 rounded-t-[60px]" />
                    <View className="absolute bottom-0 left-0 right-0 h-16 bg-[#5C3D11] opacity-60 rounded-t-[80px]" />
                    <View className="absolute bottom-0 left-0 right-0 h-10 bg-[#2D1810] opacity-80 rounded-t-[100px]" />
                    <View className="absolute bottom-4 left-4">
                        <Text className="text-white/70 text-xs tracking-widest mb-1">CURRENT FOCUS</Text>
                        <Text className="text-white text-2xl font-semibold">Digital Silence</Text>
                    </View>
                </View>

                {/* Today's Pulse */}
                <View className="px-5 mt-6">
                    <View className="flex-row items-center justify-between mb-3">
                        <Text className="text-[#1C1C1C] text-xl font-semibold">Today's Pulse</Text>
                        {!loading && (
                            <View className="bg-[#E8F0E8] px-3 py-1 rounded-full">
                                <Text style={{ color: pctColor }} className="text-xs font-medium">
                                    {pctLabel}
                                </Text>
                            </View>
                        )}
                    </View>

                    {/* Screen Intent Card */}
                    <View className="bg-white rounded-2xl p-4 mb-3">
                        <Text className="text-[#8A8A8A] text-xs tracking-widest mb-2">SCREEN INTENT</Text>
                        {loading ? (
                            <ActivityIndicator color="#2D4A2D" style={{ alignSelf: 'flex-start', marginVertical: 8 }} />
                        ) : (
                            <Text className="text-[#1C1C1C] text-5xl font-light">
                                {formatMs(screenTime)}
                            </Text>
                        )}
                        {error ? (
                            <Text className="text-[#C0392B] text-xs mt-2">{error} — tap ↻ to retry</Text>
                        ) : (
                            <View className="flex-row items-center justify-between mt-2">
                                <Text className="text-[#6B6B6B] text-sm">
                                    You've reached for your{'\n'}device 45 times today.
                                </Text>
                                <Ionicons name="bar-chart-outline" size={24} color="#C8C8C8" />
                            </View>
                        )}
                    </View>

                    {/* Bottom Stats Row */}
                    <View className="flex-row gap-3 mb-3">
                        <View className="flex-1 bg-[#EBEBEB] rounded-2xl p-4">
                            <View className="w-8 h-8 rounded-full bg-[#D0D0D0] items-center justify-center mb-3">
                                <Ionicons name="remove" size={18} color="#555" />
                            </View>
                            <Text className="text-[#8A8A8A] text-xs tracking-widest mb-1">
                                FRICTION{'\n'}ACTIVE
                            </Text>
                            {loading ? (
                                <ActivityIndicator color="#555" size="small" />
                            ) : (
                                <Text className="text-[#1C1C1C] text-2xl font-semibold">
                                    {frictionApps} Apps
                                </Text>
                            )}
                        </View>
                        <View className="flex-1 bg-[#F5E6D0] rounded-2xl p-4">
                            <View className="w-8 h-8 rounded-full bg-[#E8C898] items-center justify-center mb-3">
                                <MaterialCommunityIcons name="head-cog-outline" size={18} color="#7A5C2E" />
                            </View>
                            <Text className="text-[#8A8A8A] text-xs tracking-widest mb-1">
                                RESISTED URGES
                            </Text>
                            {loading ? (
                                <ActivityIndicator color="#7A5C2E" size="small" />
                            ) : (
                                <Text className="text-[#1C1C1C] text-2xl font-semibold">
                                    {resisted} times
                                </Text>
                            )}
                        </View>
                    </View>
                </View>

                {/* Design Your Boundaries CTA */}
                <View className="mx-5 mt-2 bg-[#1C2B1C] rounded-3xl p-6">
                    <Text className="text-white text-2xl font-semibold mb-2">
                        Design your boundaries.
                    </Text>
                    <Text className="text-white/60 text-sm leading-relaxed mb-5">
                        Introduce intentional pauses to your most distracting apps. Choose between
                        breathwork, reflection, or simple delays.
                    </Text>
                    <TouchableOpacity
                        className="bg-[#2D4A2D] rounded-full py-3 px-6 flex-row items-center justify-center gap-2"
                        onPress={() => router.push('/interventions')}
                    >
                        <Text className="text-white text-base font-medium">Configure Interventions</Text>
                        <Text className="text-white text-base">→</Text>
                    </TouchableOpacity>
                </View>

                {/* Reflections */}
                <View className="px-5 mt-6 mb-4">
                    <Text className="text-[#1C1C1C] text-xl font-semibold mb-4">Reflections</Text>

                    <View className="flex-row items-start gap-3 mb-5">
                        <View className="w-10 h-10 bg-[#EBEBEB] rounded-xl items-center justify-center">
                            <MaterialCommunityIcons name="meditation" size={20} color="#555" />
                        </View>
                        <View className="flex-1">
                            <Text className="text-[#1C1C1C] text-sm font-semibold mb-1">
                                10:45 AM — Mindfulness Gap
                            </Text>
                            <Text className="text-[#6B6B6B] text-sm leading-relaxed">
                                You paused for 30 seconds before opening Instagram. Decided to read a
                                book instead.
                            </Text>
                        </View>
                    </View>

                    <View className="flex-row items-start gap-3">
                        <View className="w-10 h-10 bg-[#EBEBEB] rounded-xl items-center justify-center">
                            <Ionicons name="time-outline" size={20} color="#555" />
                        </View>
                        <View className="flex-1">
                            <Text className="text-[#1C1C1C] text-sm font-semibold mb-1">
                                08:12 AM — Wake Up Routine
                            </Text>
                            <Text className="text-[#6B6B6B] text-sm leading-relaxed">
                                First device interaction occurred 45 minutes after waking up. Peaceful
                                start.
                            </Text>
                        </View>
                    </View>
                </View>

                <View className="h-24" />
            </ScrollView>

            {/* Floating pause button */}
            <View className="absolute bottom-20 right-5">
                <TouchableOpacity className="w-12 h-12 bg-[#1C2B1C] rounded-full items-center justify-center shadow-lg">
                    <Ionicons name="pause" size={20} color="white" />
                </TouchableOpacity>
            </View>

            <NavBar />
        </View>
    );
}
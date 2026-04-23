import React, { useEffect, useState } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    StatusBar,
    ActivityIndicator,
} from 'react-native';
import Slider from '@react-native-community/slider';
import { Ionicons, Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { router, useLocalSearchParams } from 'expo-router';
import { NavBar } from '@/components/navbar';
import {
    useInteractOutApi,
    AppProfile,
    InterventionConfig,
    InterventionType,
} from '@/hooks/useInteractOutApi';

// ─── Display metadata for each intervention type ─────────────────────────────
const INTERVENTION_META: Record<InterventionType, {
    icon: string;
    iconColor: string;
    title: string;
    description: string;
    settingLabel: string;
    showPlusMinus?: boolean;
}> = {
    TAP_DELAY: {
        icon: 'hand-left-outline', iconColor: '#2D4A2D',
        title: 'Tap Delay',
        description: 'Adds a slight hesitation before processing a tap.',
        settingLabel: 'DELAY',
    },
    TAP_PROLONG: {
        icon: 'finger-print-outline', iconColor: '#5A5A5A',
        title: 'Tap Prolong',
        description: 'Requires holding for a set duration before registering.',
        settingLabel: 'HOLD DURATION',
        showPlusMinus: true,
    },
    TAP_SHIFT: {
        icon: 'navigate-outline', iconColor: '#2D4A2D',
        title: 'Tap Shift',
        description: 'Slightly offsets the registered tap location.',
        settingLabel: 'OFFSET VARIANCE',
    },
    TAP_DOUBLE: {
        icon: 'hand-right-outline', iconColor: '#3D5C3D',
        title: 'Tap Double',
        description: 'Requires double-tap to trigger a single tap effect.',
        settingLabel: '',
    },
    SWIPE_DELAY: {
        icon: 'timer-outline', iconColor: '#C0392B',
        title: 'Swipe Delay',
        description: 'Introduces a pause between consecutive swipes.',
        settingLabel: 'INTER-SCROLL WAIT',
    },
    SWIPE_DECELERATE: {
        icon: 'trending-down-outline', iconColor: '#5A5A5A',
        title: 'Swipe Deceleration',
        description: 'Increases friction when scrolling rapidly.',
        settingLabel: 'FRICTION LEVEL',
    },
    SWIPE_REVERSE: {
        icon: 'swap-vertical-outline', iconColor: '#8B4513',
        title: 'Swipe Reverse',
        description: 'Reverses your swipe direction.',
        settingLabel: '',
    },
    SWIPE_MULTI_FINGER: {
        icon: 'hand-right-outline', iconColor: '#3D5C3D',
        title: 'Multi-Finger Swipe',
        description: 'Requires two fingers to scroll through feeds.',
        settingLabel: '',
    },
};

function formatIntensityLabel(type: InterventionType, intensity: number): string {
    switch (type) {
        case 'TAP_DELAY':       return `${Math.round(intensity * 800)}ms`;
        case 'TAP_PROLONG':     return `${(intensity * 1.5).toFixed(1)}s`;
        case 'TAP_SHIFT':       return `${Math.round(intensity * 60)}px`;
        case 'SWIPE_DELAY':     return `${Math.round(intensity * 600)}ms`;
        case 'SWIPE_DECELERATE':return `${(1 + intensity * 3).toFixed(1)}x`;
        default:                return '';
    }
}

// ─── Component ────────────────────────────────────────────────────────────────

export default function InterventionSetupScreen() {
    const { packageName } = useLocalSearchParams<{ packageName: string }>();
    const { profiles, saveProfile, updateIntensity, loading } = useInteractOutApi();

    // Local copy for optimistic UI updates
    const [localProfile, setLocalProfile] = useState<AppProfile | null>(null);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        const found = profiles.find(p => p.packageName === packageName);
        if (found) setLocalProfile(JSON.parse(JSON.stringify(found))); // deep clone
    }, [profiles, packageName]);

    if (!localProfile) {
        return (
            <View className="flex-1 bg-[#F2F0EA] items-center justify-center">
                <ActivityIndicator color="#2D4A2D" size="large" />
            </View>
        );
    }

    const toggleIntervention = (type: InterventionType) => {
        setLocalProfile(prev => {
            if (!prev) return prev;
            return {
                ...prev,
                interventions: prev.interventions.map(i =>
                    i.type === type ? { ...i, enabled: !i.enabled } : i
                ),
            };
        });
    };

    const setSliderValue = (type: InterventionType, value: number) => {
        setLocalProfile(prev => {
            if (!prev) return prev;
            return {
                ...prev,
                interventions: prev.interventions.map(i =>
                    i.type === type ? { ...i, intensity: value } : i
                ),
            };
        });
    };

    // Debounced slider release → push to backend
    const onSliderComplete = async (type: InterventionType, value: number) => {
        await updateIntensity(localProfile.packageName, type, value);
    };

    const handleSave = async () => {
        setSaving(true);
        await saveProfile(localProfile);
        setSaving(false);
        router.back();
    };

    const activeCount = localProfile.interventions.filter(i => i.enabled).length;

    // Friction coefficient — simple weighted sum matching paper's heuristic
    const frictionCoeff = localProfile.interventions
            .filter(i => i.enabled)
            .reduce((sum, i) => sum + i.intensity, 0) /
        Math.max(localProfile.interventions.length, 1);

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
                    <TouchableOpacity onPress={() => router.back()}>
                        <Ionicons name="arrow-back-outline" size={22} color="#2D4A2D" />
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
                            <Text className="text-[#8A8A8A] text-xs tracking-widest">
                                ACTIVE APPLICATION
                            </Text>
                            <Text className="text-[#1C1C1C] text-2xl font-bold">
                                {localProfile.appName}
                            </Text>
                        </View>
                    </View>
                    <Text className="text-[#8A8A8A] text-xs tracking-wide">Global Active</Text>
                </View>

                {/* Intervention Cards */}
                <View className="gap-3">
                    {localProfile.interventions.map((item: InterventionConfig) => {
                        const meta = INTERVENTION_META[item.type];
                        if (!meta) return null;
                        const hasSlider = meta.settingLabel !== '';
                        const label = formatIntensityLabel(item.type, item.intensity);

                        return (
                            <View
                                key={item.type}
                                className={`rounded-2xl p-4 ${
                                    item.type === 'SWIPE_MULTI_FINGER' ? 'bg-[#E8EDE8]' : 'bg-white'
                                }`}
                            >
                                {/* Row: Icon + Title + Toggle */}
                                <View className="flex-row items-start justify-between mb-1">
                                    <View className="flex-row items-center gap-3 flex-1">
                                        <View className="w-8 h-8 items-center justify-center">
                                            <Ionicons
                                                name={meta.icon as any}
                                                size={22}
                                                color={meta.iconColor}
                                            />
                                        </View>
                                        <View className="flex-1">
                                            <Text className="text-[#1C1C1C] text-base font-semibold">
                                                {meta.title}
                                            </Text>
                                            <Text className="text-[#8A8A8A] text-xs mt-0.5 leading-relaxed">
                                                {meta.description}
                                            </Text>
                                        </View>
                                    </View>

                                    {/* Toggle */}
                                    <TouchableOpacity
                                        className={`w-12 h-7 rounded-full ml-2 mt-0.5 items-center justify-center ${
                                            item.enabled ? 'bg-[#2D4A2D]' : 'bg-[#D0D0D0]'
                                        }`}
                                        onPress={() => toggleIntervention(item.type)}
                                        activeOpacity={0.8}
                                    >
                                        <View
                                            className={`w-5 h-5 bg-white rounded-full shadow ${
                                                item.enabled ? 'translate-x-2.5' : '-translate-x-2.5'
                                            }`}
                                        />
                                    </TouchableOpacity>
                                </View>

                                {/* Slider */}
                                {hasSlider && (
                                    <View className="mt-3 pl-11">
                                        <View className="flex-row items-center justify-between mb-1">
                                            <Text className="text-[#9A9A9A] text-xs tracking-widest">
                                                {meta.settingLabel}
                                            </Text>
                                            <Text className="text-[#9A9A9A] text-xs font-medium">{label}</Text>
                                        </View>

                                        {meta.showPlusMinus ? (
                                            <View className="flex-row items-center gap-2">
                                                <TouchableOpacity
                                                    className="w-6 h-6 border border-[#C0C0C0] rounded-full items-center justify-center"
                                                    onPress={() => {
                                                        const v = Math.max(0, item.intensity - 0.1);
                                                        setSliderValue(item.type, v);
                                                        onSliderComplete(item.type, v);
                                                    }}
                                                >
                                                    <Ionicons name="remove" size={14} color="#555" />
                                                </TouchableOpacity>
                                                <View className="flex-1">
                                                    <Slider
                                                        value={item.intensity}
                                                        minimumValue={0}
                                                        maximumValue={1}
                                                        minimumTrackTintColor="#2D4A2D"
                                                        maximumTrackTintColor="#D0D0D0"
                                                        thumbTintColor="#2D4A2D"
                                                        style={{ height: 24 }}
                                                        onValueChange={v => setSliderValue(item.type, v)}
                                                        onSlidingComplete={v => onSliderComplete(item.type, v)}
                                                    />
                                                </View>
                                                <TouchableOpacity
                                                    className="w-6 h-6 border border-[#C0C0C0] rounded-full items-center justify-center"
                                                    onPress={() => {
                                                        const v = Math.min(1, item.intensity + 0.1);
                                                        setSliderValue(item.type, v);
                                                        onSliderComplete(item.type, v);
                                                    }}
                                                >
                                                    <Ionicons name="add" size={14} color="#555" />
                                                </TouchableOpacity>
                                            </View>
                                        ) : (
                                            <Slider
                                                value={item.intensity}
                                                minimumValue={0}
                                                maximumValue={1}
                                                minimumTrackTintColor="#2D4A2D"
                                                maximumTrackTintColor="#D0D0D0"
                                                thumbTintColor="#2D4A2D"
                                                style={{ height: 24 }}
                                                onValueChange={v => setSliderValue(item.type, v)}
                                                onSlidingComplete={v => onSliderComplete(item.type, v)}
                                            />
                                        )}
                                    </View>
                                )}
                            </View>
                        );
                    })}
                </View>

                {/* Intention Summary */}
                <View className="bg-white rounded-3xl p-5 mt-5 mb-8">
                    <Text className="text-[#1C1C1C] text-xl font-bold mb-4">
                        Intention Summary
                    </Text>
                    <View className="flex-row justify-between mb-2">
                        <Text className="text-[#6B6B6B] text-sm">Active Interventions</Text>
                        <Text className="text-[#1C1C1C] text-sm font-semibold">
                            {activeCount} Active
                        </Text>
                    </View>
                    <View className="flex-row justify-between mb-2">
                        <Text className="text-[#6B6B6B] text-sm">Friction Coefficient</Text>
                        <Text className="text-[#2D7A2D] text-sm font-semibold">
                            {frictionCoeff > 0.6 ? 'High' : frictionCoeff > 0.3 ? 'Medium' : 'Low'}{' '}
                            ({frictionCoeff.toFixed(2)})
                        </Text>
                    </View>
                    {localProfile.dailyLimitMs > 0 && (
                        <View className="flex-row justify-between mb-2">
                            <Text className="text-[#6B6B6B] text-sm">Daily Limit</Text>
                            <Text className="text-[#1C1C1C] text-sm font-semibold">
                                {Math.round(localProfile.dailyLimitMs / 3_600_000)}h
                            </Text>
                        </View>
                    )}
                    <View className="h-px bg-[#F0EDE7] my-3" />
                    <TouchableOpacity
                        className="bg-[#2D4A2D] rounded-full py-4 items-center"
                        onPress={handleSave}
                        disabled={saving}
                    >
                        {saving ? (
                            <ActivityIndicator color="white" />
                        ) : (
                            <Text className="text-white text-base font-semibold">
                                Update Application Profile
                            </Text>
                        )}
                    </TouchableOpacity>
                </View>

                <View className="h-20" />
            </ScrollView>

            <NavBar />
        </View>
    );
}
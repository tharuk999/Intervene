import React, { useState, useEffect, useCallback } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    TextInput,
    StatusBar,
    ActivityIndicator,
    NativeModules,
} from 'react-native';
import { router } from 'expo-router';
import { Ionicons, Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { NavBar } from '@/components/navbar';

const { ProfilesModule } = NativeModules;

// ── Types ──────────────────────────────────────────────────────────────────────

interface AppEntry {
    packageName: string;
    appName: string;
    usedMs: number;
    hasProfile: boolean;
}

// ── Helpers ────────────────────────────────────────────────────────────────────

function appMeta(packageName: string): { bgColor: string; iconName: string } {
    if (packageName.includes('instagram')) return { bgColor: '#E8431A', iconName: 'camera' };
    if (packageName.includes('tiktok'))    return { bgColor: '#1C1C1C', iconName: 'musical-notes' };
    if (packageName.includes('twitter') || packageName.includes('x.com'))
        return { bgColor: '#1DA1F2', iconName: 'close' };
    if (packageName.includes('reddit'))    return { bgColor: '#FF4500', iconName: 'chatbubbles' };
    if (packageName.includes('youtube'))   return { bgColor: '#FF0000', iconName: 'play' };
    return { bgColor: '#6B6B6B', iconName: 'apps' };
}

function formatMs(ms: number): string {
    const h = Math.floor(ms / 3_600_000);
    const m = Math.floor((ms % 3_600_000) / 60_000);
    if (h === 0 && m === 0) return '';
    if (h === 0) return `${m}m`;
    if (m === 0) return `${h}h`;
    return `${h}h ${m}m`;
}

// ── Screen ─────────────────────────────────────────────────────────────────────

export default function InterventionsScreen() {
    const [search, setSearch]   = useState('');
    const [apps, setApps]       = useState<AppEntry[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState<string | null>(null);

    const loadApps = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const result: AppEntry[] = await ProfilesModule.getAllApps();
            setApps(result);
        } catch (e: any) {
            setError(e?.message ?? 'Failed to load apps');
            console.warn('getAllApps error:', e);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { loadApps(); }, [loadApps]);

    // Used apps = have usedMs > 0, unused = rest
    const usedApps   = apps.filter(a => a.usedMs > 0);
    const unusedApps = apps.filter(a => a.usedMs === 0);

    const filterFn = (a: AppEntry) =>
        a.appName.toLowerCase().includes(search.toLowerCase()) ||
        a.packageName.toLowerCase().includes(search.toLowerCase());

    const filteredUsed   = usedApps.filter(filterFn);
    const filteredUnused = unusedApps.filter(filterFn);

    const renderApp = (app: AppEntry) => {
        const { bgColor, iconName } = appMeta(app.packageName);
        const usage = formatMs(app.usedMs);
        return (
            <TouchableOpacity
                key={app.packageName}
                className={`rounded-2xl px-4 py-4 flex-row items-center mb-3 ${
                    app.hasProfile ? 'bg-white' : 'bg-[#F0EDE7]'
                }`}
                onPress={() => router.push({
                    pathname: '/intervention-setup',
                    params: { packageName: app.packageName },
                })}
            >
                <View
                    className="w-12 h-12 rounded-xl items-center justify-center mr-4"
                    style={{ backgroundColor: bgColor }}
                >
                    <Ionicons name={iconName as any} size={22} color="white" />
                </View>
                <View className="flex-1">
                    <Text className={`text-base font-semibold ${
                        app.hasProfile ? 'text-[#1C1C1C]' : 'text-[#9A9A9A]'
                    }`}>
                        {app.appName}
                    </Text>
                    <View className="flex-row items-center gap-2 mt-0.5">
                        {app.hasProfile && (
                            <View className="flex-row items-center gap-1">
                                <View className="w-1.5 h-1.5 rounded-full bg-[#2D4A2D]" />
                                <Text className="text-[#6B6B6B] text-xs">Friction active</Text>
                            </View>
                        )}
                        {usage !== '' && (
                            <Text className="text-[#B0B0B0] text-xs">{usage} today</Text>
                        )}
                        {!app.hasProfile && usage === '' && (
                            <Text className="text-[#B0B0B0] text-xs">No friction applied</Text>
                        )}
                    </View>
                </View>
                <Ionicons name="chevron-forward" size={18} color="#C0C0C0" />
            </TouchableOpacity>
        );
    };

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
                <TouchableOpacity onPress={loadApps}>
                    <Feather name="refresh-cw" size={20} color="#2D4A2D" />
                </TouchableOpacity>
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

                {/* Search */}
                <View className="bg-[#E8E5DF] rounded-2xl flex-row items-center px-4 py-3 mt-5 mb-6">
                    <Feather name="sliders" size={18} color="#9A9A9A" />
                    <TextInput
                        value={search}
                        onChangeText={setSearch}
                        placeholder="Filter your applications..."
                        placeholderTextColor="#9A9A9A"
                        className="flex-1 ml-3 text-[#1C1C1C] text-sm"
                    />
                    {search.length > 0 && (
                        <TouchableOpacity onPress={() => setSearch('')}>
                            <Ionicons name="close-circle" size={18} color="#9A9A9A" />
                        </TouchableOpacity>
                    )}
                </View>

                {loading && <ActivityIndicator color="#2D4A2D" style={{ marginTop: 32 }} />}
                {error && <Text className="text-[#C0392B] text-sm text-center mt-8">{error}</Text>}

                {/* Used apps section */}
                {!loading && filteredUsed.length > 0 && (
                    <>
                        <Text className="text-[#9A9A9A] text-xs tracking-widest mb-3">
                            RECENTLY USED
                        </Text>
                        {filteredUsed.map(renderApp)}
                    </>
                )}

                {/* All installed apps section */}
                {!loading && filteredUnused.length > 0 && (
                    <>
                        <Text className="text-[#9A9A9A] text-xs tracking-widest mb-3 mt-2">
                            ALL APPS
                        </Text>
                        {filteredUnused.map(renderApp)}
                    </>
                )}

                {/* No results */}
                {!loading && filteredUsed.length === 0 && filteredUnused.length === 0 && !error && (
                    <Text className="text-[#9A9A9A] text-sm text-center mt-12">
                        No apps match "{search}"
                    </Text>
                )}

                <View className="h-32" />
            </ScrollView>

            <NavBar />
        </View>
    );
}
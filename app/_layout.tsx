// app/_layout.tsx
import { Stack } from 'expo-router';
import * as TaskManager from 'expo-task-manager';
import * as BackgroundTask from 'expo-background-task';
import AsyncStorage from '@react-native-async-storage/async-storage';
import './global.css';

// ── DEFINE TASK HERE (global scope, runs before React) ──────────────────────
TaskManager.defineTask('SYNC_USAGE', async () => {
    try {
        const sessions = await AsyncStorage.getItem('pending_sessions');
        if (sessions) {
            // Call your backend
            const response = await fetch('http://10.0.2.2:8080/api/usage/session', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: sessions,
            });
            if (response.ok) {
                await AsyncStorage.removeItem('pending_sessions');
            }
        }
        return BackgroundTask.BackgroundTaskResult.Success;
    } catch (error) {
        console.error('Background sync failed:', error);
        return BackgroundTask.BackgroundTaskResult.Failed;
    }
});

export default function RootLayout() {
    return <Stack screenOptions={{ headerShown: false }} />;
}
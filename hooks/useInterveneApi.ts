/**
 * useInterveneApi.ts
 * Connects Expo frontend → Spring Boot backend.
 * Mirrors all REST endpoints from the README spec.
 */

import { useState, useEffect, useCallback } from 'react';

// Android Emulator → host machine: 10.0.2.2
// Real device: replace with your PC's LAN IP (ipconfig → IPv4)
const BASE_URL = 'http://10.0.2.2:8080/api';

// ─── Types ────────────────────────────────────────────────────────────────────

export type InterventionType =
  | 'TAP_DELAY'
  | 'TAP_PROLONG'
  | 'TAP_SHIFT'
  | 'TAP_DOUBLE'
  | 'SWIPE_DELAY'
  | 'SWIPE_DECELERATE'
  | 'SWIPE_REVERSE'
  | 'SWIPE_MULTI_FINGER';

export interface InterventionConfig {
  type: InterventionType;
  enabled: boolean;
  /** 0.0–1.0 maps to slider value */
  intensity: number;
}

export interface AppProfile {
  packageName: string;
  appName: string;
  enabled: boolean;
  dailyLimitMs: number;
  interventions: InterventionConfig[];
}

export interface TopApp {
  packageName: string;
  appName: string;
  usedMs: number;
}

export interface DashboardData {
  totalScreenTimeMs: number;
  resistedUrges: number;
  activeFrictionApps: number;
  percentChangeVsYesterday: number;
  dailyBreakdown: Record<string, number>;
  topApps: TopApp[];
}

export interface UsageStatus {
  usedMs: number;
  limitMs: number;
  bypassActive: boolean;
}

// ─── Hook ─────────────────────────────────────────────────────────────────────

export function useInterveneApi() {
  const [profiles, setProfiles] = useState<AppProfile[]>([]);
  const [dashboard, setDashboard] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ── Dashboard ──────────────────────────────────────────────────────────────
  const fetchDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${BASE_URL}/usage/dashboard`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setDashboard(await res.json());
    } catch (e: any) {
      setError(e.message ?? 'Cannot reach backend');
    } finally {
      setLoading(false);
    }
  }, []);

  const refresh = fetchDashboard;

  // ── Profiles ───────────────────────────────────────────────────────────────
  const fetchProfiles = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(`${BASE_URL}/profiles`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setProfiles(await res.json());
    } catch (e: any) {
      setError(e.message ?? 'Cannot load profiles');
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchActiveProfiles = useCallback(async (): Promise<AppProfile[]> => {
    try {
      const res = await fetch(`${BASE_URL}/profiles/active`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return res.json();
    } catch {
      return [];
    }
  }, []);

  const saveProfile = useCallback(async (profile: AppProfile): Promise<AppProfile | null> => {
    try {
      const res = await fetch(`${BASE_URL}/profiles`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(profile),
      });
      const saved: AppProfile = await res.json();
      setProfiles(prev =>
        prev.some(p => p.packageName === saved.packageName)
          ? prev.map(p => (p.packageName === saved.packageName ? saved : p))
          : [...prev, saved]
      );
      return saved;
    } catch (e: any) {
      setError(e.message);
      return null;
    }
  }, []);

  const toggleAppEnabled = useCallback(async (packageName: string, enabled: boolean) => {
    try {
      const res = await fetch(`${BASE_URL}/profiles/${packageName}/enabled`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ enabled }),
      });
      const updated: AppProfile = await res.json();
      setProfiles(prev => prev.map(p => (p.packageName === packageName ? updated : p)));
    } catch (e: any) {
      setError(e.message);
    }
  }, []);

  const updateIntensity = useCallback(
    async (packageName: string, type: InterventionType, intensity: number) => {
      try {
        const res = await fetch(
          `${BASE_URL}/profiles/${packageName}/interventions/${type}/intensity`,
          {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ intensity }),
          }
        );
        const updated: AppProfile = await res.json();
        setProfiles(prev => prev.map(p => (p.packageName === packageName ? updated : p)));
      } catch (e: any) {
        setError(e.message);
      }
    },
    []
  );

  const deleteProfile = useCallback(async (packageName: string) => {
    try {
      await fetch(`${BASE_URL}/profiles/${packageName}`, { method: 'DELETE' });
      setProfiles(prev => prev.filter(p => p.packageName !== packageName));
    } catch (e: any) {
      setError(e.message);
    }
  }, []);

  // ── Usage ──────────────────────────────────────────────────────────────────
  const getUsageToday = useCallback(async (packageName: string): Promise<UsageStatus | null> => {
    try {
      const res = await fetch(`${BASE_URL}/usage/today/${packageName}`);
      return res.json();
    } catch {
      return null;
    }
  }, []);

  const recordSession = useCallback(
    async (packageName: string, durationMs: number, timestampMs: number) => {
      try {
        await fetch(`${BASE_URL}/usage/session`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ packageName, durationMs, timestampMs }),
        });
      } catch {}
    },
    []
  );

  const recordResistedUrge = useCallback(async (packageName: string) => {
    try {
      await fetch(`${BASE_URL}/usage/resisted`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ packageName }),
      });
    } catch {}
  }, []);

  // ── Bypass ─────────────────────────────────────────────────────────────────
  const grantBypass = useCallback(async (packageName: string, durationMs: number) => {
    try {
      const res = await fetch(`${BASE_URL}/usage/bypass`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ packageName, durationMs }),
      });
      return res.json();
    } catch {
      return null;
    }
  }, []);

  const checkBypass = useCallback(async (packageName: string): Promise<boolean> => {
    try {
      const res = await fetch(`${BASE_URL}/usage/bypass/${packageName}`);
      const data = await res.json();
      return data.active ?? false;
    } catch {
      return false;
    }
  }, []);

  const getWeeklyHistory = useCallback(async () => {
    try {
      const res = await fetch(`${BASE_URL}/usage/history/weekly`);
      return res.json();
    } catch {
      return [];
    }
  }, []);

  // ── Init ───────────────────────────────────────────────────────────────────
  useEffect(() => {
    fetchDashboard();
    fetchProfiles();
  }, []);

  return {
    profiles,
    dashboard,
    loading,
    error,
    refresh,
    fetchDashboard,
    fetchProfiles,
    fetchActiveProfiles,
    saveProfile,
    toggleAppEnabled,
    updateIntensity,
    deleteProfile,
    getUsageToday,
    recordSession,
    recordResistedUrge,
    grantBypass,
    checkBypass,
    getWeeklyHistory,
  };
}

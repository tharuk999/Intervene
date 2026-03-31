import { Ionicons, Feather } from '@expo/vector-icons';
import { router, usePathname, Href } from 'expo-router';
import { View, TouchableOpacity, Text } from 'react-native';

export function NavBar() {
  const pathname = usePathname();

  const navItems: {
    label: string;
    route: Href;
    icon: (color: string) => React.ReactNode;
  }[] = [
    {
      label: 'HOME',
      route: '/',
      icon: (color) => <Ionicons name="home" size={20} color={color} />,
    },
    {
      label: 'INTERVENTIONS',
      route: '/interventions',
      icon: (color) => <Feather name="activity" size={22} color={color} />,
    },
    {
      label: 'SETTINGS',
      route: '/settings',
      icon: (color) => <Ionicons name="settings-outline" size={22} color={color} />,
    },
  ];

  return (
    <View className="absolute bottom-0 left-0 right-0 bg-[#F2F0EA] border-t border-[#E0DDD7] flex-row items-center justify-around px-6 py-3 pb-6">
      {navItems.map((item) => {
        const isActive = pathname === item.route;

        return (
          <TouchableOpacity
            key={item.route}
            className="items-center"
            onPress={() => router.push(item.route)}
            activeOpacity={0.8}
          >
            <View
              className={`rounded-full px-4 py-2 ${
                isActive ? 'bg-[#2D4A2D]' : ''
              }`}
            >
              {item.icon(isActive ? 'white' : '#9A9A9A')}
            </View>

            <Text
              className={`text-xs mt-1 tracking-widest ${
                isActive
                  ? 'text-[#2D4A2D] font-semibold'
                  : 'text-[#9A9A9A]'
              }`}
            >
              {item.label}
            </Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
}
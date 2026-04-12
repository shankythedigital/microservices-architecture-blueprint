import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.example.app',
  appName: 'keeply_react_app',
  webDir: 'dist',
  // WebView is https://localhost; HTTP API bases need this on Android or fetch is blocked.
  android: {
    allowMixedContent: true,
  },
};

export default config;

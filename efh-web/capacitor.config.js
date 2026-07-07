/** @type {import('@capacitor/cli').CapacitorConfig} */
const config = {
  appId: 'com.efh.mobile',
  appName: '叉车智联中台',
  webDir: 'dist',
  bundledWebRuntime: false,
  server: {
    androidScheme: 'http',
    cleartext: true
  },
  plugins: {
    StatusBar: {
      style: 'LIGHT',
      backgroundColor: '#ffffff'
    },
    Keyboard: {
      resize: 'body'
    }
  }
}

module.exports = config

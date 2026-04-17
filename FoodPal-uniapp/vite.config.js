import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';

export default defineConfig({
  plugins: [uni()],
  server: {
    host: '0.0.0.0',
    port: 19943,
    proxy: {
      '/api': {
        target: 'http://localhost:19941',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:19941',
        ws: true,
        changeOrigin: true,
      }
    }
  },
  preview: {
    host: '0.0.0.0',
    port: 19943
  },
  define: {
    'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'production')
  }
});

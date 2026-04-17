import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  define: {
    global: 'window',
    process: 'window.process',
    Buffer: 'window.Buffer',
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return;
          if (id.includes('ant-design-vue')) return 'antd';
          if (id.includes('@ant-design/icons-vue')) return 'antd-icons';
          if (id.includes('vue-router') || id.includes('/vue/') || id.includes('pinia')) return 'vue-vendor';
          if (id.includes('axios') || id.includes('dayjs')) return 'utils';
          return 'vendor';
        },
      },
    },
  },
  server: {
    port: 19942,
    proxy: {
      '/api': {
        target: 'http://localhost:19941',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
      '/ws': {
        target: 'ws://localhost:19941',
        ws: true,
        changeOrigin: true,
      },
    },
  },
});

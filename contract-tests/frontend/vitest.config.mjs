import { fileURLToPath } from 'node:url';

const frontendRoot = fileURLToPath(new URL('../../taska-frontend', import.meta.url));

export default {
  root: frontendRoot,
  test: {
    environment: 'jsdom',
    fileParallelism: false,
    include: ['../contract-tests/frontend/**/*.spec.ts'],
    setupFiles: ['../contract-tests/frontend/vitest.setup.ts'],
  },
};

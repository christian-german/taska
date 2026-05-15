const { execSync } = require('child_process');
const { writeFileSync } = require('fs');
const path = require('path');

let version;
try {
  version = execSync('git describe --tags --abbrev=0', { encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'] }).trim();
} catch {
  const pkg = require('../package.json');
  version = pkg.version;
  console.warn(`[set-version] No git tag found, falling back to package.json version: ${version}`);
}

const dest = path.join(__dirname, '../src/app/core/constants/app-version.ts');
writeFileSync(dest, `// Auto-generated at build time — do not edit manually.\nexport const APP_VERSION = '${version}';\n`);
console.log(`[set-version] APP_VERSION = ${version}`);

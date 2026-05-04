// Preload runs in the renderer with access to Node APIs before the web page loads.
// Keep it minimal — expose only what the renderer genuinely needs.
const { contextBridge } = require('electron');

contextBridge.exposeInMainWorld('electron', {
  isElectron: true,
});

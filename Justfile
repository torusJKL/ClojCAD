install:
    npm install
    node scripts/patch-opencascade.js
    cp node_modules/opencascade.js/dist/cascadestudio.wasm public/
    cp node_modules/three-cad-viewer/dist/three-cad-viewer.css public/

dev port='8777':
    NREPL_PORT={{port}} node scripts/start-dev.js

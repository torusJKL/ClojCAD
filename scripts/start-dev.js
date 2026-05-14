const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

const port = parseInt(process.env.NREPL_PORT, 10);
const configPath = path.join(__dirname, '..', 'shadow-cljs.edn');

function start(restoreContent) {
  const child = spawn('npx', ['shadow-cljs', 'watch', 'dev'], {
    stdio: 'inherit',
    env: { ...process.env },
  });

  process.on('SIGINT', () => child.kill('SIGINT'));
  process.on('SIGTERM', () => child.kill('SIGTERM'));

  child.on('exit', (code) => {
    if (restoreContent) fs.writeFileSync(configPath, restoreContent);
    process.exit(code);
  });
}

if (port) {
  const original = fs.readFileSync(configPath, 'utf8');
  const patched = original
    .replace(/:nrepl-port\s*\d+/g, `:nrepl-port ${port}`)
    .replace(/:nrepl\s*\{:port[^}]*\}/g, `:nrepl {:port ${port}}`);
  fs.writeFileSync(configPath, patched);
  start(original);
} else {
  start(null);
}

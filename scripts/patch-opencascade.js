const fs = require("fs");
const path = require("path");

const distPath = path.join(__dirname, "..", "node_modules", "opencascade.js", "dist", "cascadestudio.js");

if (!fs.existsSync(distPath)) {
  console.log("cascadestudio.js not found, skipping patch");
  process.exit(0);
}

let code = fs.readFileSync(distPath, "utf8");

// Replace import.meta.url in the Emscripten boilerplate.
// In the browser, this is only used to compute scriptDirectory,
// which we override via locateFile. A static string is safe.
code = code.replace(/import\.meta\.url/g, '"."');

fs.writeFileSync(distPath, code);
console.log("Patched cascadestudio.js: replaced import.meta.url");

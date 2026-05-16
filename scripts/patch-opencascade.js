const fs = require("fs");
const path = require("path");

function removeTypeModule(pkgDir, label) {
  const pkgPath = path.join(pkgDir, "package.json");
  if (!fs.existsSync(pkgPath)) return;
  const pkg = JSON.parse(fs.readFileSync(pkgPath, "utf8"));
  if (pkg.type === "module") {
    delete pkg.type;
    fs.writeFileSync(pkgPath, JSON.stringify(pkg, null, 2) + "\n");
    console.log(`Patched ${label} package.json: removed type:module`);
  }
}

// --- opencascade.js ---
const ocDist = path.join(__dirname, "..", "node_modules", "opencascade.js", "dist", "cascadestudio.js");

if (fs.existsSync(ocDist)) {
  let code = fs.readFileSync(ocDist, "utf8");

  // Replace import.meta.url in the Emscripten boilerplate.
  code = code.replace(/import\.meta\.url/g, '"."');
  // Convert ESM default export to CommonJS so Node can require() it.
  code = code.replace(/export default Module;/, "module.exports = Module;");

  fs.writeFileSync(ocDist, code);
  console.log("Patched cascadestudio.js: replaced import.meta.url, converted to CJS");

  removeTypeModule(
    path.join(__dirname, "..", "node_modules", "opencascade.js"),
    "opencascade.js"
  );
} else {
  console.log("cascadestudio.js not found, skipping opencascade.js patch");
}

// --- three-cad-viewer ---
// Its dist file is already UMD/CommonJS, but "type":"module" in package.json
// prevents Node from providing the CJS globals.
removeTypeModule(
  path.join(__dirname, "..", "node_modules", "three-cad-viewer"),
  "three-cad-viewer"
);

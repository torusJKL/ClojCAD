const fs = require("fs");
const path = require("path");

const pkgPath = path.join(__dirname, "..", "node_modules", "three", "package.json");

if (!fs.existsSync(pkgPath)) {
  console.log("three/package.json not found, skipping patch");
  process.exit(0);
}

const pkg = JSON.parse(fs.readFileSync(pkgPath, "utf8"));

if (pkg.exports) {
  delete pkg.exports;
  fs.writeFileSync(pkgPath, JSON.stringify(pkg, null, 2));
  console.log("Patched three/package.json: removed exports map");
} else {
  console.log("three/package.json already patched (no exports map)");
}

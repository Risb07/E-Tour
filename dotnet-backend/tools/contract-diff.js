/**
 * Diff every Java DTO/entity's serialized field names against the .NET equivalent.
 *
 * Both backends serve the same React client, so any name present on one side and absent on
 * the other is a field the client silently reads as undefined, or silently fails to send.
 * Nothing else catches this: the endpoint still returns 200 with a well-formed body, and the
 * compiler and the behavioural tests are equally happy. Compares camelCase JSON names -
 * what actually goes on the wire.
 *
 *   node tools/contract-diff.js
 *
 * Reading the output:
 *   MISSING in .NET  - the serious direction. Java (and therefore the client) has this name
 *                      and we do not. Check whether the client actually reads it before
 *                      renaming anything: several Java fields are vestigial and read by
 *                      nobody (see the known-benign list below).
 *   only in .NET     - usually harmless. Mostly FK id properties and navigation collections
 *                      on Domain entities, which this port never serializes (it always
 *                      returns DTOs, whereas the Java controllers return entities directly).
 *
 * Known-benign MISSING entries, all verified against the Java sources and the client:
 *   CartRequest.numberOfPassengers - Java's CartServiceImpl only ever reads adultCount/childCount.
 *   PassengerInput.passengerType   - never read when pricing; the band is derived from the DOB.
 *   Category.activeTourCount       - a JPA @Transient on the entity; we expose it on CategoryResponse.
 *   StayMeal.location              - entity navigation; the DTO carries locationName instead.
 *
 * Anything else showing up under MISSING is a real bug - fix it and pin the name in
 * tests/eTour.Tests/Services/WireContractTests.cs.
 */
const fs = require("fs");
const path = require("path");

// tools/ -> dotnet-backend/ -> repository root holding Backend/ and dotnet-backend/.
const ROOT = path.resolve(__dirname, "..", "..");
const JAVA = path.join(ROOT, "Backend", "src", "main", "java", "com", "etour");
const NET_DIRS = [
  path.join(ROOT, "dotnet-backend", "src", "eTour.Application", "Dtos"),
  path.join(ROOT, "dotnet-backend", "src", "eTour.Domain", "Entities"),
];

const camel = (n) => (n ? n[0].toLowerCase() + n.slice(1) : n);
const stripComments = (s) => s.replace(/\/\*[\s\S]*?\*\//g, "").replace(/\/\/.*/g, "");

function walk(dir, ext, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, ext, out);
    else if (e.name.endsWith(ext)) out.push(p);
  }
  return out;
}

function parseJava(file) {
  const src = stripComments(fs.readFileSync(file, "utf8"));
  const out = {};
  let cur = null;
  for (const line of src.split("\n")) {
    const c = line.match(/\b(?:public\s+|abstract\s+)*class\s+(\w+)/);
    if (c) { cur = c[1]; out[cur] ??= []; }
    // private [final] <Type> name; or  ... name = ...
    const f = line.match(/^\s*private\s+(?:final\s+|static\s+|transient\s+)*[\w<>,\[\]\s.]+?\s+(\w+)\s*(?:=|;)/);
    if (f && cur && f[1] !== "serialVersionUID") out[cur].push(f[1]);
  }
  return out;
}

function parseNet(file) {
  const src = stripComments(fs.readFileSync(file, "utf8"));
  const out = {};
  let cur = null;
  for (const line of src.split("\n")) {
    const c = line.match(/\b(?:public|internal)\s+(?:sealed\s+|abstract\s+|partial\s+)*class\s+(\w+)/);
    if (c) { cur = c[1]; out[cur] ??= []; }
    const f = line.match(/^\s*public\s+[\w<>,\[\]?\s.]+?\s+(\w+)\s*\{\s*get;/);
    if (f && cur) out[cur].push(camel(f[1]));
  }
  return out;
}

const javaClasses = {};
for (const f of walk(JAVA, ".java"))
  for (const [k, v] of Object.entries(parseJava(f))) if (v.length && !javaClasses[k]) javaClasses[k] = v;

const netClasses = {};
for (const d of NET_DIRS)
  for (const f of walk(d, ".cs"))
    for (const [k, v] of Object.entries(parseNet(f))) if (v.length && !netClasses[k]) netClasses[k] = v;

const shared = Object.keys(javaClasses).filter((k) => netClasses[k]);
const problems = [];
for (const name of shared) {
  const jf = javaClasses[name], nf = netClasses[name];
  const js = new Set(jf), ns = new Set(nf);
  const missing = jf.filter((f) => !ns.has(f));
  const extra = nf.filter((f) => !js.has(f));
  if (missing.length || extra.length) problems.push({ name, missing, extra });
}

console.log(`Compared ${shared.length} shared type names; ${problems.length} differ\n`);
for (const p of problems.sort((a, b) => b.missing.length - a.missing.length)) {
  console.log(`--- ${p.name}`);
  if (p.missing.length) console.log(`    MISSING in .NET : ${p.missing.join(", ")}`);
  if (p.extra.length)   console.log(`    only in .NET    : ${p.extra.join(", ")}`);
}
const orphans = Object.keys(javaClasses).filter((k) => !netClasses[k]);
console.log(`\n[Java types with no same-named .NET type] ${orphans.join(", ")}`);

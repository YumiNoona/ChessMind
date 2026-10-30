import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const sharp = require("sharp");

const root = path.resolve(import.meta.dirname, "..");
const sourceRoot = path.join(root, "third_party", "chess_pieces");
const outputRoot = path.join(root, "app", "src", "main", "res", "drawable-nodpi");

const sets = ["cburnett", "california", "merida", "staunty", "cardinal"];
const sides = ["w", "b"];
const pieces = ["K", "Q", "R", "B", "N", "P"];

for (const set of sets) {
  const setDir = path.join(sourceRoot, set);
  if (!fs.existsSync(setDir)) {
    fs.mkdirSync(setDir, { recursive: true });
  }

  for (const side of sides) {
    for (const piece of pieces) {
      const fileName = `${side}${piece}.svg`;
      const svgPath = path.join(setDir, fileName);

      if (!fs.existsSync(svgPath)) {
        const url = `https://raw.githubusercontent.com/lichess-org/lila/master/public/piece/${set}/${fileName}`;
        const res = await fetch(url);
        if (!res.ok) throw new Error(`Failed to fetch ${url}: ${res.statusText}`);
        const text = await res.text();
        fs.writeFileSync(svgPath, text, "utf8");
      }

      const target = path.join(outputRoot, `piece_${set}_${side}${piece.toLowerCase()}.png`);
      await sharp(svgPath).resize(256, 256, { fit: "contain" }).png({ quality: 100, compressionLevel: 9 }).toFile(target);
    }
  }
  console.log(`Rendered piece set: ${set}`);
}

console.log("All pieces downloaded and rendered successfully!");


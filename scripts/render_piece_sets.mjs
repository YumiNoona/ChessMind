import path from "node:path";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const sharp = require("sharp");

const root = path.resolve(import.meta.dirname, "..");
const sourceRoot = path.join(root, "third_party", "chess_pieces");
const outputRoot = path.join(root, "app", "src", "main", "res", "drawable-nodpi");
const sets = ["rhosgfx", "fantasy", "spatial", "celtic", "chessnut"];
const sides = ["w", "b"];
const pieces = ["K", "Q", "R", "B", "N", "P"];

for (const set of sets) {
  for (const side of sides) {
    for (const piece of pieces) {
      const source = path.join(sourceRoot, set, `${side}${piece}.svg`);
      const target = path.join(outputRoot, `piece_${set}_${side}${piece.toLowerCase()}.png`);
      await sharp(source).resize(192, 192, { fit: "contain" }).png().toFile(target);
    }
  }
}

console.log(`Rendered ${sets.length * sides.length * pieces.length} chess piece images.`);

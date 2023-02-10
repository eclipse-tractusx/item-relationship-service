const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");
const TARGET_PATH = "../../target/";

fs.readdirSync(TARGET_PATH).forEach((file) => {
  let plantuml = spawn("java", [
    "-jar",
    "./plantuml.jar",
    "-tpng",
    "../../target/" + file,
    "-o",
    `../src/diagram-replacer/extracted-images/${path.parse(file).name}`,
  ]);
  plantuml.on("close", (code) => {
    console.log(`extracted .PNGs from ${file} with code ${code}`);
  });
});

const fs = require("fs");
const path = require("path");
const SOURCE_PATH = "../../target/";
const TARGET_PATH = "./generated-adocs/";
const IMAGES_PATH = "./assets/";

/**
 * CAUTION: Images get not replaced if you specified an imagedir variable in the .adoc
 */

fs.readdirSync(SOURCE_PATH).forEach((file) => {
  if (path.extname(file) === ".adoc") {
    fs.readFile(SOURCE_PATH + file, "utf8", (err, data) => {
      if (err) throw err;

      let imageList = [];
      // loop over images in extracted Image Directory of corresponding adoc
      fs.readdirSync(IMAGES_PATH + path.parse(file).name + "/").forEach(
        (image) => {
          // add suffix _000 to the first png that is exported to  by plantuml.jar
          if (path.parse(image).name === path.parse(file).name) {
            fs.rename(
              IMAGES_PATH + path.parse(file).name + "/" + image,
              IMAGES_PATH +
                path.parse(file).name +
                "/" +
                path.parse(image).name +
                "_000.png",
              function (err) {
                if (err) {
                  console.log(
                    "Error trying to read images: Access failed or no Images found. Error:"
                  );
                  console.log(err);
                }
              }
            );
          }
          imageList.push(image);
        }
      );

      if (!imageList.length) {
        console.log("No Images to replace.");
        return;
      }

      let ImageDirectoryPathForAdoc = IMAGES_PATH + path.parse(file).name + "/";

      const lines = data.split("\n");
      let insidePlantUmlBlock = false;
      const output = [];

      for (let i = 0; i < lines.length; i++) {
        // when line starts with plantuml tag replace with corresponding .PNG
        if (lines[i].startsWith("[plantuml, target=")) {
          const filename = lines[i].match(/target=(.*), format=svg/)[1];
          output.push(
            `image::${"." + ImageDirectoryPathForAdoc + imageList.shift()}[]`
          );
        } else if (lines[i].startsWith("@startuml")) {
          insidePlantUmlBlock = true;
        } else if (lines[i].startsWith("@enduml")) {
          insidePlantUmlBlock = false;
          continue;
        } else if (insidePlantUmlBlock) {
          continue;
        } else {
          output.push(lines[i]);
        }

        // delete empty black box
        if (
          output[output.length - 1] === "...." &&
          output[output.length - 2] === "...."
        ) {
          output.pop();
          output.pop();
        }
      }

      // create target directory for adoc with pngs if not existant
      if (!fs.existsSync(TARGET_PATH)) {
        fs.mkdirSync(TARGET_PATH);
      }

      fs.writeFile(
        `${TARGET_PATH}${file}`,
        output.join("\n"),
        "utf8",
        (err) => {
          if (err) throw err;
          console.log(
            `successfully replaced PlantUML Code inside ${file} with .PNG Images`
          );
        }
      );
    });
  }
});

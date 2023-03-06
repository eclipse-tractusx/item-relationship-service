const fs = require("fs");
const path = require("path");
const PATH_TO_MD_FILES = "../../target/generated-docs/";

fs.readdirSync(PATH_TO_MD_FILES).forEach((file) => {
  if (path.extname(file) === ".md") {
    fs.readFile(PATH_TO_MD_FILES + file, "utf8", (err, data) => {
      if (err) throw err;

      const lines = data.split("\n");
      const output = [];

      for (let i = 0; i < lines.length; i++) {
        // when line starts from _ and end with _ script will remove it (MD036 - Emphasis used instead of a heading)
        if (lines[i].startsWith("_") || lines[i].endsWith("_")) {
          output.push(lines[i].replace("_", "").replace("_", ""));
        // fix for MD033 - Inline HTML
        } else if (lines[i].startsWith("##### <")) {
          output.push(lines[i].replace(" <", " ").replace(">", ""));
        } else {
          output.push(lines[i]);
        }

      }

      fs.writeFile(
        `${PATH_TO_MD_FILES}${file}`,
        output.join("\n"),
        "utf8",
        (err) => {
          if (err) throw err;
          console.log(
            `successfully emphasis used instead of heading ${file}`
          );
        }
      );
    });
  }
});

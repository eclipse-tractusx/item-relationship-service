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
        if (lines[i].startsWith("# ")) {
          // replace first level headings with second level (need to be for other levels too)
          lines[i] = lines[i].replace("# ", "## ");
        }
        // when line starts with hashtag add new line after
        if (lines[i].startsWith("#") && (typeof lines[i+1] !== 'undefined')) {
          output.push(lines[i]);
          if (lines[i+1] !== "" && !lines[i+1].startsWith("#")) {
            output.push("");
          }
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
            `successfully fix headers in ${file}`
          );
        }
      );
    });
  }
});

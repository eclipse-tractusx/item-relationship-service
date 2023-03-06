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
        // when line contains https without marks around we need to add <> before and after (MD034 - Bare URL used)
        if (lines[i].includes(" https://") || lines[i].startsWith("https://")) {
          const firstIndex = lines[i].indexOf("https://");
          const lastIndex = lines[i].indexOf(" ", firstIndex + 1);
          const fixFirst = lines[i].replace("https://", "<https://");
          let afterReplacement;
          if (lastIndex === -1) {
            afterReplacement = fixFirst + ">";
          } else {
            afterReplacement = fixFirst.slice(0, lastIndex + 1) + ">" + fixFirst.slice(lastIndex + 1);
          }

          output.push(afterReplacement);
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
            `successfully fix https links ${file}`
          );
        }
      );
    });
  }
});

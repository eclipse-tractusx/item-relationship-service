/********************************************************************************
 * Copyright (c) 2026 Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V. (represented by Fraunhofer ISST)
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

const fs = require("fs");
const path = require("path");

const PATH_TO_MD_FILES = "../../target/generated-docs/";

/**
 * Normalizes Markdown tables so that markdownlint MD060 complains less
 * about pipe spacing and simple alignment deviations.
 *
 * Goal:
 * - Remove spaces directly adjacent to pipe characters
 * - Unify table rows with different spacing
 * - Only touch real Markdown tables, not code blocks or YAML fences
 */
function normalizeTableLine(line) {
  // Removes spaces directly before/after pipes:
  // "| a | b |" -> "| a | b |" stays the same in content,
  // but " | a | " / "|  a  |" etc. get cleaned up.
  let normalized = line;

  // Keep pipes at the start/end clean (no spaces before first pipe, no spaces after last pipe)
  normalized = normalized.replace(/^\s*\|\s*/, "|");
  normalized = normalized.replace(/\s*\|\s*$/, " |");

  // Normalize inner pipes (multiple blanks around pipe = single space)
  normalized = normalized.replace(/\s*\|\s*/g, " | ");

  // Remove multiple spaces in cells
  normalized = normalized.replace(/\s{2,}/g, " ");

  return normalized.trimEnd();
}

function isTableRow(line) {
  return /^\s*\|.*\|\s*$/.test(line);
}

function isTableSeparator(line) {
  return /^\s*\|?[\s:-]+\|[\s|:-]*$/.test(line);
}

// per md file
fs.readdirSync(PATH_TO_MD_FILES).forEach((file) => {
  if (path.extname(file) !== ".md") return;

  fs.readFile(PATH_TO_MD_FILES + file, "utf8", (err, data) => {
    if (err) throw err;

    const lines = data.split("\n");
    const output = [];

    let inCodeBlock = false;
    let inTable = false;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];

      if (line.startsWith("```")) {
        inCodeBlock = !inCodeBlock;
        inTable = false;
        output.push(line);
        continue;
      }

      if (inCodeBlock) {
        output.push(line);
        continue;
      }

      const currentIsTable = isTableRow(line) || isTableSeparator(line);

      if (currentIsTable) {
        inTable = true;
        output.push(normalizeTableLine(line));
        continue;
      }

      if (inTable) {
        // End of the table
        inTable = false;
      }

      output.push(line);
    }

    fs.writeFile(`${PATH_TO_MD_FILES}${file}`, output.join("\n"), "utf8", (writeErr) => {
      if (writeErr) throw writeErr;
      console.log(`successfully fixed tables in ${file}`);
    });
  });
});
#################################################################################
# Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
# Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# SPDX-License-Identifier: Apache-2.0
##################################################################################


##################################################################################
# Helper script for generating the documentation locally in WSL.
# The commands are copied from .github/workflows/publish-documentation.yaml.
#
# Prerequisistes:
#
#    1) Install Java 17 (OpenJDK)
#         sudo apt install openjdk-17-jdk openjdk-17-jre
#    2) Install NodeJS
#         sudo apt install nodejs
#    3) Install asciidoctor-reducer
#         sudo apt install ruby
#         sudo gem install asciidoctor-reducer
#    4) Install markdown lint cli
#         npm install markdownlint-cli2
#
# Please run this script from repository root:
#
#    ./docs/build-docs-locally.sh
#
##################################################################################


WD=$(pwd)
echo "$WD"


read -p "Do you want to build the API documentation? (y/n): " build_docs
if [ "$build_docs" == "y" ] || [ "$build_docs" == "Y" ]; then
    echo "Building API documentation with Maven..."
    mvn clean package -pl irs-common,irs-models,irs-policy-store,irs-testing,irs-edc-client,irs-registry-client,irs-api -DskipTests --batch-mode
    cp irs-api/target/generated-sources/openapi/index.html docs/src/docs/api-specification/index.html
else
    echo "Skipping API documentation."
fi


echo "Building documentation with Maven..."
mvn -f docs/pom.xml clean generate-resources --batch-mode

echo "Reducing docs..."
echo "$LANG"
locale
asciidoctor-reducer -o docs/target/adminguide.adoc docs/src/docs/administration/administration-guide.adoc
asciidoctor-reducer -o docs/target/arc42.adoc docs/src/docs/arc42/full.adoc

echo "Downloading PlantUML..."
wget -O plantuml.jar https://sourceforge.net/projects/plantuml/files/plantuml.jar/download
cp plantuml.jar docs/src/diagram-replacer/

echo "Extracting PNG images from documentation..."
pwd
cd docs/src/diagram-replacer/ || exit
pwd
node extract.js

echo "Replacing PlantUML code in docs with PNG images..."
node replace.js
cd "$WD" || exit
pwd

echo "Converting adminguide to Markdown..."
npx downdoc -o docs/target/generated-docs/adminguide.md docs/src/diagram-replacer/generated-adocs/adminguide.adoc
echo "Converting arc42 documentation to Markdown..."
npx downdoc -o docs/target/generated-docs/arc42.md docs/src/diagram-replacer/generated-adocs/arc42.adoc

echo "Post-processing markdown files"
cd docs/src/post-processing/ || exit
pwd

node fix_headers.js
node fix_no_emphasis.js
node fix_https_links.js
node fix_relative_links.js

cd "$WD" || exit
pwd

echo "Markdown linting..."
npx markdownlint-cli2 --config docs/.markdownlint.yaml docs/target/generated-docs/adminguide.md
npx markdownlint-cli2 --config docs/.markdownlint.yaml docs/target/generated-docs/arc42.md

echo "Moving assets to target directory..."
DOCTARGET="docs/target/generated-docs/assets/"
mv docs/src/diagram-replacer/assets/ $DOCTARGET

echo "Finished generating documentation: $DOCTARGET"
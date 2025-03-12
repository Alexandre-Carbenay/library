#!/bin/bash

installIfPlantJarNotFound () {
  if [[ ! -f "$1" ]]; then
    curl -sL https://github.com/plantuml/plantuml/releases/download/v1.2025.2/plantuml-1.2025.2.jar -o "$(dirname $(git rev-parse --git-dir))/.git/hooks/plantuml.jar"
  fi
}

echo "Running pre-commit to check for staged files to convert to plantuml diagrams"

# plantuml.jar file should be placed at the project directory
plantJarFile="$(dirname $(git rev-parse --git-dir))/.git/hooks/plantuml.jar"

installIfPlantJarNotFound $plantJarFile

plantUmlFilesToGenerate="";
diagramFilesToAddToGitCommit="";
while read status plantUmlFile; do

  plantUmlFileWithoutExtension=${plantUmlFile%%.*}

  # clean up image file
  if [[ $status == 'D' ]]; then
    rm "${plantUmlFileWithoutExtension}.png"
  fi

  if [[ $status == 'A' ]] || [[ $status == 'M' ]]; then
    plantUmlFilesToGenerate="${plantUmlFilesToGenerate} ${plantUmlFile}"
    diagramFilesToAddToGitCommit="${diagramFilesToAddToGitCommit} ${plantUmlFileWithoutExtension%%.*}.png"
  fi

done <<< "$(git diff-index --cached HEAD --name-status | grep '\.puml$')"

echo "Generating plantuml diagrams for $plantUmlFilesToGenerate"

java -jar $plantJarFile -progress $plantUmlFilesToGenerate

git add $diagramFilesToAddToGitCommit

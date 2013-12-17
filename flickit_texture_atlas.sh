#!/bin/bash
java -classpath flickit/libs/gdx.jar:flickit/libs/gdx-tools.jar com.badlogic.gdx.tools.imagepacker.TexturePacker2 flickit-android/raw/ . game_atlas

echo -n "Moving assets into place..."
mv game_atlas* flickit-android/assets/data/

echo "DONE!"

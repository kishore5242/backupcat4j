### FFMPEG binaries
- Download ffmpeg essentials
- Create ffmpeg folder
- Place ffmpeg.exe and ffprobe.exe inside the ffmpeg folder

### Custom runtime
Run below command to create custom runtime
```declarative
jlink \
  --module-path "%JAVA_HOME%\jmods;D:\apps\javafx-jmods-24.0.1" \
  --add-modules java.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.naming,java.sql \
  --output jre \
  --compress=2 \
  --strip-debug \
  --no-header-files \
  --no-man-pages
```
Above command create a `jre` folder here

### Running the JavaFX jar file

```declarative
"D:\projects\idea\backupcat4j\bin\jre\bin\java.exe" \
  --enable-native-access=ALL-UNNAMED \
  --add-modules javafx.controls,javafx.fxml \
  -cp backupcat4j-fx.jar \
  org.kapps.AppUI
```
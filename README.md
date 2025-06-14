<p>
  <img src="src/main/resources/app-icon.png" alt="BackupCat4j logo">
</p>

# 🐾 BackupCat4j

**BackupCat4j** is a lightweight Java-based tool designed to back up files from a source folder to a destination directory with intelligence and reliability. It includes robust logging, resume capabilities, media compression (especially for videos), and smart organization based on file types.

---

## ✨ Features

* ✅ **File-by-File Copying**: Transfers files individually for better error isolation.
* 📦 **Video Compression**: Compresses video files on-the-fly using a target average bitrate (via FFMPEG).
* 📁 **Auto File Organization**: Based on the file types, each file will be organized into respective folders.
    * Videos
    * Images
    * Documents
    * Audio
    * Others
* ⚙️ **Highly Configurable**: Easy customization of backup options through the builder pattern.
* 🔁 **Resume Support**: Resumes from the last successful file in case of interruptions.
* 📝 **Understandable Logs**: Tracks progress and reports errors clearly.
* 🚧 **More Features Coming Soon**:

    * Incremental backups
    * File verification using checksums
    * More CLI options

---

# 🚀 Getting Started

## Option 1: Run the Executable

1. **Download** the latest `backupcat4j-x.x.x.zip` from [here](https://github.com/kishore5242/backupcat4j/releases)
2. **Extract** the contents to any folder on your system.
3. **Run** `BackupCat4j.exe`. The app will open in a window and guide you through the required inputs, using sensible defaults.
4. **Result** will be printed in the window.

<p>
  <img src="src/main/resources/app-screenshot.png" alt="BackupCat4j App screenshot" width="600px">
</p>

Output folder structure
```
B:\backup\20250120\
├── Videos\
├── Images\
├── Documents\
├── Audio\
└── Others\
```

## Option 2: Command Line Interface

If you prefer to run the JAR directly with custom options, following below steps:

### 1. Install prerequisites

Make sure the following tools are installed:

* [Java 24 (or higher)](https://jdk.java.net/)
* [FFMPEG (latest version)](https://ffmpeg.org/download.html)

### 2. Download the latest JAR file from [here](https://github.com/kishore5242/backupcat4j/releases)

### 3. Run the java command

Replace the paths and options as needed.

```bash
java -jar backupcat4j-fx.jar \
source="I:\media" \
target="B:\backup" \
ffmpeg="D:\ffmpeg-7.1.1-essentials_build\ffmpeg.exe" \
ffprobe="D:\ffmpeg-7.1.1-essentials_build\ffprobe.exe" \
organize=FULL \
--compress \
--resume \
--replace \
--bitrate=3000000
```

## 🧑‍💻 Contributing

### 1. Clone the Project

```bash
git clone https://github.com/yourusername/BackupCat4j.git
cd BackupCat4j
```

### 2. Open in Your IDE

Import as a **Maven** Java project.

### 3. Set Backup Options

Modify the `main` method in `App.java` to configure your test backup:

```java
BackupOptions backupOptions = BackupOptions.builder()
        .source("I:\\media\\dump") // Source folder
        .target("B:\\backup\\20250120") // Destination folder
        .replace(true) // Overwrite if file exists
        .organize(OrganizeMode.FULL) // Organize files into type-based folders
        .compressVideos(true) // Enable video compression
        .maxAvgBitRate(3_000_000) // Set target bitrate for videos
        .ffmpeg("D:\\apps\\ffmpeg\\bin\\ffmpeg.exe") // Path to ffmpeg
        .ffprobe("D:\\apps\\ffmpeg\\bin\\ffprobe.exe") // Path to ffprobe
        .skipOthers(false) // Include uncategorized files
        .resume(true) // Enable resume feature
        .build();
```

### 4. Run the Application

Simply run the `main` method from your IDE.

### 5. Make code changes

Feel free to fix bugs or make improvements as needed. It would also be greatly appreciated if you could include relevant **JUnit tests** to ensure your changes are reliable and well-tested.

### 6. Create pull requests
Pull requests and feature suggestions are welcome. Let’s make **BackupCat4j** the go-to utility for organized, smart backups.

---

## 🛠️ Tools Used

* **Java 24** – Core language
* **FFMPEG / FFPROBE** – For video inspection and compression

---

## ⚠️ Troubleshooting

* Ensure the FFMPEG binary paths are correct and accessible.
* Recommended to test on a small batch before running on large sets.
* Some files may be skipped if unreadable, corrupt, or unsupported—check logs.

---

## 🗓️ Roadmap

* [ ] Incremental backup (only changed files)
* [ ] File type filter
* [ ] Linux executable file
* [ ] Dry-run mode
* [ ] CLI interface improvements
* [ ] Config file support (e.g., `backup.json`)
* [ ] Hash-based integrity verification

---

# 🐾 BackupCat4j

**BackupCat4j** is a lightweight Java-based tool designed to back up files from a source folder to a destination directory with intelligence and reliability. It includes robust logging, resume capabilities, media compression (especially for videos), and smart organization based on file types.

---

## ✨ Features

* ✅ **One-by-One File Copying**: Transfers files individually for better error isolation.
* 📝 **Understandable Logs**: Tracks progress and reports errors clearly.
* 🔁 **Resume Support**: Resumes from the last successful file in case of interruptions.
* 📦 **Video Compression**: Compresses video files on-the-fly using a target average bitrate (via FFMPEG).
* 📁 **Auto File Organization**:

    * Videos
    * Images
    * Documents
    * Audio
    * Others
* ⚙️ **Highly Configurable**: Easy customization of backup options through the builder pattern.
* 🚧 **More Features Coming Soon**:

    * Incremental backups
    * File verification using checksums
    * UI/CLI options

---

## 🚀 Getting Started

### ⚒️ Prerequisites

Make sure the following tools are installed:

* [Java 24 (or higher)](https://jdk.java.net/)
* [FFMPEG (latest version)](https://ffmpeg.org/download.html)

### 1. Download the jar file

### 2. Run the java command
```declarative
java -jar backupcat4j.jar \
source="I:\media" \
target="B:\backup" \
ffmpeg="D:\ffmpeg\ffmpeg.exe" \
ffprobe="D:\ffmpeg\ffprobe.exe" \
organize=FULL \
--compress \
--resume \
--replace \
--bitrate=3000000
```

### 3. Output Directory Structure (Example)

```
B:\backup\20250120\
├── Videos\
├── Images\
├── Documents\
├── Audio\
└── Others\
```
---

## 🧑‍💻 Contributing

Pull requests and feature suggestions are welcome. Let’s make **BackupCat4j** the go-to utility for organized, smart backups.

### 1. Clone the Project

```bash
git clone https://github.com/yourusername/BackupCat4j.git
cd BackupCat4j
```

### 2. Open in Your IDE

Import as a **Maven** or **Gradle** Java project (depending on your setup).

### 3. Set Backup Options

Modify the `main` method to configure your backup:

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

---

## 🛠️ Tools Used

* **Java 24** – Core language
* **FFMPEG / FFPROBE** – For video inspection and compression

---

## ⚠️ Notes

* Ensure the FFMPEG binary paths are correct and accessible.
* Recommended to test on a small batch before running on large sets.
* Some files may be skipped if unreadable, corrupt, or unsupported—check logs.

---

## 🗓️ Roadmap

* [ ] Incremental backup (only changed files)
* [ ] Dry-run mode
* [ ] CLI interface improvements
* [ ] Config file support (e.g., `backup.json`)
* [ ] Hash-based integrity verification

---

## 📄 License

MIT License – *Feel free to use and modify with attribution.*

---
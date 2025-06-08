package org.kapps.backup;

public class OrganizedFolder {
    public static final String IMAGES = "Images";
    public static final String VIDEOS = "Videos";
    public static final String AUDIO = "Audio";
    public static final String DOCUMENTS = "Documents";
    public static final String OTHERS = "Others";

    private OrganizedFolder() {
    }

    public static String forFileType(FileType fileType) {
        return switch (fileType) {
            case IMAGE -> IMAGES;
            case VIDEO -> VIDEOS;
            case AUDIO -> AUDIO;
            case DOCUMENT -> DOCUMENTS;
            default -> OTHERS;
        };
    }
}

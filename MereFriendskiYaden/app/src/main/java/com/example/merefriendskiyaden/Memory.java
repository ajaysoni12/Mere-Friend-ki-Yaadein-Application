package com.example.merefriendskiyaden;

import android.net.Uri;

public class Memory {
    private String venue, date, memoryId, fileName;
    Uri uri;

    public Memory(String memoryId, String venue, String date, Uri uri, String fileName) {
        this.memoryId = memoryId;
        this.venue = venue;
        this.date = date;
        this.uri = uri;
        this.fileName = fileName;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

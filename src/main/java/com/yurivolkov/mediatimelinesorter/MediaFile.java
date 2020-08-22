package com.yurivolkov.mediatimelinesorter;
/*
 * Copyright (c) 2017 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MediaFile implements Comparable<MediaFile> {
    private final static List<String> firmTimeExtensions = Arrays.asList("jpg", "jpeg", "raw");

    final Path path;
    final ParsedName name;
    private String fileName = "";
    final long fileTimeInitial;
    private long fileTime = 0;

    public MediaFile(@NotNull Path path) {
        this.path = path;
        fileName = path.getFileName().toString();
        name = ParsedName.parse(fileName);
        try {
            if (exists()) {
                fileTime = Files.getLastModifiedTime(path).toMillis();
            } else {
                System.out.println("Not a file: " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileTimeInitial = fileTime;
    }

    public boolean exists() {
        return exists(path);
    }

    public boolean targetExists() {
        return exists(getTargetPath());
    }

    private static boolean exists(Path path) {
        try {
            return Files.isRegularFile(path);
        } catch (SecurityException e) {
            System.err.println("Security exception for: " + path);
        }
        return false;
    }

    public boolean hasSourceTimeline() {
        return name.hasSourceTimeline();
    }

    public long getFileTime() {
        return fileTime;
    }

    public void setFileTime(long fileTime) {
        this.fileTime = fileTime;
    }

    @Override
    public int compareTo(MediaFile o) {
        return Long.compare(fileTime, o.fileTime);
    }

    public void setFileName(@NotNull String fileName) {
        this.fileName = fileName;
    }

    public boolean changed() {
        return nameChanged() || timeChanged();
    }

    boolean nameChanged() {
        return !this.name.name.equals(fileName);
    }

    boolean timeChanged() {
        return fileTime != fileTimeInitial;
    }

    /**
     * @return true if saved and succeeded
     */
    public boolean save() {
        if (!changed()) {
            System.out.println("No changes for " + name.name);
            return false;
        }
        if (!exists()) {
            System.err.println("Source doesn't exist: " + path);
            return false;
        }
        if (nameChanged()) {
            if (targetExists()) {
                System.err.println("Target exists for: " + path +
                        ", target: " + getTargetPath());
                return false;
            }

            String msgLog = "Renaming " + name.name + " to " + fileName;
            try {
                Files.move(path, getTargetPath());
                System.out.println(msgLog);
            } catch (IOException e) {
                System.err.println(msgLog + ", error:" + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        if (timeChanged()) {
            if (!targetExists()) {
                System.err.println("Target doesn't exist for: " + path +
                        ", target: " + getTargetPath());
                return false;
            }

            String msgLog = "Changing time of " + name.name + ": " + timeDifference() + " to " +
                    new Date((getFileTime()));
            try {
                Files.setLastModifiedTime(getTargetPath(), FileTime.fromMillis(getFileTime()));
                System.out.println(msgLog);
            } catch (IOException e) {
                System.err.println(msgLog + ", error:" + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @NotNull
    private String timeDifference() {
        long shift = fileTime - fileTimeInitial;
        if (Math.abs(shift) > 1000 * 60 * 10) {
            return shift / 1000 / 60 + " min";
        }
        if (Math.abs(shift) > 1000 * 10) {
            return shift / 1000 + " sec";
        }
        return shift + " ms";
    }

    Path getTargetPath() {
        return path.resolveSibling(fileName);
    }

    public long getBestTime() {
        return hasFirmTime() && name.parsedTime > 0
            ? name.parsedTime
            : getFileTime();
    }

    public boolean hasFirmTime() {
        return name.parsedTime > 0 || firmTimeExtensions.contains(name.extension.toLowerCase());
    }

    public MediaFile getSameFile(List<MediaFile> files) {
        for (MediaFile file : files) {
            if (name.getPermanentNamePart().equals(file.name.getPermanentNamePart())) {
                return file;
            }
        }
        return new MediaFile(files.get(0).path.resolveSibling("notFound.txt"));
    }

    @Override
    public String toString() {
        return "MediaFile{" +
                name + ", " +
                (fileTime == 0 ? " - " : new Date(fileTime)) +
                ( timeChanged() ? ", " + ( fileTime - fileTimeInitial ) + "ms" : "") +
                '}';
    }

    public void tryToSetTimeFromName() {
        if (name.parsedTime > 0) {
            setFileTime(name.parsedTime);
        }
    }
}

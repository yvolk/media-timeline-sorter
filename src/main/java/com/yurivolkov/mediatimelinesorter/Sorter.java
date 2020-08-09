package com.yurivolkov.mediatimelinesorter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sorter {
    final Path dir;

    public Sorter(@NotNull Path dir) {
        this.dir = dir;
        String s = dir.toAbsolutePath().toString();
        System.out.println("Processing the directory: " + s);
    }

    public List<MediaFile> getFiles() {
        List<MediaFile> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path: stream) {
                if (Files.isRegularFile(path)) {
                    MediaFile mediaFile = new MediaFile(path);
                    files.add(mediaFile);
                    System.out.println(mediaFile.toString());
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }
        return files;
    }

    public void fixFileTime(List<MediaFile> files) {
        List<String> timelineKeys = new ArrayList<>();
        for (MediaFile file : files) {
            if (!timelineKeys.contains(file.name.sourceKey)) {
                timelineKeys.add(file.name.sourceKey);
            }
        }
        for (String key : timelineKeys) {
            fixTimeForOneTimeline(getSortedTimelineByKey(files, key));
        }
    }

    @NotNull
    private List<MediaFile> getSortedTimelineByKey(List<MediaFile> files, String key) {
        List<MediaFile> timelineFiles = new ArrayList<>();
        for (MediaFile file : files) {
            if (key.equals(file.name.sourceKey)) {
                timelineFiles.add(file);
            }
        }
        timelineFiles.sort(Comparator.comparingLong(o -> o.name.sourceOrder));
        return timelineFiles;
    }

    private void fixTimeForOneTimeline(List<MediaFile> files) {
        int indFirmPrev = -1;
        int indFirmNext = -1;
        int ind = 0;
        while ( ind >= 0 && ind < files.size()) {
            if (ind >= indFirmNext) {
                indFirmNext = indFirmNext(indFirmPrev, files);
            }
            MediaFile file = files.get(ind);
            if (file.hasFirmTime()) {
                if (indFirmPrev >= 0 && files.get(indFirmPrev).getFileTime() > file.getFileTime()) {
                    throw new IllegalStateException("file " + files.get(indFirmPrev) +
                            " is older than " + file );
                }
                indFirmPrev = ind;
            } else if (
                    ind > 0 && files.get(ind - 1).getFileTime() > file.getFileTime() ||
                    indFirmNext > 0 && files.get(indFirmNext).getFileTime() < file.getFileTime()
                ) {
                ind = fixFileTimesTillNextFirm(indFirmPrev, indFirmNext, files);
            } else {
                file.tryToSetTimeFromName();
            }
            ind++;
        }
    }

    private int indFirmNext(int indFirmPrev, List<MediaFile> files) {
        int ind1 = indFirmPrev + 1;
        while (ind1 < files.size()) {
            MediaFile file = files.get(ind1);
            if (file.hasFirmTime()) {
                return ind1;
            }
            ind1++;
        }
        return -1;
    }

        private int fixFileTimesTillNextFirm(int indFirmPrev, int indFirmNext, List<MediaFile> files) {
        long timeFrom = indFirmPrev < 0 ? 0 : files.get(indFirmPrev).getFileTime();
        long timeTo = indFirmNext < 0 ? 0 : files.get(indFirmNext).getFileTime() - 1000 * (indFirmNext - indFirmPrev);
        if (timeTo < 0) {
            timeTo = 0;
        }
        if (timeFrom > 0 && timeTo > 0 && timeTo < timeFrom ) {
            timeTo = timeFrom + files.get(indFirmNext).getFileTime();
        }
        long timeShiftForward = -1;
        if (indFirmPrev >= 0 && indFirmPrev < (files.size() - 1)) {
            timeShiftForward = files.get(indFirmPrev).getFileTime() - files.get(indFirmPrev + 1).getFileTime() + 1000;
        }
        long timeShiftBack = -1;
        if (indFirmNext > 0) {
            timeShiftBack = files.get(indFirmNext - 1).getFileTime() + 1000 - files.get(indFirmNext).getFileTime();
        }
        if (timeShiftForward > 0) {
            long fileTimePrev = timeFrom;
            for (int ind2 = indFirmPrev + 1; ind2 < (indFirmNext >= 0 ? indFirmNext : files.size()); ind2++) {
                long fileTime = files.get(ind2).getFileTime() + timeShiftForward;
                if (fileTime <= fileTimePrev) {
                    fileTime = fileTimePrev + 1000;
                }
                if (timeFrom > 0 && fileTime <= timeFrom) {
                    fileTime = timeFrom;
                    timeFrom += 1000;
                }
                if (timeTo > 0 && fileTime >= timeTo) {
                    fileTime = timeTo;
                    timeTo += 1000;
                }
                files.get(ind2).setFileTime(fileTime);
                fileTimePrev = fileTime;
            }
        } else if (timeShiftBack > 0) {
            long fileTimePrev = timeFrom;
            for (int ind2 = indFirmPrev + 1; ind2 < (indFirmNext >= 0 ? indFirmNext : files.size()); ind2++) {
                long fileTime = files.get(ind2).getFileTime() - timeShiftBack;
                if (fileTime <= fileTimePrev) {
                    fileTime = fileTimePrev + 1000;
                }
                if (timeFrom > 0 && fileTime <= timeFrom) {
                    fileTime = timeFrom;
                    timeFrom += 1000;
                }
                if (timeTo > 0 && fileTime >= timeTo) {
                    fileTime = timeTo;
                    timeTo += 1000;
                }
                files.get(ind2).setFileTime(fileTime);
                fileTimePrev = fileTime;
            }
        }
        return indFirmNext < 0 ? files.size() : indFirmNext - 1;
    }

    public void sort(List<MediaFile> files) {
        Collections.sort(files);
        int nDigits = calcNDigits(files.size());
        int order = 1;
        for (MediaFile file : files) {
            file.setFileName(String.format("%0" + nDigits + "d-%s", order, file.name.getPermanentNamePart()));
            order++;
        }
    }

    public void saveChanges(List<MediaFile> files) {
        int savedCount = 0;
        for (MediaFile file : files) {
            if (file.save()) {
                savedCount++;
            }
        }
        System.out.println("Saved " + savedCount + " of " + files.size() + " files");
    }

    int calcNDigits(int size) {
        return (int) Math.ceil(Math.log10(size + 1));
    }
}

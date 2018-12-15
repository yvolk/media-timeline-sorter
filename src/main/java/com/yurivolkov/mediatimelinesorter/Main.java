package com.yurivolkov.mediatimelinesorter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Path dir = Paths.get("");
        Sorter sorter = new Sorter(dir);
        List<MediaFile> files = sorter.getFiles();
        sorter.fixFileTime(files);
        sorter.sort(files);
        sorter.saveChanges(files);
    }
}

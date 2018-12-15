package com.yurivolkov.mediatimelinesorter;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
public class SorterTest {
    long baseTime = System.currentTimeMillis();

    @Test
    public void calcNDigits() throws Exception {
        Sorter sorter = new Sorter(Paths.get(""));
        assertEquals(0, sorter.calcNDigits(0));
        assertEquals(1, sorter.calcNDigits(1));
        assertEquals(1, sorter.calcNDigits(9));
        assertEquals(2, sorter.calcNDigits(10));
        assertEquals(2, sorter.calcNDigits(99));
        assertEquals(3, sorter.calcNDigits(100));
        assertEquals(3, sorter.calcNDigits(999));
        assertEquals(4, sorter.calcNDigits(1000));
    }

    @Test
    public void wholeProcess() throws Exception {
        Path currentDir = Paths.get("");
        listDir(currentDir);
        Path testFilesToChange = currentDir.resolve("target/test-classes/test-files-to-change");
        if (Files.exists(testFilesToChange)) {
            System.out.println("Recreating existing dir: " + testFilesToChange);
            for (Path target : listDir(testFilesToChange)) {
                Files.delete(target);
            }
            Files.delete(testFilesToChange);
            if (Files.exists(testFilesToChange)) {
                fail("Couldn't delete: " + testFilesToChange);
            }
        }
        Files.createDirectory(testFilesToChange);

        MediaFile yp38 = addFile(testFilesToChange, "yp0038.jpg", 0);
        MediaFile yp39 = addFile(testFilesToChange, "yp0039.mp4", -200);
        MediaFile yp40 = addFile(testFilesToChange, "yp0040.mp4", -200);
        MediaFile yp45 = addFile(testFilesToChange, "yp0045.jpg", 300);
        MediaFile yc19 = addFile(testFilesToChange, "yc019.jpg", -500);
        MediaFile yc21 = addFile(testFilesToChange, "yc021.mp4", -1000);
        MediaFile yc50 = addFile(testFilesToChange, "yc050.mp4", -850);
        MediaFile pa1 = addFile(testFilesToChange, "pa1.mp4", 0);
        MediaFile pa2 = addFile(testFilesToChange, "pa2.mp4", -200);
        MediaFile pa3 = addFile(testFilesToChange, "pa3.jpg", -1010);
        MediaFile pa4 = addFile(testFilesToChange, "pa4.jpg", 300);
        MediaFile s1468 = addFile(testFilesToChange, "s1468.jpg", 100);
        MediaFile s1469 = addFile(testFilesToChange, "s1469.mp4", 10000);
        MediaFile s1470 = addFile(testFilesToChange, "s1470.jpg", 150);

        listDir(testFilesToChange);

        Sorter sorter = new Sorter(testFilesToChange);
        List<MediaFile> files = sorter.getFiles();
        sorter.fixFileTime(files);
        sorter.sort(files);
        sorter.saveChanges(files);

        files = sorter.getFiles();

        int time1 = 0;
        assertTime(files, yp38, time1);
        assertTime(files, yp39, time1 + 1);
        assertTime(files, yp40, time1 + 2);
        assertTime(files, yp45, 300);

        time1 = -500;
        assertTime(files, yc19, time1);
        assertTime(files, yc21, time1 + 1);
        assertTime(files, yc50, time1 + 1 + 150);

        time1 = -1010;
        assertTime(files, pa3, time1);
        assertTime(files, pa4, 300);
        assertTime(files, pa1, time1 - 3);
        assertTime(files, pa2, time1 - 2);

        assertTime(files, s1468, 100);
        assertTime(files, s1469, 148);
        assertTime(files, s1470, 150);

    }

    private void assertTime(List<MediaFile> files, MediaFile file, int seconds) {
        assertEquals(file.toString(), seconds * 1000,
                getByPermanentName(files, file.name.getPermanentNamePart()).getFileTime() - baseTime);
    }

    private MediaFile getByPermanentName(List<MediaFile> files, String permanentNamePart) {
        for (MediaFile file : files) {
            if (permanentNamePart.equals(file.name.getPermanentNamePart())) {
                return file;
            }
        }
        return new MediaFile(files.get(0).path.resolveSibling("notFound.txt"));
    }

    private MediaFile addFile(Path dir, String fileName, int seconds) throws IOException {
        Path path = dir.resolve(fileName);
        Files.createFile(path);
        Files.setLastModifiedTime(path, FileTime.fromMillis(baseTime + seconds * 1000));
        return new MediaFile(path);
    }

    public List<Path> listDir(Path dir) {
        System.out.println("Listing dir: '" + dir + "'");
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path: stream) {
                System.out.println("path: " + path);
                files.add(path);
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }
        return files;
    }

}
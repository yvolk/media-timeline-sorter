package com.yurivolkov.mediatimelinesorter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

public class ParsedNameTest {
    @Test
    public void parse() throws Exception {
        assertOneName("00010-yp0038.jpg", 10, "yp0038.jpg",
                true, "yp", 38, "jpg");
        assertOneName("-yp0038.jpg", 0, "yp0038.jpg",
                true, "yp", 38, "jpg");
        assertOneName("yp38.jpg", 0, "yp38.jpg",
                true, "yp", 38, "jpg");
        assertOneName("1-yp38.mp4", 1, "yp38.mp4",
                true, "yp", 38, "mp4");
    }

    private void assertOneName(String name, int globalOrder, String permanentNamePart,
                               boolean hasSourceTimeline, String sourceKey, int sourceOrder, String extension) {
        ParsedName parsedName = ParsedName.parse(name);
        assertEquals(globalOrder, parsedName.globalOrder);
        assertEquals(permanentNamePart, parsedName.getPermanentNamePart());
        assertEquals(hasSourceTimeline, parsedName.hasSourceTimeline());
        assertEquals(sourceKey, parsedName.sourceKey);
        assertEquals(sourceOrder, parsedName.sourceOrder);
        assertEquals(extension, parsedName.extension);
    }

    @Test
    public void parseTime() throws Exception {
        assertOneTime("yv015-VID_20200718_210725.mp4");
        assertOneTime("125-yv015-VID_20200718_210725.mp4");
    }

    private void assertOneTime(String name) {
        ParsedName parsedName = ParsedName.parse(name);
        assertTrue(parsedName.toString(), parsedName.parseTimeFromName() > 0);
    }
}
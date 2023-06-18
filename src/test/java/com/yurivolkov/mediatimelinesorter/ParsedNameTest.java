package com.yurivolkov.mediatimelinesorter;

import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
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
    public void parse() {
        assertOneName("00010-yp0038.jpg", 10, "yp0038.jpg",
                true, "yp", 38, "jpg");
        assertOneName("-yp0038.jpg", 0, "yp0038.jpg",
                true, "yp", 38, "jpg");
        assertOneName("yp38.jpg", 0, "yp38.jpg",
                true, "yp", 38, "jpg");
        assertOneName("1-yp38.mp4", 1, "yp38.mp4",
                true, "yp", 38, "mp4");
        assertOneName("zagv036-вода-бурлит.mov", 0, "zagv036-вода-бурлит.mov",
                true, "zagv", 36, "mov");
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
    public void parseTime() {
        // Time with milliseconds is in UTC
        // Ignore time in seconds

        long parsedTime = 1595095645000L;
        long expectedTime = parsedTime + TimeZone.getDefault().getRawOffset();

        assertOneTime("yv015-VID_20200718_210725000.mp4", expectedTime);
        assertOneTime("yv015-VID_20200718_210725.mp4", 0);
        assertOneTime("yv015-VID_20200718_210725000_Organic_Maps.mp4", 0);
        assertOneTime("yv015-20200718_210725000.mp4", expectedTime);
        assertOneTime("yv015-20200718_210725000-super.mp4", expectedTime);
        assertOneTime("125-yv015-VID_20200718_210725000.mp4", expectedTime);
        assertOneTime("003-yv015-VID_20200718_210725000-best-clip.mp4", expectedTime);
        assertOneTime("zagv036-вода-бурлит.mov", 0);

        assertOneTime("yp003-20200718_210725000-10.jpg", expectedTime);

        // With milliseconds
        assertOneTime("yp003-20200718_210725910.jpg", expectedTime + 910);
        assertOneTime("yp003-20200718_210725910.MP.jpg", expectedTime + 910);
    }

    private void assertOneTime(String name, long parsedTime) {
        ParsedName parsedName = ParsedName.parse(name);
        assertEquals(parsedName.toString(), parsedTime, parsedName.parsedTime);
    }
}
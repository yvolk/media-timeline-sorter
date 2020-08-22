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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParsedName {
    @NotNull
    public final String name;
    public final long globalOrder;
    private final int indPermanentFileNamePart;
    @NotNull
    public final String sourceKey;
    public final long sourceOrder;
    @NotNull
    public final String extension;
    /** 0 if failed to parse */
    public final long parsedTime;

    private ParsedName(@NotNull String name) {
        this.name = name;
        globalOrder = calcGlobalOrder();
        indPermanentFileNamePart = calcPermanentFileNamePart();
        sourceKey = calcSourceTimelineKey();
        sourceOrder = calcSourceTimelineOrder();
        extension = calcExtension();
        parsedTime = parseTimeFromName();
    }

    public static ParsedName parse(@NotNull String name) {
        return new ParsedName(name);
    }

    public boolean hasSourceTimeline() {
        return sourceKey.length() > 0;
    }

    public String getPermanentNamePart() {
        return name.substring(indPermanentFileNamePart);
    }

    private long calcGlobalOrder() {
        int ind = 0;
        while (isDigit(name, ind)) {
            ind++;
        }
        if (isMinus(name, ind) && ind > 0) {
            return Long.parseLong(name.substring(0, ind));
        }
        return 0;
    }

    private int calcPermanentFileNamePart() {
        int ind = 0;
        while (isDigit(name, ind)) {
            ind++;
        }
        if (isMinus(name, ind) && name.length() > ind + 1) {
            return ind + 1;
        }
        return 0;
    }

    private String calcSourceTimelineKey() {
        int ind = indPermanentFileNamePart;
        while (isValidTimelineKeyChar(name, ind)) {
            ind++;
        }
        if (isDigit(name, ind) && ind > indPermanentFileNamePart) {
            return name.substring(indPermanentFileNamePart, ind);
        }
        return "";
    }

    private long calcSourceTimelineOrder() {
        int ind = indAfterSourceTimelineOrder();
        if (ind >= 0) {
            int indStart = indPermanentFileNamePart + sourceKey.length();
            return indStart < ind ? Long.parseLong(name.substring(indStart, ind)) : 0;
        }
        return 0;
    }

    private int indAfterSourceTimelineOrder() {
        if (hasSourceTimeline()) {
            int indStart = indPermanentFileNamePart + sourceKey.length();
            int ind = indStart;
            while (isDigit(name, ind)) {
                ind++;
            }
            return ind;
        }
        return -1;
    }

    private String calcExtension() {
        int indStart = name.lastIndexOf('.');
        if (indStart >= 0 && name.length() >= indStart) {
            return name.substring(indStart + 1);
        }
        return "";
    }

    private boolean isValidTimelineKeyChar(String name, int ind) {
        if (name == null || name.length() <= ind) {
            return false;
        }
        return !isDigit(name, ind) && !isMinus(name, ind);
    }

    static boolean isDigit(String name, int ind) {
        if (name == null || name.length() <= ind || ind < 0) {
            return false;
        }
        return "0123456789".contains(name.substring(ind, ind + 1));
    }

    static boolean isMinus(String name, int ind) {
        if (name == null || name.length() <= ind) {
            return false;
        }
        return "-".contentEquals(name.substring(ind, ind + 1));
    }

    /**
     * @return 0 if failed to parse
     */
    private long parseTimeFromName() {
        // e.g. from VID_20200718_210725.mp4
        String name = dropExtension(getNameAfterSourceTimelineOrder());
        int ind = -1;
        while (!isDigit(name, ind + 1) && ind < name.length()) {
            ind++;
        }
        if (!isDigit(name, ind+1)) return 0;

        String toParse = name.substring(ind + 1);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        try {
            return format.parse(toParse).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    public String getNameAfterSourceTimelineOrder() {
        int ind = indAfterSourceTimelineOrder();
        if (ind >= 0) {
            return name.substring(ind + (name.charAt(ind) == '-' ? 1 : 0));
        }
        return name;
    }

    static String dropExtension(String name) {
        int indStart = name.lastIndexOf('.');
        if (indStart >= 0 && name.length() >= indStart) {
            return name.substring(0, indStart);
        }
        return name;
    }

    @Override
    public String toString() {
        return "ParsedName{" +
                "name='" + name + '\'' +
                ", globalOrder='" + globalOrder + '\'' +
                ", indPermanentFileNamePart=" + indPermanentFileNamePart +
                ", sourceKey='" + sourceKey + '\'' +
                ", sourceOrder=" + sourceOrder +
                (parsedTime == 0 ? "" : ", " + new Date(parsedTime)) +
                '}';
    }
}

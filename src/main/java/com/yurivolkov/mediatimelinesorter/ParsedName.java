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

    private ParsedName(@NotNull String name) {
        this.name = name;
        globalOrder = calcGlobalOrder();
        indPermanentFileNamePart = calcPermanentFileNamePart();
        sourceKey = calcSourceTimelineKey();
        sourceOrder = calcSourceTimelineOrder();
        extension = calcExtension();
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
        if (hasSourceTimeline()) {
            int indStart = indPermanentFileNamePart + sourceKey.length();
            int ind = indStart;
            while (isDigit(name, ind)) {
                ind++;
            }
            return Long.parseLong(name.substring(indStart, ind));
        }
        return 0;
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

    private static boolean isDigit(String name, int ind) {
        if (name == null || name.length() <= ind) {
            return false;
        }
        return "0123456789".contains(name.substring(ind, ind + 1));
    }

    private static boolean isMinus(String name, int ind) {
        if (name == null || name.length() <= ind) {
            return false;
        }
        return "-".contentEquals(name.substring(ind, ind + 1));
    }

    @Override
    public String toString() {
        return "ParsedName{" +
                "name='" + name + '\'' +
                ", globalOrder='" + globalOrder + '\'' +
                ", indPermanentFileNamePart=" + indPermanentFileNamePart +
                ", sourceKey='" + sourceKey + '\'' +
                ", sourceOrder=" + sourceOrder +
                '}';
    }
}

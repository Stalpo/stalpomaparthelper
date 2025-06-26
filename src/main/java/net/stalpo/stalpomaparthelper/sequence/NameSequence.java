package net.stalpo.stalpomaparthelper.sequence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameSequence {
    // open a shulker/an anvil to restore the sequence (true while renaming)
    // for instance, an anvil has broken, but we are a little bit ahead. Fix it
    // Or we want to continue renaming in the middle of map art - just open the previous shulker!
    // it was isFirstMap, I decided to explain what does it use for
    // You have to rename the first map in the shulk to something else if you do not need to update the sequence (if its match)
    public boolean carryOverSequence = true;
    private String mapName = "StalpoIsAwesome! ({x} {y})";
    private int mapX = 1;
    private int mapY = 1;
    private int currX = 0;
    private int currY = 0;
    private boolean incrementY = true; // doesn't matter is isMultiDimensional = false
    private boolean isMultiDimensional = true; // if false, use "StalpoIsAwesome! ({current}/{total})" starting with 1
    private Pattern namePattern = null;
    private int matchedX = 0;
    private int matchedY = 0;


    public NameSequence(String mapName, int mapX, int mapY, boolean incrementY, boolean isMultiDimensional) {
        this.mapName = mapName;
        this.mapX = mapX;
        this.mapY = mapY;
        this.incrementY = incrementY;
        this.isMultiDimensional = isMultiDimensional;

        if (isMultiDimensional) {
            String regex = Pattern.quote(mapName)
                    .replace("{x}", "\\E(?<x>\\d+)\\Q")
                    .replace("{y}", "\\E(?<y>\\d+)\\Q");
            this.namePattern = Pattern.compile("^" + regex + "$", Pattern.UNICODE_CASE);
        } else {
            this.currX = 1;
            String regex = Pattern.quote(mapName)
                    .replace("{cur}", "\\E(?<x>\\d+)\\Q")
                    .replace("{total}", "\\E(?<y>\\d+)\\Q");
            this.namePattern = Pattern.compile("^" + regex + "$", Pattern.UNICODE_CASE);
        }
    }

    public NameSequence() {
        // this constructor is only used when starting the mod
        String regex = Pattern.quote(mapName)
                .replace("{x}", "\\E(?<x>\\d+)\\Q")
                .replace("{y}", "\\E(?<y>\\d+)\\Q");
        this.namePattern = Pattern.compile("^" + regex + "$", Pattern.UNICODE_CASE);
    }

    ;

    public int getCurrX() {
        return currX;
    }

    public int getCurrY() {
        return currY;
    }

    public Pattern getNamePattern() {
        return namePattern;
    }

    public void increment() {
        // uuhhh basically it is infinity and won't stop if both maximum x and y were reached
        if (isMultiDimensional) {
            // [x, y] -> mapX or mapY
            if (incrementY) {
                currY++;
                if (currY == mapY) {
                    currY = 0;
                    currX++;
                }
            } else {
                currX++;
                if (currX == mapX) {
                    currX = 0;
                    currY++;
                }
            }
        } else {
            // [x] -> total
            currX++;
        }
    }

    public void decrement() {
        if (currX == 0 && currY == 0) return;
        if (!isMultiDimensional && currX == 1) return;

        if (isMultiDimensional) {
            // [x, y] -> mapX or mapY
            if (incrementY) {
                if (currY == 0) {
                    currY = mapY - 1;
                    currX--;
                }
            } else {
                if (currX == 0) {
                    currX = mapX - 1;
                    currY--;
                }
            }
        } else {
            // [x] -> total
            currX--;
        }
    }

    public boolean isNameMatches(String text) {
        Matcher checkName = namePattern.matcher(text);

        if (checkName.matches()) {
            matchedX = Integer.parseInt(checkName.group("x"));
            matchedY = Integer.parseInt(checkName.group("y"));
        }

        return checkName.matches();
    }

    public boolean isFollowingSequence(String text) {
        if (isNameMatches(text)) {
            if (isMultiDimensional) {
                if (carryOverSequence) {
                    currX = matchedX;
                    currY = matchedY;
                    carryOverSequence = false; // because we found the first map in the shulker that matches the sequence
                }
                return (currX == matchedX && currY == matchedY);
            } else {
                if (matchedX == 0) return false; // does not allow starting from 0
                if (carryOverSequence) {
                    currX = matchedX;
                }
                return currX == matchedX;
            }
        }

        return false;
    }

    public String getCurrentMapName() {
        if (isMultiDimensional) {
            return mapName.replace("{x}", Integer.toString(currX)).replace("{y}", Integer.toString(currY));
        } else {
            return mapName.replace("{cur}", Integer.toString(currX)).replace("{total}", Integer.toString(mapX));
        }
    }

    public boolean reachedEnd() {
        if (isMultiDimensional)
            return (currX >= mapX || currY >= mapY); // can't be [20, 20] if maximum is [20, 20] because starts with 0
        else return currX > mapX; // can be 20/20 etc
    }
}
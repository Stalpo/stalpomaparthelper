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
    private int mapX = 100;
    private int mapY = 9;
    private int currX = 0;
    private int currY = 0;
    private boolean incrementY = true; // doesn't matter is isMultiDimensional = false
    // if false, use "StalpoIsAwesome! ({current}/{total})" starting with 1
    // change it to enum or something if you gonna add another map names pattern
    private boolean isMultiDimensional = true;
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
                    .replace("{cur}", "\\E(?<cur>\\d+)\\Q")
                    .replace("{total}", "\\E(?<total>\\d+)\\Q");
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

    public int getCurrX() {
        return currX;
    }

    public int getCurrY() {
        return currY;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public Pattern getNamePattern() {
        return namePattern;
    }

    public void setCurrX(int value) {
        currX = value;
    }

    public void setCurrY(int value) {
        currY = value;
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
            if (isMultiDimensional) {
                matchedX = Integer.parseInt(checkName.group("x"));
                matchedY = Integer.parseInt(checkName.group("y"));
            } else {
                matchedX = Integer.parseInt(checkName.group("cur"));
            }
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
                    carryOverSequence = false;
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

    public int getSequenceSerialNumber(boolean useCurrent) {
        // 0 means the first slot in the shulker
        // therefore serial number starts with 0
        int thisX = useCurrent ? currX : matchedX;
        int thisY = useCurrent ? currY : matchedY;

        if (!isMultiDimensional) return thisX - 1;

            // [0, 0] = 0
            // [0, 1] = 1
            // [0, 2] = 2
            // [1, 0] = 3
            // [1, 1] = 4 and so on
        else {
            if (incrementY) return thisX * mapY + thisY;
            else return thisY * mapX + thisX;
        }
        // divide the result by 27 and you will have its slot in the shulker
    }

    public int[] getDimensionsBySerialNumber(int serial) {
        if (!isMultiDimensional) return new int[]{serial + 1};

            // 0 = [0, 0]
            // 1 = [0, 1]
            // 2 = [0, 2]
            // 3 = [1, 0] and so on
        else {
            if (incrementY) return new int[]{serial / mapY, serial % mapY};
            else return new int[]{serial / mapX, serial % mapX};
        }
    }

    public int getMaxSerialNumber() {
        if (isMultiDimensional) return mapX * mapY - 1;
        else return mapX - 1;
    }
}
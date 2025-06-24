# stalpomaparthelper
A client side mapart utility mod designed for 2b2t mapart enthusiasts!

# About
This mod was made because I am too lazy to manually copy 2000+ (probably a lot more since writing this) unique maps more than once. Some of the features include: map copier, auto locker, map downloader, and a duplicate checker.

# Contributing
Feel free to make pull requests with whatever new features or bug fixes may be needed and I will make sure to check them out!

# Feature list
1. Map Downloader
2. Map Copier
3. Map Locker
4. Map Namer
5. Duplicate Finder

### <b> DISABLE ExtraCraft, AutoCraft, QuickCraft and other modules that affect crafting or anvil before using the mod! </b>

# Commands
1. /nameMap x y name

used for the map namer

2. /setMaxWrongPixels max

sets the amount of incorrect pixels a map can have compared to a map from duplicate checker (defaults to 3 every time you open the game)

3. /clearDownloadedMaps

clears the .minecraft/maparts folder

4. /delay value

sets the new delay value (in milliseconds) between each inventory action 

# Applies to everything below
Make sure that your 3 inventory rows are empty for most of the features to work properly.

Make sure that you have all the maps loaded client side. You can do this by turning on auto copy (numpad 5) and opening each shulker twice to take the maps out and back in (don't forget to turn copier back off after). If your maps are or were hanging in render distance this step is not necessary.

# How to download maps
1. Have a shulker full of maps you want to download
2. Open the shulker and press numpad 1
3. You can find your maps downloaded at .minecraft/maparts

# How to auto copy maps
1. Have a shulker full of maps that you want to copy, an empty shulker to put the copied maps in, a crafting table, and empty maps in your hotbar
2. Toggle auto copy with numpad 5
3. Open the shulker to take out your maps
4. Open the crafting table to copy the maps
5. Open the original shulker to put the original set back
6. Open the other shulker to put the copied set in
7. Turn auto copy back off with numpad 5

# How to auto lock maps
1. Have a shulker full of maps that you want to lock, a cartography table, and glass panes in your hotbar
2. Toggle auto locker with numpad 6
3. Open the shulker and press numpad 3 to take all non-locked maps
4. Open a cartography table to auto lock all the maps
5. Open the original shulker to put the newly locked maps back
6. Turn auto locker back off with numpad 6

# How to auto name maparts
1. Have shulker(s) full of maps of your complete mapart IN ORDER GOING RIGHT THEN DOWN, an anvil, and enough xp / xp bottles to name the maps
2. Toggle auto namer with numpad 7
3. Run the /nameMap command with your maps dimensions and name <br>
   `/nameMap x y incrementY name` <br>
   example: `/nameMap 25 35 true [{x}, {y}] The True Kings`
4. Open the next shulker in order to take out the maps
5. Make sure you have at least the same amount of levels as maps from shulker
6. Open an anvil to auto name the maps
7. Open the shulker again to put the maps back
8. Repeat steps 4-7 until you have named all the maps
9. Turn auto namer back off with numpad 7

<br>**Important note:** <br>
   If you want to continue renaming process somewhere in the middle of your mapart,
   take the previous renamed maps and open an anvil or just open a shulker with them. You will be notified in chat "previous indexes have been updated".
   Put the maps back and take next maps, they will be renamed according to the sequence! <br>
<br>**Increment Y:** <br>
   `true` -> sequence y+1: `[0, 0], [0, 1], [0, 2], [0, 3], ...`<br>
   `false` -> sequence x+1: `[0, 0], [1, 0], [2, 0], [3, 0], ...`


# How to check for duplicates
1. Have a shulker full of maps you want to check and all the maps you want to check against already downloaded in .minecraft/maparts
2. Open the shulker and press numpad 2
3. Now all the duplicates are moved to your inventory

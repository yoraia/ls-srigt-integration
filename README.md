# Livesplit SpeedrunIGT Integration


Make a txt file called `splits.txt` and place it in the `.minecraft/config` folder.  
The 'name' for each split in the file doesnt matter.  
Its possible to do item only, just remove any coordinates and only have the split name / item. Names for items are typed as you see them when you hover over them in the inventory.
The final split should happen automatically once the run is complete

### coordinate range format

```txt
split name
item name (optional)
x1, y1, z1
x2, y2, z2
```

### coordinate radius format

```txt
split name
item name (optional)
x, y, z
radius
```

### examples

```txt
Start
593, 51, 387
3
```

```txt
Halls of Chaos
Purple Wool
55, 106, -547
15
```
```
split5
Emerald 53
22, 17, -1693
21, 17, -1695
```

Full Example File (Goliath 100% Splits): https://pastebin.com/Zr4rc4w5



## setup

Optional: This custom build of LiveSplit is recommended if you want a transparent background without color keying issues.  
Set the capture method for your LiveSplit source in OBS to Windows 10 (1903 and up) for it to be transparent in OBS
* [https://www.reddit.com/r/speedrun/comments/1k8i3i7/release_transparent_livesplit/](https://www.reddit.com/r/speedrun/comments/1k8i3i7/release_transparent_livesplit/)

Set timing method to game time inside livesplit, otherwise the timer will use real time (Bad)  
Right click -> Compare Against -> Game time  

When you're ready to start using the mod, start the livesplit server.  
Right click -> Control -> Start tcp server  
The port is `16834` by default, don't change it or else it probably won't work  

once that's done, go to the srigt menu ingame. scroll to the bottom of the general tab and click on `Connect to Livesplit`. if you want to edit your split conditions, edit them in the splits.txt file and then reload your splits which is found in the same menu. once you load into a new world and the timer starts, you should hopefully see the timer running alongside srigt. when you reset, the timer will keep going until start another run (or reset it yourself)  

## external components i like to have displayed (optional)

**Theory comparison generator**  
https://github.com/fmichea/LiveSplit.TheoryComparisonGenerator  
allows you to generate “Theory PB” splits, where your timesave is distributed evenly across all splits based on your sum of best segments. This gives a more accurate representation of how good a run is as opposed to simply comparing to your pb

For example, you might have had a run where you played close to your sum of best in the late game but had a terrible early game. Even if you were 5 seconds ahead of your pb heading into the late game, a pb may still be unrealistic unless you played **extremely** well and therefore a more balanced comparison would be better to use as you have a better reference on what split times you should be getting to be able to PB  

**Best Pace Ever**  
https://beckabney.com/mk64/bestpace.html  
shows how good your run is compared to the best run you’ve ever to that split


# issues

The console/logs will have outputs for split names, conditions, coordinates, and a list of items in your inventory (when you reload splits inside of a world). This should help debug any issues you have if the autosplit isn’t working.  
If somehow there’s a case where an item isn’t read properly for splits, Items will have their “raw” name displayed next to them in the console/logs (for example, the raw name of an Emerald with green text would be `§aEmerald`, i've tried to remove these cases so you can just write `Emerald` but let me know if you come across something that doesn't work).  

Furthermore, splits work sequentially in the splits.txt file, meaning that if you want to test if a split works then you’ll have to set it as the first split in the file then reload splits, then place it in the correct spot when you’re done.  
(This also means that skipping splits (livesplit feature) is not recommended as the conditions may be mismatched to the split you’re on, but you shouldn’t really have to do this anyway.)


# potential to do list

Port to other versions  
Load different split files  
Properly read current split  
Ingame menu or way to add split conditions  
Decathlon support ??  
splits built into the mod itself/srigt !! 

# contact
if you have any issues or suggestions, my discord is `hystericnightgirl` 
or you can join the ctm speedrunning guild (the community which this mod was made for) and ask me there  
https://discord.gg/QN4wQTat4J

 this is the first time i've ever released something publically or even tried to mod something so sorry if things are wrong on the github/building side of things

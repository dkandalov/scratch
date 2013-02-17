<img src="https://github.com/dkandalov/scratch/blob/proper-rewrite/screenshot.png?raw=true" alt="screenshot" title="screenshot" align="right" width="480"/>

What is this?
=============

This is a plugin for IntelliJ IDEA (and other IntelliJ IDEs).
It quickly opens temporary editor tab.
<br/>
Can be useful if you want to type something quickly without having to create new file or switch to another editor.

You can install it from IntelliJ plugin manager. It should be compatible with IntelliJ 11 upwards.
(Just in case [this is plugin page](http://plugins.jetbrains.com/plugin/?null&pluginId=4428) in plugin repository.)


How to use?
===========
 - Alt+C, Alt+C - open default scratch
 (it can be 'last opened' or 'topmost in scratches list'; see "Tools -> Scratch -> Default Scratch")
 - Alt+C, Alt+S - open list with all scratches
 - Alt+C, Alt+A - add new scratch

In scratches list popup:
 - Alt+Up/Down - move scratch
 - Delete* - delete scratch
 - Shift+F6* - rename scratch
 - Alt+Insert* - add new scratch

(* - shortcuts are copied from "Delete", "Rename" and "Generate" actions; i.e.
if you have changed keyboard layout, your own shortcuts should work as well)

Some of these actions are also in "Tools -> Scratch" menu.


"Hidden" features
=================
 - **Listen to clipboard and add its content to default scratch**. <br/>
 In "IDE Settings -> Keymap" search for "Listen to clipboard" and assign it a shortcut (Alt+C, Alt+V is recommended :)).
 (Note that you can do similar thing with built-in IntelliJ clipboard.
 E.g. Ctrl+C several words, Ctrl+Shift+V to see clipboard history, select several items.
 If you press Ctrl+C, selected items will be joined with new lines.)

 - **Listen to clipboard append/prepend** (default is "APPEND").<br/>
 Find folder with IntelliJ preference files ("$HOME/.IntelliJ12/options" on Windows/Linux; "$HOME/Library/Preferences/IntelliJIdea12/options" on OSX).<br/>
 Edit "scratch_config.xml" to add the following line (works after IntelliJ restart):
```
<option name="clipboardAppendType" value="PREPEND"/>
```

 - **Append/prepend new scratch to scratches list** (default is "APPEND").
 Edit "scratch_config.xml" to add the following line (works after IntelliJ restart):
```
<option name="newScratchAppendType" value="PREPEND"/>
```

 - **Location of scratch folder**.<br/>
 Primary motivation for this was that IntelliJ data like caches and preferences can be a part of roaming profile by default (at least on Windows).
 In other words all your caches, preferences and scratches might end up on another computer.
 If it bothers you, it's probably better to reconfigure IntelliJ to store files somewhere else.
 You can also reconfigure just scratches folder. Edit "scratch_config.xml" to add the following line (works after IntelliJ restart):
```
<option name="scratchesFolderPath" value="/safe/place/to/store/my/stuff/"/>
```


Thanks to [Vojtěch Krása](https://github.com/krasa) for insisting on new features, making me update this plugin and testing it.
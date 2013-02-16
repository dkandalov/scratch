<img src="https://raw.github.com/dkandalov/scratch/master/screenshot.png" alt="screenshot" title="screenshot" align="right"/>

What is this?
=============

This is a plugin for IntelliJ IDEA (and other IntelliJ IDEs).
It quickly opens temporary editor tab.
<br/>
Can be useful if you want to type something quickly without having to create new file or switch to another editor.

You can install it from IntelliJ plugin manager. Should be compatible with IntelliJ 11 upwards.
(Just in case [this is plugin page](http://plugins.jetbrains.com/plugin/?null&pluginId=4428) in plugin repository.)


How to use?
===========
 - Alt+C, Alt+C - open default scratch
 - Alt+C, Alt+S - open list of all scratches
 - Alt+C, Alt+A - add new scratch

In scratches list popup:
 - Alt+Up/Down - move scratches
 - Delete* - delete scratch
 - Shift+F6* - rename scratch
 - Alt+Insert* - add new scratch

(* - shortcuts are copied from "Delete", "Rename" and "Generate" actions; i.e.
if you have changed keyboard layout, your own shortcuts should work as well.)


"Hidden" features
=================
 - **Listen to clipboard** and add its content to default scratch. <br/>
 In "IDE Settings -> Keymap" search for "Listen to clipboard" and assign it a shortcut (Alt+C, Alt+V is recommended :)).

 - **Listen to clipboard** append/prepend (default is "APPEND").<br/>
 Find folder with IntelliJ preference files ("$HOME/.IntelliJ12/options" on Windows/Linux; "$HOME/Library/Preferences/IntelliJIdea12/options" on OSX).<br/>
 Edit "scratch_config.xml" to add the following line:
```
<option name="clipboardAppendType" value="PREPEND"/>
```
 Restart IntelliJ.

 - **Append/prepend new scratch to scratches list** (default is "APPEND").
 Edit "scratch_config.xml" to add the following line:
```
<option name="newScratchAppendType" value="PREPEND"/>
```
  Restart IntelliJ.

 - **Location of scratch folder**.<br/>
 Primary motivation for this is that IntelliJ data like caches and preferences can be a part of roaming profile by default (at least on Windows).
 In other words your data and scratches might end up on other computer.
 Edit "scratch_config.xml" to add the following line:
```
<option name="scratchesFolderPath" value="/safe/place/to/store/my/stuff/"/>
```
 Restart IntelliJ.


Thanks to Vojtech Krasa for insisting on new features and making me update this plugin.
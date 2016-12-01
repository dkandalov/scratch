<img src="https://github.com/dkandalov/scratch/blob/proper-rewrite/screenshot.png?raw=true" alt="screenshot" title="screenshot" align="right" width="480"/>

### Scratch Plugin

This is a plugin for IntelliJ IDEs for cases when you want to type something without having to create new file or switch to another editor.

It allows you to have multiple temporary editor tabs, one of which can be made the default.
The default scratch tab can be quickly opened with a shortcut.  

Contents of the temporary editor tabs are stored outside of the project, so your directory will not get cluttered.

Note that since IJ14 there are [built-in scratches](https://blog.jetbrains.com/idea/2014/09/intellij-idea-14-eap-138-2210-brings-scratch-files-and-better-mercurial-integration/)
with similar functionality which unfortunately still don't have actions to open "default" scratch and show list of available scratches.
At some point this plugin might be reimplemented as additional actions on top of built-in scratches.


### How to use?
 - `Alt+C, Alt+C` - open default scratch
 (it can be 'last opened' or 'topmost in scratches list'; see `Tools -> Scratch -> Default Scratch`)
 - `Alt+C, Alt+S` - open list with all scratches
 - `Alt+C, Alt+A` - add new scratch

In scratches list popup:
 - `Alt+Insert`* - add new scratch
 - `Alt+Up/Down` - move scratch
 - `Shift+F6`* - rename scratch
 - `Delete`* - delete scratch
 - `Ctrl+Delete` - delete scratch without prompt

\* - shortcuts are copied from `Delete`, `Rename` and `Generate` actions, 
i.e. if you have changed keyboard layout, your own shortcuts should work as well.

Some of these actions are also in `Tools -> Scratch` menu.


### "Hidden" features
 - **Listen to clipboard and add its content to default scratch**. <br/>
 In `IDE Settings -> Keymap` search for "Listen to clipboard" and assign it a shortcut (`Alt+C, Alt+V` is recommended :)).
 (Note that you can do similar thing with built-in IDE clipboard.
 E.g. `Ctrl+C` several words, `Ctrl+Shift+V` to see clipboard history, select several items.
 If you press `Ctrl+C`, selected items will be joined with new lines.)

 - **Listen to clipboard append/prepend** (default is "APPEND").<br/>
 Find folder with IDE preference files (e.g. `$HOME/.IntelliJ/options` on Windows/Linux; `$HOME/Library/Preferences/IntelliJIdea/options` on OSX).<br/>
 Edit `scratch_config.xml` to add the following line (works after IDE restart):
```
<option name="clipboardAppendType" value="PREPEND"/>
```

 - **Append/prepend new scratch to scratches list** (default is "APPEND").
 Edit `scratch_config.xml` to add the following line (works after IDE restart):
```
<option name="newScratchAppendType" value="PREPEND"/>
```

 - **Location of scratch folder**.<br/>
 Primary motivation for this was that IDE data like caches and preferences can be a part of roaming profile by default (at least on Windows).
 In other words all your caches, preferences and scratches might end up on another computer.
 If it bothers you, it's probably better to reconfigure IDE to store files somewhere else.
 You can also reconfigure just scratches folder. Edit `scratch_config.xml` to add the following line (works after IDE restart):
```
<option name="scratchesFolderPath" value="/safe/place/to/store/my/stuff/"/>
```


Thanks to [Vojtěch Krása](https://github.com/krasa) for making version 1.0 of this plugin happen and testing it.

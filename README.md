
# the-great-chicken / PluginGUI

Minecraft Plugin that should allow to create complex GUIs with extended variables generation

# Creating an action

```
%define %action home_action
| %type ROUTING
| %path /
```

# Creating an item

```
%define %item simple_item
| %type DIRT
| %count 1
| %onclick $home_action
```

# Creating a GUI

```
%define simple_gui
| %route /
| %shape 27
| %fill 1:1 1:9 GLASS_PANE
| %fill 3:1 3:9 GLASS_PANE
| %setitem 2:5 $simple_item
```

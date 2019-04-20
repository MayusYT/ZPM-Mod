# ZPM Mod
[![Build Status](https://travis-ci.org/MayusYT/ZPM-Mod.png)](https://travis-ci.org/MayusYT/ZPM-Mod)


ZPM-Mod is a power generating Mod. You can find (and craft) Zero Point Modules and extract Energy from them. Huge amounts of energy!

### Feature list
* TheOneProbe Integration
* OpenComputers Integration
* Automation through pipes and redstone modes

### OpenComputers Integration
You can query the energy amount left in a ZPM.
To do that, place an OpenComputers Adapter next to a ZPM Controller.
Then, you can execute the following code on the computer that is connected to the Adapter.

Note: You can get the name of the component (it's "controllerlarge" below) if you execute the command `components` on the computer.

Example for the large controller:
```lua
local component = require("component")

local controller = component.controllerlarge

print(controller.getZpmEnergy()[1]) -- [1] resembles the slot
```

Example for the small controller:
```lua
local component = require("component")

local controller = component.controllerlarge

print(controller.getZpmEnergy()) -- No index needed as this returns just an int, no table.
```

More information coming soon!

My Website: https://mayus.me

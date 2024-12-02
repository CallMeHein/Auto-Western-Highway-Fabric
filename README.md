# Auto Western Highway (Fabric)

A mod to automatically expand the western overworld highway (-X) on 2b2t

## Disclaimer

This is still a WIP and should not be used without supervision. Baritone likes to get stuck and the script does not
always take the cleanest path. Manual interference is sometimes needed.

## Prerequisites

Baritone

## Usage

Put the schematics from ./schematics into your baritone's schematic folder (default: `.minecraft/schematics`)

### Commands

- `/autoWesternHighway`: starts the script
- `/stopAutoWesternHighway`: stops the script, interrupting everything immediately
- `/toggleAWHFuturePath`: toggles rendering of the path that will be taken
- `/toggleAWHStatusDisplay`: toggles the status message at the top left of the screen
- `/toggleAWHNightLogout`: toggles the logout during the in-game night
    - set your auto-reconnect to 500 seconds

### In-Game

- stand on the end of the highway
- have all required items in your inventory
    - Stone Bricks
    - Stone Brick Slabs
    - Smooth Stone
    - Smooth Stone Slabs
    - (Optional) Shulkers containing any of the above

### Tips

- **set Baritone's allowInventory = false**, it messes with your tools and hijacks your last hotbar slot, so the script
  won't work
- disable Auto Replenish, it might mess with reserved slots for building materials
- set up a very strict Auto Drop list, we don't want to pickup most things to keep our inventory empty enough to
  replenish from
  shulkers (flowers, grass, leaves, logs, ...)

## Troubleshooting

### Gets stuck while placing blocks

- `blockReachDistance` may be too low

## Gets stuck while breaking blocks

- `blockBreakSpeed` may be too low
- external AutoTool is enabled, but Baritone's `autoTool` is also enabled
    - also make sure to enable `assumeExternalAutoTool`

## TODO

- a cleaner way to break shulkers, right now we just hope the server accepts any of our packets
- any way to pick up the shulker item after breaking it if it somehow falls out of instant pickup range?
- prevent it from closing chat/inventory/menu/map when it's just building (?)
- scaffolding sometimes gets stuck, fix that
- let the player queue certain commands at a certain x-coordinate
    - something like /forceAutoWesternHighway {x} STEP_UP 10
    - this lets us scout ahead with freecam to catch/prevent known issues with the current pathing system like crashing
      into a cave/

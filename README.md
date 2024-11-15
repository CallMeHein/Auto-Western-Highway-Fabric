# Auto Western Highway (Fabric)

## Usage

Put the schematics from ./schematics into your baritone's schematic folder (default: `.minecraft/schematics`)

## Things to consider

- **set Baritone's allowInventory = false**, it messes with your tools and hijacks your last hotbar slot, so the script
  won't work
- disable Auto Replenish, might mess with reserved slots for building materials
- a very strict Auto Drop list, we don't want to pickup most things to keep our inventory empty enough to replenish from
  shulkers (flowers, grass, leaves, logs, ...)

## TODO

- the readme lol
- multiple status messages at once, all toggleable
    - more status messages than just the current build target
- a cleaner way to break shulkers, right now we just hope the server accepts any of our packets
- any way to pick up the shulker item after breaking it if it somehow falls out of instant pickup range?
- prevent it from closing chat/inventory/menu/map when it's just building (?)
- log out just before mobs can spawn, log in just after mobs can't spawn
- scaffolding sometimes gets stuck, fix that
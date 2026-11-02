# TACZ Third Person Animations

A NeoForge 1.21.1 addon for [TACZ](https://modrinth.com/mod/timeless-and-classics-zero) that plays TACZ's first-person gun animations (reload, inspect, shoot, bolt, draw…) on the third-person gun model, both for yourself in F5 and for every other player. It also syncs hotbars so TACZ's sling display is visible to everyone.

## Install

- **Server:** required for other players' animations and slings to be relayed.
- **Client:** required to *see* the animations. Players without the mod can still join a server that has it; they just won't see the animations.

Requires NeoForge 21.1+, Minecraft 1.21.1 and TACZ 1.1.8+.

## Building

1. Put the TACZ jar (e.g. `tacz-neoforge-1.21.1-1.1.8-hotfix-r7.jar`) in `libs/`. It's only needed to compile and isn't bundled.
2. Run `gradle build` (Gradle 8.8+, Java 21). The jar ends up in `build/libs/`.

## Changelog

### 1.0.1
- Fixed the server failing to place a player in the world (`Payload tacz_tp_anim:sling_inventory may not be sent to the client!`) when a player without the mod, or with a mismatched build, joined. All packet sends now go through `SafeSend` / `ClientSafeSend`, which skip connections that didn't negotiate the mod's channels.

## License

MIT

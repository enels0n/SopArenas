# SopArenas

Shared arena and match engine for Sop mini-games.

It owns match lifecycle, participants, player state restoration, named arena points and regions,
temporary worlds, cleanup and game-mode registration. Mini-game plugins implement `GameMode`
and keep their own rules, scoring and content.

## Integration

Add `SopArenas` as a provided Maven dependency and as a hard dependency in `plugin.yml`, then register a mode:

```java
SopArenasApi.engine().registerGameMode(new MyGameMode());
```

Start an isolated match with `MatchRequest`. Always unregister the mode when the mini-game disables.

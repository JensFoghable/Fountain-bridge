# Bridge Session Routing (V2 Instance/Dimension API)

This document describes how the Fountain bridge routes Bedrock player sessions to the
correct instance (dimension) on the Fabric server, using the **ConnectorPlugin v2 API**.

## Motivation

In the v1 architecture the bridge created a 1:1 mapping between a Bedrock session and a
separate TCP connection to a single backend. Each player was placed into the Overworld
(dimension 0) unconditionally.

V2 introduces **instances** -- game sessions (buildplates, adventures, encounters) that
can run as separate dimensions in a single persistent Fabric server process. The bridge
no longer spawns backends; instead it routes players to the correct dimension so the
Fabric server can handle multiple instances concurrently.

## Architecture overview

```
Bedrock clients ──UDP 19132──> [Fountain Bridge]
                                    │
                        ┌───────────┼───────────┐
                        │           │           │
                   player A    player B    player C
                        │           │           │
                   instanceX   instanceY   instanceX
                  (dimId=2)   (dimId=3)   (dimId=2)
                                    │
                      TCP (mcprotocollib) ──> [Fabric server]
                                              dimension 2
                                              dimension 3
```

- One bridge process, one UDP port (19132).
- One TCP connection per player to the same Fabric server.
- The Fabric server runs multiple dimensions simultaneously.
- Dimension assignment happens **before** the `StartGamePacket` is sent to the Bedrock
  client, so the Bedrock client's first join targets the correct dimension.

## Lifecycle of a player session

1. Bedrock client sends `LoginPacket` to the bridge.
2. Bridge extracts the player UUID from the JWT (via `LoginUtils`).
3. v1 backward compatibility: `connectorPlugin.onPlayerConnected()` is called.
4. Bridge determines the target `instanceId`:
   - If a **pre-configured player UUID** mapping exists (from JSON config or
     orchestrator pre-bind call), that instance is used.
   - Otherwise the player is routed to the `"default"` shared instance.
5. Bridge calls `connectorPlugin.bindPlayerToInstance(playerId, instanceId)`, which
   returns a `CompletableFuture<BindPlayerResult>` containing the **numeric
   `dimensionId`**.
6. Bridge blocks on the future (5-second timeout; the plugin runs in-process so this
   should complete near-instantly).
7. `PlayerSession.setAssignedDimension(dimensionKey, dimensionId)` stores the routing
   info on the session.
8. The `StartGamePacket` is sent with `dimensionId` set to the assigned value (instead
   of the old hardcoded `0`).
9. The player joins the correct dimension on the Fabric server.

## Configuration: dimension mappings JSON file

Use the `--dimensionMappingsFile <file>.json` CLI argument to pre-register instance
mappings and player bindings before any clients connect.

### JSON format

```json
{
  "instances": {
    "buildplate_abc123": {
      "dimensionId": 2,
      "dimensionKey": "fountain:buildplate_abc123"
    },
    "adventure_xyz": {
      "dimensionId": 3,
      "dimensionKey": "fountain:adventure_xyz"
    }
  },
  "playerBindings": {
    "00000000-0000-0000-0000-000000000001": "buildplate_abc123",
    "00000000-0000-0000-0000-000000000002": "adventure_xyz"
  }
}
```

- **`instances`**: Maps `instanceId` to `{dimensionId, dimensionKey}`.
  - `dimensionId`: numeric ID used in the Java protocol (0=Overworld, 1=Nether,
    -1=End; custom dimensions start at 2+).
  - `dimensionKey`: `ResourceLocation` string (`namespace:path`) identifying the
    dimension on the Fabric server.
- **`playerBindings`**: Maps player UUIDs to instance IDs. When a bound player
  connects, they are routed to the specified instance automatically.

## API reference

### SessionsManager (V2 additions)

| Method | Description |
|--------|-------------|
| `updateInstanceDimensionMapping(instanceId, dimensionKey, dimensionId)` | Register an instance mapping. The dimension ID is not stored here -- it is sourced from the connector plugin's `bindPlayerToInstance` result (see "Error handling"). |
| `preBindPlayerToInstance(playerUuid, instanceId)` | Pre-bind a player UUID to an instance. |
| `getDimensionKeyForInstance(instanceId)` | Look up the `ResourceLocation` dimension key. |
| `getInstanceIdForPlayer(playerUuid)` | Look up the bound instance for a player UUID. |

### PlayerSession (V2 additions)

| Method | Description |
|--------|-------------|
| `setAssignedDimension(dimensionKey, dimensionId)` | Assign this session to a specific dimension. Must be called before `StartGamePacket` is sent. |
| `getInstanceId()` | Returns the instance ID this session is assigned to. |
| `getAssignedDimensionId()` | Returns the numeric dimension ID this session is assigned to. |

### DefaultConnectorPlugin (V2 additions)

| Method | Description |
|--------|-------------|
| `registerInstanceMapping(instanceId, dimensionId, dimensionKey)` | Register an instance mapping for the default plugin. |
| `removeInstanceMapping(instanceId)` | Remove an instance mapping. |
| `preBindPlayer(playerId, instanceId)` | Pre-bind a player to an instance. |

## Error handling

- If `bindPlayerToInstance` returns `BindResult.success == false`, the player is
  disconnected with a log warning.
- If the future times out (5 seconds), the player is disconnected.
- If the connector plugin throws `ConnectorPluginException`, the player is
  disconnected.
- If the connector plugin returns `BindResult.failure(...)` for the target
  instance, the player is disconnected. The built-in `DefaultConnectorPlugin`
  never fails here: when no instance mapping is found for the target instance it
  falls back to dimension 0 (Overworld) instead.
- If no pre-binding exists and no join code is provided, the player is routed to
  `"default"` instance, which falls back to dimension 0 (Overworld).

## Compatibility

- **v1 path preserved**: `onPlayerConnected()` is still called during login for
  backward compatibility with existing connector plugins. Plugins that only implement
  v1 methods continue to work unchanged.
- **DefaultConnectorPlugin**: The built-in connector plugin now implements all v2
  methods and supports pre-configuration via `registerInstanceMapping()` and
  `preBindPlayer()`.

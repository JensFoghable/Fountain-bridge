package micheal65536.fountain.connector;

import org.jetbrains.annotations.NotNull;

import micheal65536.fountain.connector.plugin.BindPlayerResult;
import micheal65536.fountain.connector.plugin.ConnectorPlugin;
import micheal65536.fountain.connector.plugin.CreateInstanceRequest;
import micheal65536.fountain.connector.plugin.CreateInstanceResult;
import micheal65536.fountain.connector.plugin.DestroyInstanceResult;
import micheal65536.fountain.connector.plugin.DisconnectResponse;
import micheal65536.fountain.connector.plugin.Inventory;
import micheal65536.fountain.connector.plugin.Logger;
import micheal65536.fountain.connector.plugin.PlayerLoginInfo;
import micheal65536.fountain.connector.plugin.ResourceLocation;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public final class DefaultConnectorPlugin implements ConnectorPlugin
{
	private final HashMap<String, GenoaInventory> playerInventories = new HashMap<>();
	private final HashMap<String, InstanceData> instances = new HashMap<>();
	private final HashMap<String, String> playerBindings = new HashMap<>();

	private static class InstanceData
	{
		final int dimensionId;
		final ResourceLocation dimensionKey;

		InstanceData(int dimensionId, ResourceLocation dimensionKey)
		{
			this.dimensionId = dimensionId;
			this.dimensionKey = dimensionKey;
		}
	}

	@Override
	public void init(@NotNull String arg, @NotNull Logger logger) throws ConnectorPluginException
	{
		// empty
	}

	@Override
	public void shutdown() throws ConnectorPluginException
	{
		// empty
	}

	@Override
	public boolean onPlayerDead(@NotNull String playerId) throws ConnectorPluginException
	{
		return true;
	}

	@Override
	public boolean onPlayerConnected(@NotNull PlayerLoginInfo playerLoginInfo) throws ConnectorPluginException
	{
		this.playerInventories.put(playerLoginInfo.uuid, new GenoaInventory(new Inventory(new Inventory.StackableItem[0], new Inventory.NonStackableItem[0], new Inventory.HotbarItem[7])));
		return true;
	}

	@Override
	@NotNull
	public DisconnectResponse onPlayerDisconnected(@NotNull String playerId, float health) throws ConnectorPluginException
	{
		this.playerInventories.remove(playerId);
		return new DisconnectResponse();
	}

	@Override
	@NotNull
	public Inventory onPlayerGetInventory(@NotNull String playerId) throws ConnectorPluginException
	{
		return this.getInventoryForPlayer(playerId).toConnectorPluginInventory();
	}

	@Override
	public void onPlayerInventoryAddItem(@NotNull String playerId, @NotNull String itemId, int count) throws ConnectorPluginException
	{
		this.getInventoryForPlayer(playerId).addItem(itemId, count);
	}

	@Override
	public void onPlayerInventoryAddItem(@NotNull String playerId, @NotNull String itemId, @NotNull String instanceId, int wear) throws ConnectorPluginException
	{
		this.getInventoryForPlayer(playerId).addItem(itemId, instanceId, wear);
	}

	@Override
	public int onPlayerInventoryRemoveItem(@NotNull String playerId, @NotNull String itemId, int count) throws ConnectorPluginException
	{
		return this.getInventoryForPlayer(playerId).takeItem(itemId, count);
	}

	@Override
	public boolean onPlayerInventoryRemoveItem(@NotNull String playerId, @NotNull String itemId, @NotNull String instanceId) throws ConnectorPluginException
	{
		return this.getInventoryForPlayer(playerId).takeItem(itemId, instanceId);
	}

	@Override
	public void onPlayerInventoryUpdateItemWear(@NotNull String playerId, @NotNull String itemId, @NotNull String instanceId, int wear) throws ConnectorPluginException
	{
		this.getInventoryForPlayer(playerId).updateItemWear(itemId, instanceId, wear);
	}

	@Override
	public void onPlayerInventorySetHotbar(@NotNull String playerId, Inventory.HotbarItem[] hotbar) throws ConnectorPluginException
	{
		this.getInventoryForPlayer(playerId).setHotbar(hotbar);
	}

	// --- V2 Instance / Dimension API ---

	/**
	 * Registers an instance-to-dimension mapping so that {@link #bindPlayerToInstance}
	 * can resolve dimension IDs. This is called by the bridge after loading
	 * the dimension mapping configuration file or via the public API.
	 */
	public void registerInstanceMapping(@NotNull String instanceId, int dimensionId, @NotNull ResourceLocation dimensionKey)
	{
		this.instances.put(instanceId, new InstanceData(dimensionId, dimensionKey));
	}

	/**
	 * Removes a registered instance mapping (called when an instance is destroyed).
	 */
	public void removeInstanceMapping(@NotNull String instanceId)
	{
		this.instances.remove(instanceId);
		this.playerBindings.entrySet().removeIf(entry -> entry.getValue().equals(instanceId));
	}

	/**
	 * Pre-binds a player to an instance before they connect.
	 * This is called by the bridge when it receives a player dimension mapping
	 * from the orchestrator (via configuration or connector plugin API).
	 */
	public void preBindPlayer(@NotNull String playerId, @NotNull String instanceId)
	{
		this.playerBindings.put(playerId, instanceId);
	}

	@Override
	@NotNull
	public CompletableFuture<CreateInstanceResult> createInstance(@NotNull CreateInstanceRequest request) throws ConnectorPluginException
	{
		// Default implementation: simple in-memory registration.
		// The orchestrator must provide the dimensionId separately (via configuration).
		// For now, auto-assign dimension IDs starting from 2 (0=overworld, 1=nether, -1=end).
		int dimensionId = 2 + this.instances.size();
		ResourceLocation dimensionKey = ResourceLocation.fromString("fountain:" + request.instanceId);
		this.instances.put(request.instanceId, new InstanceData(dimensionId, dimensionKey));
		return CompletableFuture.completedFuture(CreateInstanceResult.success(dimensionKey));
	}

	@Override
	@NotNull
	public CompletableFuture<DestroyInstanceResult> destroyInstance(@NotNull String instanceId) throws ConnectorPluginException
	{
		if (this.instances.remove(instanceId) != null)
		{
			// Remove any player bindings for this instance
			this.playerBindings.entrySet().removeIf(entry -> entry.getValue().equals(instanceId));
			return CompletableFuture.completedFuture(DestroyInstanceResult.success());
		}
		return CompletableFuture.completedFuture(DestroyInstanceResult.failure("Instance not found: " + instanceId));
	}

	@Override
	@NotNull
	public CompletableFuture<BindPlayerResult> bindPlayerToInstance(@NotNull String playerId, @NotNull String instanceId) throws ConnectorPluginException
	{
		// Look up the pre-configured binding for this player first
		String boundInstanceId = this.playerBindings.getOrDefault(playerId, instanceId);

		InstanceData instanceData = this.instances.getOrDefault(boundInstanceId, null);
		if (instanceData == null)
		{
			return CompletableFuture.completedFuture(BindPlayerResult.failure("Instance not found: " + boundInstanceId));
		}

		// Store the binding for future reference
		this.playerBindings.put(playerId, boundInstanceId);
		return CompletableFuture.completedFuture(BindPlayerResult.success(instanceData.dimensionId));
	}

	@NotNull
	private GenoaInventory getInventoryForPlayer(@NotNull String playerId) throws ConnectorPluginException
	{
		GenoaInventory inventory = this.playerInventories.getOrDefault(playerId, null);
		if (inventory == null)
		{
			throw new ConnectorPluginException("No inventory found for player %s".formatted(playerId));
		}
		return inventory;
	}
}
package net.runelite.client.plugins.zgsfreezetimer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@PluginDescriptor(
        name = "ZGS Freeze Timer",
        description = "Displays a countdown when an NPC is frozen by Zamorak Godsword",
        tags = {"ZGS", "freeze", "timer"}
)

@Slf4j
public class ZGSFreezeTimerPlugin extends Plugin {

    private static final int ZGS_WEAPON_SPEC_ID = 7638; // ZGS special attack animation ID
    private static final int FREEZE_DURATION_TICKS = 32; // 20 seconds (32 game ticks)

    @Getter
    private final Map<Integer, Long> frozenNpcs = new HashMap<>(); // Stores NPC ID and timestamp when they will unfreeze

    @Inject
    private Client client;

    @Inject
    private ZGSFreezeTimerOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Override
    protected void startUp() throws Exception {
        // Register the overlay with the OverlayManager to display it
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        // Unregister the overlay when the plugin shuts down
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onHitsplatApplied(@NotNull HitsplatApplied event) {
        if (!(event.getActor() instanceof NPC)) {
            return;
        }

        // Now check other players for ZGS special and NPC freeze hit
        for (Player players : client.getPlayers()) {
            // Check the animation of other players
            if (players.getAnimation() == ZGS_WEAPON_SPEC_ID) {
                // The other player used the ZGS special, check if they hit an NPC
                if (event.getHitsplat().getAmount() != 0) { // Make sure it's a freeze
                    NPC npc = (NPC) event.getActor();
                    long unfreezeTime = System.currentTimeMillis() + (FREEZE_DURATION_TICKS * 600); // convert ticks to ms
                    frozenNpcs.put(npc.getIndex(), unfreezeTime);
                    log.debug("ZGS special applied by {}: NPC {} frozen for {} seconds.", players.getName(), npc.getIndex(), FREEZE_DURATION_TICKS);
                }
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        long currentTime = System.currentTimeMillis();
        frozenNpcs.entrySet().removeIf(entry -> entry.getValue() <= currentTime); // Remove NPCs that have unfrozen
    }
}

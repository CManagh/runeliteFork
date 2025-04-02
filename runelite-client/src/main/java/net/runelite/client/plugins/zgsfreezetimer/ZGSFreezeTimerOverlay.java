package net.runelite.client.plugins.zgsfreezetimer;

import net.runelite.api.NPC;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

@Slf4j  // Automatically provides a logger for this class
public class ZGSFreezeTimerOverlay extends Overlay {

    private final ZGSFreezeTimerPlugin plugin;
    private final Client client;

    @Inject
    public ZGSFreezeTimerOverlay(ZGSFreezeTimerPlugin plugin, Client client) {
        this.plugin = plugin;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC); // Set overlay position dynamically
        setLayer(OverlayLayer.ABOVE_SCENE); // Place overlay above the game scene
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        long currentTime = System.currentTimeMillis();

        // Iterate through all frozen NPCs and render the countdown for each
        for (Map.Entry<Integer, Long> entry : plugin.getFrozenNpcs().entrySet()) {
            int npcId = entry.getKey();
            long unfreezeTime = entry.getValue();

            // Get the NPC by its index
            NPC npc = client.getNpcs().stream()
                    .filter(n -> n.getIndex() == npcId)
                    .findFirst()
                    .orElse(null);

            if (npc == null) {
                continue;
            }

            // Calculate remaining time until unfreeze in seconds
            long remainingTime = (unfreezeTime - currentTime) / 1000;

            // Skip NPCs that have already unfrozen
            if (remainingTime <= 0) {
                continue;
            }

            // Get the NPC's screen coordinates
            String npcName = npc.getName();
            Point textLocation = npc.getCanvasTextLocation(graphics, npcName, npc.getLogicalHeight() + 40);

            // If the NPC's position is visible on the canvas, draw the timer
            if (textLocation != null) {
                // Set up the text drawing properties
                graphics.setColor(Color.RED);
                graphics.setFont(new Font("Arial", Font.BOLD, 14));

                // Position the text slightly above the NPC
                graphics.drawString(String.valueOf(remainingTime), textLocation.getX(), textLocation.getY() - 20);

                log.debug("Rendering freeze timer for NPC {}: {} seconds remaining.", npcId, remainingTime);
            }
        }

        return null;
    }
}

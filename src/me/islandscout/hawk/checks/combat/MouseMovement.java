package me.islandscout.hawk.checks.combat;

import me.islandscout.hawk.HawkPlayer;
import me.islandscout.hawk.checks.AsyncCustomCheck;
import me.islandscout.hawk.checks.AsyncMovementCheck;
import me.islandscout.hawk.checks.Cancelless;
import me.islandscout.hawk.events.Event;
import me.islandscout.hawk.events.InteractAction;
import me.islandscout.hawk.events.InteractEntityEvent;
import me.islandscout.hawk.events.PositionEvent;
import me.islandscout.hawk.utils.Debug;
import net.minecraft.server.v1_7_R4.Position;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityInteractEvent;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FightAimbot exploits a flaw in aim-bot cheats by
 * analyzing mouse movement patterns during combat. Although
 * easily bypassed, it catches a significant number of cheaters.
 */
public class MouseMovement extends AsyncCustomCheck implements Cancelless {

    private Map<UUID, Double> lastLookDistanceSquared;
    private Map<UUID, Long> lastAttackTick;

    public MouseMovement() {
        super("mousemovement", true, -1, 5, 0.97, 2000, "&7%player% may be using aimbot. VL %vl%", null);
        lastLookDistanceSquared = new HashMap<>();
        lastAttackTick = new HashMap<>();
    }

    public void check(Event e) {
        if(e instanceof PositionEvent) {
            processMove((PositionEvent)e);
        }
        else if(e instanceof InteractEntityEvent) {
            processHit((InteractEntityEvent)e);
        }
    }

    private void processMove(PositionEvent e) {
        Player p = e.getPlayer();
        HawkPlayer pp = e.getHawkPlayer();
        UUID uuid = p.getUniqueId();
        if(pp.getCurrentTick() - lastAttackTick.getOrDefault(uuid, 0L) > 2)
            return;
        double lookDistanceSquared = pp.getDeltaYaw() * pp.getDeltaYaw() + pp.getDeltaPitch() * pp.getDeltaPitch();

        if(lastLookDistanceSquared.containsKey(uuid)) {
            if(lastLookDistanceSquared.get(uuid) > 8 && lookDistanceSquared < 0.001 && System.currentTimeMillis() - pp.getLastMoveTime() < 60) {
                punish(pp);
            }
            else {
                reward(pp);
            }
        }

        lastLookDistanceSquared.put(uuid, lookDistanceSquared);
    }

    private void processHit(InteractEntityEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        HawkPlayer pp = e.getHawkPlayer();
        lastAttackTick.put(uuid, pp.getCurrentTick());
    }

    public void removeData(Player p) {
        lastLookDistanceSquared.remove(p.getUniqueId());
        lastAttackTick.remove(p.getUniqueId());
    }
}

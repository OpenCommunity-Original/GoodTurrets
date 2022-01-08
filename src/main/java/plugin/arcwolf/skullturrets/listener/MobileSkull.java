package plugin.arcwolf.skullturrets.listener;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import plugin.arcwolf.skullturrets.SkullTurret;
import plugin.arcwolf.skullturrets.utils.Utils;

import java.util.UUID;

public class MobileSkull extends PlacedSkull {
    private int ammoAmount;

    public MobileSkull(final Block skullBlock, final UUID skullCreator, final int maxRange, final SkullIntelligence intelligence, final int ammo) {
        super(skullBlock, skullCreator, maxRange, intelligence);
        this.ammoAmount = 0;
        this.ammoAmount = ammo;
        this.ammoType = EntityType.ARROW;
    }

    public MobileSkull(final String inString) {
        super(inString);
        this.ammoAmount = 0;
        final String[] split = inString.split(this.SEPERATOR.toString());
        this.ammoAmount = Short.parseShort(split[16]);
        this.ammoType = EntityType.ARROW;
    }

    @Override
    public void tick() {
        if (this.ammoAmount < 0) {
            this.ammoAmount = 0;
        }
        if (this.ammoAmount == 0) {
            if (SkullTurret.ALLOW_TEMPTURRET_REARM) {
                return;
            }
            this.die();
        }
        super.tick();
    }

    public int getAmmoAmount() {
        return this.ammoAmount;
    }

    public void setAmmoAmount(final int ammoAmount) {
        this.ammoAmount = ammoAmount;
    }

    @Override
    public void setAmmoType(final String name) {
    }

    @Override
    public void setAmmoType(final EntityType ent) {
    }

    @Override
    public void destruct() {
        if (SkullTurret.SKULLVFX) {
            final Location loc = this.getCenterPoint();
            final double x = loc.getX();
            final double y = loc.getY();
            final double z = loc.getZ();
            final World world = loc.getWorld();
            world.createExplosion(x, y, z, 0.0f, false, false);
        }
        Utils.clearBlock(this.getLocation().getBlock());
        Utils.clearBlock(this.getLocation().getBlock().getRelative(BlockFace.DOWN));
    }

    @Override
    public String toString() {
        final Character designation = '\u001e';
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(this.SEPERATOR.toString());
        sb.append(designation);
        sb.append(this.SEPERATOR);
        sb.append(this.ammoAmount);
        return sb.toString();
    }
}

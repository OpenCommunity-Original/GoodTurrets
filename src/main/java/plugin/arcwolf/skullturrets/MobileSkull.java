package plugin.arcwolf.skullturrets;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class MobileSkull extends PlacedSkull {
    private int ammoAmount = 0;

    public MobileSkull(Block skullBlock, UUID skullCreator, int maxRange, SkullIntelligence intelligence, int ammo) {
        super(skullBlock, skullCreator, maxRange, intelligence);
        this.ammoAmount = ammo;
        this.ammoType = EntityType.ARROW;
    }

    public MobileSkull(String inString) {
        super(inString);
        String[] split = inString.split(this.SEPERATOR.toString());
        this.ammoAmount = Short.parseShort(split[16]);
        this.ammoType = EntityType.ARROW;
    }

    public void tick() {
        if (this.ammoAmount < 0)
            this.ammoAmount = 0;
        if (this.ammoAmount == 0) {
            if (SkullTurret.ALLOW_TEMPTURRET_REARM)
                return;
            die();
        }
        super.tick();
    }

    public int getAmmoAmount() {
        return this.ammoAmount;
    }

    public void setAmmoAmount(int ammoAmount) {
        this.ammoAmount = ammoAmount;
    }

    public void setAmmoType(String name) {
    }

    public void setAmmoType(EntityType ent) {
    }

    public void destruct() {
        if (SkullTurret.SKULLVFX) {
            Location loc = getCenterPoint();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            World world = loc.getWorld();
            world.createExplosion(x, y, z, 0.0F, false, false);
        }
        Utils.clearBlock(getLocation().getBlock());
        Utils.clearBlock(getLocation().getBlock().getRelative(BlockFace.DOWN));
    }

    public String toString() {
        Character designation = Character.valueOf('\036');
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(this.SEPERATOR.toString());
        sb.append(designation);
        sb.append(this.SEPERATOR);
        sb.append(this.ammoAmount);
        return sb.toString();
    }
}

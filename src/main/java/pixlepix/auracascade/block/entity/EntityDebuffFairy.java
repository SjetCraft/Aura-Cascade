package pixlepix.auracascade.block.entity;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import pixlepix.auracascade.AuraCascade;
import pixlepix.auracascade.network.PacketBurst;

import java.util.List;

/**
 * Created by pixlepix on 12/13/14.
 */
public class EntityDebuffFairy extends EntityFairy {
    public PotionEffect[] potionEffects;

    public EntityDebuffFairy(World p_i1582_1_) {
        super(p_i1582_1_);
        potionEffects = new PotionEffect[]{
        		
                new PotionEffect(MobEffects.POISON, 200),
                new PotionEffect(MobEffects.NAUSEA, 200),
                new PotionEffect(MobEffects.WEAKNESS, 200),
                new PotionEffect(MobEffects.WITHER, 200),
                new PotionEffect(MobEffects.SLOWNESS, 200),
                new PotionEffect(MobEffects.HUNGER, 200)};
                
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (!world.isRemote && world.getTotalWorldTime() % 3 == 0) {
            List<EntityMob> nearbyEntities = world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(posX - 2, posY - 2, posZ - 2, posX + 2, posY + 2, posZ + 2));
            if (nearbyEntities.size() > 0) {
                EntityMob entity = nearbyEntities.get(0);
                for (PotionEffect potionEffect : potionEffects) {
                    entity.addPotionEffect(potionEffect);
                }
               AuraCascade.proxy.networkWrapper.sendToAllAround(new PacketBurst(4, entity.posX, entity.posY, entity.posZ), new NetworkRegistry.TargetPoint(entity.world.provider.getDimension(), entity.posX, entity.posY, entity.posZ, 32));
            }
        }
    }
}

package archives.tater.phantomfall.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PhantomEntity.class)
public abstract class PhantomEntityMixin extends FlyingEntity {
	protected PhantomEntityMixin(EntityType<? extends FlyingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public boolean tryAttack(Entity target) {
		if (super.tryAttack(target)) {
			if (target instanceof LivingEntity livingEntity) {
				livingEntity.startRiding((PhantomEntity) (Object) this, true);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
		if (this.hasPassenger(passenger)) {
			double y = this.getY() - passenger.getHeight();
			positionUpdater.accept(passenger, this.getX(), y, this.getZ());
		}
	}
}

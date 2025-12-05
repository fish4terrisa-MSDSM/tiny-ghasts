package fish4terrisa.tinyghasts.utils;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

// Dynamic interface for all entities that implemented
// a subset of TameableEntity & Tameable(interface) methods
public final class TameableInterface {
    public static boolean IsTameable(Entity entity) {
        if (entity == null) return false;
        try {
            Class<?> clazz = entity.getClass();

            // boolean isOwner(LivingEntity entity)
            Method isOwner = clazz.getMethod("isOwner", LivingEntity.class);
            if (isOwner.getReturnType() != boolean.class) {
                return false;
            }

            // boolean isTamed()
            Method isTamed = clazz.getMethod("isTamed");
            if (isTamed.getReturnType() != boolean.class) {
                return false;
            }

            // void setTamed(boolean tamed, boolean updateAttributes)
            Method setTamed = clazz.getMethod("setTamed", boolean.class, boolean.class);
            if (setTamed.getReturnType() != void.class) {
                return false;
            }

            // Optional: void setTamedBy(PlayerEntity player)
            // Optional: LivingEntity getTopLevelOwner()
            // Use internal implemention if not present

        } catch (NoSuchMethodException e) {
            return false;
        }
        // At least one setOwner() implemention should be present
        boolean setOwnerExist = false;
        try {
            Class<?> clazz = entity.getClass();
            // void setOwner(@Nullable LivingEntity owner)
            Method setOwner = clazz.getMethod("setOwner", LivingEntity.class);
            if (setOwner.getReturnType() == void.class) {
                setOwnerExist = true;
            }
            // incompatible interface
            // Do nothing
        } catch (NoSuchMethodException e) {
            // incompatible interface
            // Do nothing
        }
        try {
            Class<?> clazz = entity.getClass();
            // void setOwner(@Nullable LazyEntityReference<LivingEntity> owner)
            Method setOwnerRef = clazz.getMethod("setOwner", LazyEntityReference.class);
            if (setOwnerRef.getReturnType() == void.class) {
                setOwnerExist = true;
            }
            // incompatible interface
            // Do nothing
        } catch (NoSuchMethodException e) {
            // incompatible interface
            // Do nothing
        }
        if (!setOwnerExist) {
            return false;
        }
        // At least one of getOwner()/getOwnerReference() should be present
        boolean getOwnerExist = false;
        try {
            Class<?> clazz = entity.getClass();
            // LivingEntity getOwner()
            Method getOwner = clazz.getMethod("getOwner");
            if (getOwner.getReturnType() == LivingEntity.class) {
                getOwnerExist = true;
            }
            // incompatible interface
            // Do nothing
        } catch (NoSuchMethodException e) {
            // incompatible interface
            // Do nothing
        }
        try {
            Class<?> clazz = entity.getClass();
            // LazyEntityReference<LivingEntity> getOwnerReference();
            Method getOwnerRef = clazz.getMethod("getOwnerReference");
            if (LazyEntityReference.class.isAssignableFrom(getOwnerRef.getReturnType())) {
                getOwnerExist = true;
            }
            // incompatible interface
            // Do nothing
        } catch (NoSuchMethodException e) {
            // incompatible interface
            // Do nothing
        }
        if (!getOwnerExist) {
            return false;
        }
        return true;

    }

    // LazyEntityReference<LivingEntity> getOwnerReference();
    @Nullable
    public static LazyEntityReference<LivingEntity> CastgetOwnerReference(@Nullable Entity target) {
        if (target == null) return null;
        try {
            Method method = target.getClass().getMethod("getOwnerReference");
            Object result = method.invoke(target);
            return (LazyEntityReference<LivingEntity>) result;
        } catch (Exception e) {
            // Method not found or invocation failed
            // Failback
            LivingEntity owner = TameableInterface.CastgetOwner(target);
            if (owner == null) return null;
            return new LazyEntityReference<LivingEntity>(owner);
        }
    }

    // boolean isOwner(LivingEntity entity)
    public static boolean CastisOwner(@Nullable Entity target, @Nullable LivingEntity entity) {
        if (target == null || entity == null) return false;
        try {
            Method method = target.getClass().getMethod("isOwner", LivingEntity.class);
            Object result = method.invoke(target, entity);
            return (boolean) result;
        } catch (Exception e) {
            // Method not found or invocation failed
            return false;
        }
    }

    // public boolean isTamed()
    public static boolean CastisTamed(@Nullable Entity target) {
        if (target == null) return false;
        try {
            Method method = target.getClass().getMethod("isTamed");
            Object result = method.invoke(target);
            return (boolean) result;
        } catch (Exception e) {
            return false;
        }
    }

    // void setTamed(boolean tamed, boolean _updateAttributes)
    public static void CastsetTamed(@Nullable Entity target, boolean tamed, boolean updateAttributes) {
        if (target == null) return;
        try {
            Method method = target.getClass().getMethod("setTamed", boolean.class, boolean.class);
            method.invoke(target, tamed, updateAttributes);
        } catch (Exception e) {
            // Do nothing
        }
    }

    // LivingEntity getOwner()
    @Nullable
    public static LivingEntity CastgetOwner(@Nullable Entity target) {
        if (target == null) return null;
        try {
            Method method = target.getClass().getMethod("getOwner");
            Object result = method.invoke(target);
            return (LivingEntity) result;
        } catch (Exception e) {
            // Failback
            LazyEntityReference<LivingEntity> ownerref = TameableInterface.CastgetOwnerReference(target);
            if (ownerref == null) return null;
            return LazyEntityReference.resolve(ownerref, target.getWorld(), LivingEntity.class);
            
        }
    }

    // void setOwner(@Nullable LivingEntity owner)
    @Nullable
    public static void CastsetOwner(@Nullable Entity target, @Nullable LivingEntity owner) {
        boolean succeed = false;
        if (target == null || owner == null) return;
        try {
            Method method = target.getClass().getMethod("setOwner", LivingEntity.class);
            method.invoke(target, owner);
            succeed = true;
        } catch (Exception e) {
        }
        if (!succeed) {
            // Failback
            try {
                Method method = target.getClass().getMethod("setOwner", LazyEntityReference.class);
                method.invoke(target, new LazyEntityReference<LivingEntity>(owner));
                // Nobody cares... really
                succeed = true;
            } catch (Exception e) {
            }
        }
    }

    // void setOwner(@Nullable LazyEntityReference<LivingEntity> owner)
    @Nullable
    public static void CastsetOwner(@Nullable Entity target, @Nullable LazyEntityReference<LivingEntity> owner) {
        boolean succeed = false;
        if (target == null || owner == null) return;
        try {
            Method method = target.getClass().getMethod("setOwner", LazyEntityReference.class);
            method.invoke(target, owner);
            succeed = true;
        } catch (Exception e) {
        }
        if (!succeed) {
            // Failback
            try {
            Method method = target.getClass().getMethod("setOwner", LivingEntity.class);
            method.invoke(target, LazyEntityReference.resolve(owner, target.getWorld(), LivingEntity.class));
            // Nobody cares... really
            succeed = true;
            } catch (Exception e) {
            }
        }
    }

    // setTamedBy(PlayerEntity player)
    public static void CastsetTamedBy(@Nullable Entity target, @Nullable PlayerEntity player) {
        if (target == null || player == null) return;
        try {
            Method method = target.getClass().getMethod("setTamedBy", PlayerEntity.class);
            method.invoke(target, player);
        } catch (Exception e) {
            TameableInterface.CastsetTamed(target, true, true);
            TameableInterface.CastsetOwner(target, player);
        }
    }

    // LivingEntity getTopLevelOwner()
    @Nullable
    public static LivingEntity CastgetTopLevelOwner(@Nullable Entity target) {
        if (target == null) return null;
        try {
            Method method = target.getClass().getMethod("getTopLevelOwner");
            Object result = method.invoke(target);
            return (LivingEntity) result;
        } catch (Exception e) {
            ObjectArraySet set = new ObjectArraySet();
            LivingEntity owner = TameableInterface.CastgetOwner(target);
            set.add(target);
            while (TameableInterface.IsTameable(owner)) {
                LivingEntity upperowner = TameableInterface.CastgetOwner(owner);
                if (upperowner == null) {
                    break;
                }
                if (set.contains(upperowner)) {
                    return null;
                }
                set.add(owner);
                owner = upperowner;
            }
            return owner;
        }
    }

}

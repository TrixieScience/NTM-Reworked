package com.hbm.ntm.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Coordinate frame used by turrets placed in ordinary levels or moving Sable sublevels. */
public interface TurretTargetingFrame {
    default Vec3 hbm$localPositionToWorld(Vec3 position) { return position; }
    default Vec3 hbm$worldPositionToLocal(Vec3 position) { return position; }
    default Vec3 hbm$localVectorToWorld(Vec3 vector) { return vector; }
    default Vec3 hbm$worldVectorToLocal(Vec3 vector) { return vector; }
    default Vec3 hbm$entityPosition(Entity entity) { return entity.position(); }
    default Vec3 hbm$entityEyePosition(Entity entity) { return entity.getEyePosition(); }
    default Vec3 hbm$velocityAt(Vec3 localPosition) { return Vec3.ZERO; }
}

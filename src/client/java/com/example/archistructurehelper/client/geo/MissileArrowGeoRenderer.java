package com.example.archistructurehelper.client.geo;

import com.example.archistructurehelper.client.ArchiArrowClient;
import com.example.archistructurehelper.client.ArchiArrowClient.ResolvedAssets;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

public class MissileArrowGeoRenderer extends GeoObjectRenderer<MissileArrowAnimatable> {
    public static final MissileArrowGeoRenderer INSTANCE = new MissileArrowGeoRenderer();

    // Orientation tuning
    private static final double SPEED_EPS = 1.0e-4;
    private static final int    FREEZE_AFTER_FRAMES = 4;
    private static final float  SMOOTH_ALPHA = 0.35f;

    // Model forward-axis correction (adjust per your models)
    private static final float MODEL_YAW_OFFSET   = 180f;
    private static final float MODEL_PITCH_OFFSET = 0f;


    private static final class RotState {
        boolean init;
        float yaw, pitch;
        int stillFrames;
        boolean frozen;
    }
    private final Int2ObjectOpenHashMap<RotState> rot = new Int2ObjectOpenHashMap<>();

    private MissileArrowGeoRenderer() {
        super(new MissileArrowGeoModel());
    }

    @Override
    public RenderLayer getRenderType(MissileArrowAnimatable anim, Identifier texture,
                                     VertexConsumerProvider buffers, float partialTick) {
        return RenderLayer.getEntityCutoutNoCull(texture);
    }

    private RotState stateFor(ProjectileEntity e) {
        return rot.computeIfAbsent(e.getId(), id -> new RotState());
    }

    private static float yawFrom(Vec3d v) {
        return (float) Math.toDegrees(Math.atan2(-v.x, v.z));
    }

    private static float pitchFrom(Vec3d v) {
        double xz = Math.sqrt(v.x * v.x + v.z * v.z);
        return (float) Math.toDegrees(Math.atan2(-v.y, xz));
    }

    private static float smoothAngle(float current, float target, float alpha) {
        float diff = MathHelper.wrapDegrees(target - current);
        return current + diff * alpha;
    }

    public void renderProjectile(ProjectileEntity entity, float tickDelta,
                                 MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        if (!entity.isAlive() || entity.isRemoved()) {
            rot.remove(entity.getId());
            return;
        }

        // Parse AS:identifier|...
        String token = ArchiArrowClient.parseAssetToken(entity.getCustomName());
        if (token == null || token.isEmpty()) {
            return; // no AS tag → do nothing (mixin will allow vanilla if you don't cancel there)
        }

        // Resolve assets (packs first; then fallback to mod ns). If either missing → render nothing.
        ResolvedAssets assets = ArchiArrowClient.resolveAssetsOrNull(token);
        if (assets == null) {
            return;
        }

        // Orientation from velocity only (yaw+pitch, no roll)
        RotState rs = stateFor(entity);
        Vec3d v = entity.getVelocity();
        double speed2 = v.lengthSquared();

        Float desiredYaw = null, desiredPitch = null;
        if (speed2 > SPEED_EPS) {
            desiredYaw   = yawFrom(v);
            desiredPitch = pitchFrom(v);
        }

        if (!rs.init) {
            if (desiredYaw != null) {
                rs.yaw = desiredYaw;
                rs.pitch = desiredPitch;
            }
            rs.init = true;
        } else if (!rs.frozen && desiredYaw != null) {
            rs.yaw   = smoothAngle(rs.yaw,   desiredYaw,   SMOOTH_ALPHA);
            rs.pitch = smoothAngle(rs.pitch, desiredPitch, SMOOTH_ALPHA);
        }

        if (speed2 <= SPEED_EPS) {
            if (++rs.stillFrames >= FREEZE_AFTER_FRAMES) rs.frozen = true;
        } else {
            rs.stillFrames = 0;
        }

        // --- Render with the resolved model+texture ---
        matrices.push();

        final float renderYaw   = -rs.yaw   + MODEL_YAW_OFFSET;
        final float renderPitch = -rs.pitch + MODEL_PITCH_OFFSET;

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(renderYaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(renderPitch));

        matrices.translate(-0.5, -0.6, 0.0);


        MissileArrowAnimatable anim = new MissileArrowAnimatable(entity);
        anim.setResolvedModel(assets.model());
        anim.setResolvedTexture(assets.texture());

        RenderLayer layer = getRenderType(anim, assets.texture(), consumers, tickDelta);
        this.render(matrices, anim, consumers, layer, null, light, tickDelta);

        matrices.pop();
    }
}

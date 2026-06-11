package com.example.archistructurehelper.client.geo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Holds the projectile and the resolved model/texture chosen by the renderer. */
public class MissileArrowAnimatable implements GeoAnimatable {
    private final ProjectileEntity projectile;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Identifier modelId;
    private Identifier textureId;

    public MissileArrowAnimatable(ProjectileEntity projectile) {
        this.projectile = projectile;
    }

    public ProjectileEntity getProjectile() { return projectile; }

    /* Set by renderer before calling GeckoLib's render */
    public void setResolvedModel(Identifier model)   { this.modelId = model; }
    public void setResolvedTexture(Identifier tex)   { this.textureId = tex; }
    public Identifier getResolvedModel()             { return modelId; }
    public Identifier getResolvedTexture()           { return textureId; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations needed for static missiles (add if you have anims).
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public double getTick(Object object) {
        return projectile.age + MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
    }
}

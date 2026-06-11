package com.example.archistructurehelper.client.mixin;

import com.example.archistructurehelper.client.ArchiArrowClient;
import com.example.archistructurehelper.client.geo.MissileArrowGeoRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts all ProjectileEntity with an "AS:..." tag and replaces vanilla rendering
 * with our GeckoLib model. If no AS tag, do nothing (vanilla continues).
 */
@Mixin(EntityRenderer.class)
public class ArrowRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void archi$render(Entity entity, float entityYaw, float tickDelta,
                              MatrixStack matrices, VertexConsumerProvider consumers, int light,
                              CallbackInfo ci) {
        if (!(entity instanceof ProjectileEntity proj)) return;

        // Only intercept if there's an AS: token; else let vanilla draw.
        if (ArchiArrowClient.parseAssetToken(proj.getCustomName()) == null) return;

        // Our renderer will resolve assets and may choose to draw nothing if missing.
        MissileArrowGeoRenderer.INSTANCE.renderProjectile(proj, tickDelta, matrices, consumers, light);
        ci.cancel();
    }
}

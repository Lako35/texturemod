package com.example.archistructurehelper.client.geo;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeoModel simply returns whatever the renderer resolved.
 * That guarantees the model+texture used by GeckoLib match the existence checks done earlier.
 */
public class MissileArrowGeoModel extends GeoModel<MissileArrowAnimatable> {

    @Override
    public Identifier getModelResource(MissileArrowAnimatable anim) {
        return anim.getResolvedModel(); // must be non-null (renderer enforces)
    }

    @Override
    public Identifier getTextureResource(MissileArrowAnimatable anim) {
        return anim.getResolvedTexture(); // must be non-null (renderer enforces)
    }

    @Override
    public Identifier getAnimationResource(MissileArrowAnimatable anim) {
        return null; // static
    }
}

package com.example.archistructurehelper.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchiArrowClient implements ClientModInitializer {
    public static final String MOD_NS = "archistructurehelper";

    // AS:identifier|anything (identifier may include namespace like ns:path/sub)
    private static final Pattern AS_TOKEN =
            Pattern.compile("(?i)\\bAS\\s*:\\s*([^|]+)");

    @Override public void onInitializeClient() { /* no-op */ }

    /** Returns the identifier token after AS: (trimmed), or null if not present. */
    public static String parseAssetToken(Text name) {
        if (name == null) return null;
        String raw = Normalizer.normalize(name.getString(), Normalizer.Form.NFKC);
        Matcher m = AS_TOKEN.matcher(raw);
        if (!m.find()) return null;
        return m.group(1).trim();
    }

    /** Splits "ns:path/thing" or "path/thing" into (namespace, path). */
    public static NsPath splitNsPath(String token) {
        String ns = MOD_NS;
        String path = token;
        int i = token.indexOf(':');
        if (i > 0) {
            ns = token.substring(0, i).trim();
            path = token.substring(i + 1).trim();
        }
        path = path.replace('\\', '/');
        while (path.startsWith("/")) path = path.substring(1);
        return new NsPath(ns, path);
    }

    /** Builds model id: assets/<ns>/geo/<path>.geo.json */
    public static Identifier modelId(String ns, String path) {
        return Identifier.of(ns, "geo/" + path + ".geo.json");
    }

    /** Builds texture id: assets/<ns>/textures/entity/<path>.png */
    public static Identifier textureId(String ns, String path) {
        return Identifier.of(ns, "textures/entity/" + path + ".png");
    }

    /** True if any enabled resource pack (or mod jar) provides this id. */
    public static boolean resourceExists(Identifier id) {
        if (id == null) return false;
        ResourceManager rm = MinecraftClient.getInstance().getResourceManager();
        return rm.getResource(id).isPresent();
    }

    private static final boolean DEBUG = true;
    private static void log(String s){ if (DEBUG) System.out.println("[ArchiArrow] " + s); }

    public static ResolvedAssets resolveAssetsOrNull(String token) {
        if (token == null || token.isEmpty()) return null;

        NsPath np = splitNsPath(token);
        Identifier modelPrimary   = modelId(np.ns, np.path);
        Identifier texturePrimary = textureId(np.ns, np.path);

        boolean m1 = resourceExists(modelPrimary);
        boolean t1 = resourceExists(texturePrimary);
        log("token="+token+" ns="+np.ns+" path="+np.path+" model="+modelPrimary+"="+m1+" tex="+texturePrimary+"="+t1);

        if (m1 && t1) return new ResolvedAssets(modelPrimary, texturePrimary);

        if (!MOD_NS.equals(np.ns)) {
            Identifier modelFallback   = modelId(MOD_NS, np.path);
            Identifier textureFallback = textureId(MOD_NS, np.path);
            boolean m2 = resourceExists(modelFallback);
            boolean t2 = resourceExists(textureFallback);
            log("fallback model="+modelFallback+"="+m2+" tex="+textureFallback+"="+t2);
            if (m2 && t2) return new ResolvedAssets(modelFallback, textureFallback);
        }
        return null;
    }

    /* --- tiny records for convenience --- */
    public record NsPath(String ns, String path) {}
    public record ResolvedAssets(Identifier model, Identifier texture) {}
}

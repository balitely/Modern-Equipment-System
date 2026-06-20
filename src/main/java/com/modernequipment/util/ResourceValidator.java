package com.modernequipment.util;

import com.modernequipment.MESMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源验证工具类
 * 用于检查贴图、模型等资源是否存在，避免渲染时因资源缺失导致日志刷屏
 */
@OnlyIn(Dist.CLIENT)
public class ResourceValidator {

    // 缓存已检查过的资源，避免重复检查
    private static final ConcurrentHashMap<String, Boolean> resourceCache = new ConcurrentHashMap<>();

    // 默认贴图路径
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("modernequipment", "textures/armor/default.png");
    private static final ResourceLocation DEFAULT_GEO = new ResourceLocation("modernequipment", "geo/armor/default.geo.json");

    /**
     * 检查贴图资源是否存在
     * @param texturePath 贴图路径（如 "modernequipment:textures/item/armor.png"）
     * @return 如果存在返回 true，否则返回 false
     */
    public static boolean textureExists(String texturePath) {
        if (texturePath == null || texturePath.isEmpty()) {
            return false;
        }

        String cacheKey = "texture:" + texturePath;
        Boolean cached = resourceCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            ResourceLocation location = parseResourceLocation(texturePath, "textures");
            boolean exists = checkResourceExists(location);
            resourceCache.put(cacheKey, exists);

            if (!exists) {
                MESMod.LOGGER.warn("Texture resource not found: {} (resolved to {})", texturePath, location);
            }

            return exists;
        } catch (Exception e) {
            MESMod.LOGGER.warn("Failed to check texture existence: {}", texturePath, e);
            resourceCache.put(cacheKey, false);
            return false;
        }
    }

    /**
     * 检查 Geo 模型资源是否存在
     * @param geoPath Geo 模型路径（如 "modernequipment:geo/armor/helmet.geo.json"）
     * @return 如果存在返回 true，否则返回 false
     */
    public static boolean geoExists(String geoPath) {
        if (geoPath == null || geoPath.isEmpty()) {
            return false;
        }

        String cacheKey = "geo:" + geoPath;
        Boolean cached = resourceCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            ResourceLocation location = parseResourceLocation(geoPath, "geo");
            boolean exists = checkResourceExists(location);
            resourceCache.put(cacheKey, exists);

            if (!exists) {
                MESMod.LOGGER.warn("Geo model resource not found: {} (resolved to {})", geoPath, location);
            }

            return exists;
        } catch (Exception e) {
            MESMod.LOGGER.warn("Failed to check geo existence: {}", geoPath, e);
            resourceCache.put(cacheKey, false);
            return false;
        }
    }

    /**
     * 检查动画资源是否存在
     * @param animationPath 动画文件路径
     * @return 如果存在返回 true，否则返回 false
     */
    public static boolean animationExists(String animationPath) {
        if (animationPath == null || animationPath.isEmpty()) {
            return false;
        }

        String cacheKey = "animation:" + animationPath;
        Boolean cached = resourceCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            ResourceLocation location = parseResourceLocation(animationPath, "animations");
            boolean exists = checkResourceExists(location);
            resourceCache.put(cacheKey, exists);

            if (!exists) {
                MESMod.LOGGER.warn("Animation resource not found: {} (resolved to {})", animationPath, location);
            }

            return exists;
        } catch (Exception e) {
            MESMod.LOGGER.warn("Failed to check animation existence: {}", animationPath, e);
            resourceCache.put(cacheKey, false);
            return false;
        }
    }

    /**
     * 检查模型资源是否存在
     * @param modelPath 模型路径
     * @return 如果存在返回 true，否则返回 false
     */
    public static boolean modelExists(String modelPath) {
        if (modelPath == null || modelPath.isEmpty()) {
            return false;
        }

        String cacheKey = "model:" + modelPath;
        Boolean cached = resourceCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            ResourceLocation location = parseResourceLocation(modelPath, "models");
            boolean exists = checkResourceExists(location);
            resourceCache.put(cacheKey, exists);

            if (!exists) {
                MESMod.LOGGER.warn("Model resource not found: {} (resolved to {})", modelPath, location);
            }

            return exists;
        } catch (Exception e) {
            MESMod.LOGGER.warn("Failed to check model existence: {}", modelPath, e);
            resourceCache.put(cacheKey, false);
            return false;
        }
    }

    /**
     * 解析资源路径为 ResourceLocation
     * @param path 资源路径
     * @param defaultPrefix 默认前缀（如 "textures", "geo"）
     * @return ResourceLocation
     */
    private static ResourceLocation parseResourceLocation(String path, String defaultPrefix) {
        if (path.contains(":")) {
            return new ResourceLocation(path);
        } else {
            return new ResourceLocation("modernequipment", defaultPrefix + "/" + path);
        }
    }

    /**
     * 检查资源是否存在
     * @param location 资源位置
     * @return 如果存在返回 true，否则返回 false
     */
    private static boolean checkResourceExists(ResourceLocation location) {
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            return resourceManager.getResource(location).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取默认贴图路径
     */
    public static ResourceLocation getDefaultTexture() {
        return DEFAULT_TEXTURE;
    }

    /**
     * 获取默认 Geo 模型路径
     */
    public static ResourceLocation getDefaultGeo() {
        return DEFAULT_GEO;
    }

    /**
     * 清空缓存（用于资源重载时）
     */
    public static void clearCache() {
        resourceCache.clear();
        MESMod.LOGGER.info("ResourceValidator cache cleared");
    }

    /**
     * 获取缓存统计信息
     */
    public static String getCacheStats() {
        return String.format("ResourceValidator cache: %d entries", resourceCache.size());
    }
}

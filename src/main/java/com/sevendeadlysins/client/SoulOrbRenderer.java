package com.sevendeadlysins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sevendeadlysins.entity.SoulOrbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Клиентский рендерер для сферы души Чревоугодия (SoulOrbEntity) под Minecraft 1.20.1 (Forge 47.3.0).
 * Совместим с Embeddium / Oculus шейдерами в сборке Cisco's Fantasy Medieval RPG [Dragonfyre].
 */
public class SoulOrbRenderer extends EntityRenderer<SoulOrbEntity> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION =
            new ResourceLocation("minecraft", "textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE =
            RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    public SoulOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    protected int getBlockLightLevel(SoulOrbEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(SoulOrbEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float pulse = (float) Math.sin((entity.tickCount + partialTicks) * 0.25F) * 0.08F;
        float scale = 0.45F + pulse;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);

        int r = 45;
        int g = 255;
        int b = 195;

        vertex(consumer, matrix4f, matrix3f, -0.5F, -0.25F, r, g, b, 0.0F, 0.25F, packedLight);
        vertex(consumer, matrix4f, matrix3f, 0.5F, -0.25F, r, g, b, 0.25F, 0.25F, packedLight);
        vertex(consumer, matrix4f, matrix3f, 0.5F, 0.75F, r, g, b, 0.25F, 0.0F, packedLight);
        vertex(consumer, matrix4f, matrix3f, -0.5F, 0.75F, r, g, b, 0.0F, 0.0F, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix4f,
            Matrix3f matrix3f,
            float x,
            float y,
            int r,
            int g,
            int b,
            float u,
            float v,
            int packedLight
    ) {
        consumer.vertex(matrix4f, x, y, 0.0F)
                .color(r, g, b, 220)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SoulOrbEntity entity) {
        return EXPERIENCE_ORB_LOCATION;
    }
}

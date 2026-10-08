package cn.ac.nya.opticzcraft.render;

import cn.ac.nya.opticzcraft.entity.EntityArrow;
import cn.ac.nya.opticzcraft.opticzCraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Renders the {@link EntityArrow} cluster missile: the four-bladed missile body the 1.12.2
 * {@code render.ArrowRenderer} drew, ported vertex for vertex.
 *
 * <p>Mapping from 1.12.2 {@code Render<EntityArrow>} to 1.21.1 {@link EntityRenderer}:</p>
 * <ul>
 *   <li>{@code Render<T>} is gone; the base is {@code EntityRenderer<T>} and the constructor takes the
 *       {@link EntityRendererProvider.Context} that FML builds for the registration event.</li>
 *   <li>{@code doRender(entity, x, y, z, u, v)} becomes
 *       {@code render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight)}. The
 *       renderer is handed a {@link PoseStack} already translated to the entity, so the 1.12.2
 *       {@code GlStateManager.translate((float)x, (float)y, (float)z)} disappears -- the remaining
 *       rotations, the shake, the 45-degree tilt, the 0.05625 scale and the -4 translate are kept in
 *       the same order.</li>
 *   <li>{@code getEntityTexture} becomes {@link #getTextureLocation(EntityArrow)} (same resource:
 *       {@code opticzcraft:textures/entity/arrow.png}). {@code bindEntityTexture} disappears because
 *       the texture is chosen when the render type is created.</li>
 *   <li>The {@code Tessellator}/{@code BufferBuilder.begin(7, POSITION_TEX)} pairs become a
 *       {@link VertexConsumer} from
 *       {@code bufferSource.getBuffer(RenderType.entitySolid(texture))} -- the same render type
 *       vanilla's own {@code ArrowRenderer} uses for its {@code POSITION_TEX} stream. The stride is
 *       written with an identity pose so the raw model coordinates stay exactly as they were.</li>
 *   <li>The interpolated yaw/pitch read {@code yRotO}/{@code xRotO} and {@code getYRot()}/{@code
 *       getXRot()} ({@code prevRotationYaw}/{@code rotationYaw} in 1.12.2), and the 1.12.2
 *       {@code arrowShake} field is {@code shakeTime} on {@code AbstractArrow} (both verified with
 *       javap against neoforge-21.1.256-merged.jar).</li>
 * </ul>
 *
 * <pre>
 * NOT PORTED -- no 1.21.1 counterpart:
 *   renderOutlines / GlStateManager.enableColorMaterial() / enableOutlineMode(getTeamColor(arrow))
 *       {@code EntityRenderer} in 1.21.1 has no {@code renderOutlines} field (verified with javap:
 *       neither it nor {@code getTeamColor} exists on the class). The glowing-outline pass is driven
 *       by the entity's glowing effect through the outline post-processor, so the two manual branches
 *       have nothing left to switch on.
 *   GlStateManager.disableLighting()/enableLighting(), enableRescaleNormal()/disableRescaleNormal(),
 *   glNormal3f, GlStateManager.color(1,1,1,1)
 *       Per-vertex lighting state, the rescale-normal stack and the fixed-function normal calls no
 *       longer exist; the per-vertex normal now travels on the vertex itself and lighting comes from
 *       the render type.
 * </pre>
 */
public class ArrowRenderer extends EntityRenderer<EntityArrow> {

    /** 1.12.2 {@code ArrowRenderer.ARROW}. */
    public static final ResourceLocation ARROW =
            ResourceLocation.fromNamespaceAndPath(opticzCraft.MODID, "textures/entity/arrow.png");

    public ArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityArrow arrow) {
        return ARROW;
    }

    @Override
    public void render(EntityArrow arrow, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(partialTick, arrow.yRotO, arrow.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(partialTick, arrow.xRotO, arrow.getXRot())));

        float shake = (float) arrow.shakeTime - partialTick;
        if (shake > 0.0F) {
            float rot = -Mth.sin(shake * 3.0F) * shake;
            poseStack.mulPose(Axis.ZP.rotationDegrees(rot));
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entitySolid(ARROW));
        PoseStack.Pose pose = poseStack.last();

        // BufferBuilder.begin(7, POSITION_TEX) + pos(...).tex(...) x4.
        quad(pose, buffer, packedLight,
                -7.0D, -2.0D, -2.0D, 0.0D, 0.15625D,
                -7.0D, -2.0D, 2.0D, 0.15625D, 0.15625D,
                -7.0D, 2.0D, 2.0D, 0.15625D, 0.3125D,
                -7.0D, 2.0D, -2.0D, 0.0D, 0.3125D);

        quad(pose, buffer, packedLight,
                -7.0D, 2.0D, -2.0D, 0.0D, 0.15625D,
                -7.0D, 2.0D, 2.0D, 0.15625D, 0.15625D,
                -7.0D, -2.0D, 2.0D, 0.15625D, 0.3125D,
                -7.0D, -2.0D, -2.0D, 0.0D, 0.3125D);

        for (int i = 0; i < 4; ++i) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose = poseStack.last();
            quad(pose, buffer, packedLight,
                    -8.0D, -2.0D, 0.0D, 0.0D, 0.0D,
                    8.0D, -2.0D, 0.0D, 0.5D, 0.0D,
                    8.0D, 2.0D, 0.0D, 0.5D, 0.15625D,
                    -8.0D, 2.0D, 0.0D, 0.0D, 0.15625D);
        }

        poseStack.popPose();

        super.render(arrow, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    /**
     * One 1.12.2 {@code POSITION_TEX} quad.
     *
     * <p>The four corners are the four {@code endVertex()} calls of the original in order, so the
     * triangles {@code a-b-c} / {@code a-c-d} cover the same surface with the same winding and the same
     * UVs. The 1.12.2 stream carried no per-vertex normal at all (the class set one fixed
     * {@code glNormal3f} before each quad); the normal written here is the face's geometric normal,
     * which is constant across the six vertices, so the flat shading is unchanged while the value is a
     * real unit vector instead of the model-space constant the 0.05625 scale used to cancel out.</p>
     */
    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int packedLight,
                             double ax, double ay, double az, double au, double av,
                             double bx, double by, double bz, double bu, double bv,
                             double cx, double cy, double cz, double cu, double cv,
                             double dx, double dy, double dz, double du, double dv) {
        float[] normal = windingNormal(ax, ay, az, bx, by, bz, cx, cy, cz);
        vertex(pose, buffer, packedLight, normal, ax, ay, az, au, av);
        vertex(pose, buffer, packedLight, normal, bx, by, bz, bu, bv);
        vertex(pose, buffer, packedLight, normal, cx, cy, cz, cu, cv);
        vertex(pose, buffer, packedLight, normal, dx, dy, dz, du, dv);
    }

    /** The unit normal implied by the corner order of one face -- see the note on {@link #quad}. */
    private static float[] windingNormal(double ax, double ay, double az,
                                         double bx, double by, double bz,
                                         double cx, double cy, double cz) {
        double ux = bx - ax, uy = by - ay, uz = bz - az;
        double vx = cx - ax, vy = cy - ay, vz = cz - az;
        double nx = uy * vz - uz * vy;
        double ny = uz * vx - ux * vz;
        double nz = ux * vy - uy * vx;
        double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length == 0.0) return new float[] { 0.0F, 0.0F, 1.0F };
        return new float[] { (float) (nx / length), (float) (ny / length), (float) (nz / length) };
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, int packedLight,
                               float[] normal,
                               double x, double y, double z, double u, double v) {
        // The vertex order below follows vanilla's 1.21.1 ArrowRenderer#vertex exactly.
        buffer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(-1)
                .setUv((float) u, (float) v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, normal[0], normal[1], normal[2]);
    }
}

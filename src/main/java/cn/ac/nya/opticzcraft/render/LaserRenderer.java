package cn.ac.nya.opticzcraft.render;

import cn.ac.nya.opticzcraft.api.IOpticzDev;
import cn.ac.nya.opticzcraft.tileentity.AbsOpticzDev;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * Created by drzzm32 on 2019.3.30.
 *
 * <p>Renders the six laser beams of an {@link AbsOpticzDev}: for every {@link Direction} the block
 * entity reports an out point for, a thin coloured bar is drawn from the block centre to that point.
 * The geometry, the radius, the colour source and the per-direction branch are carried over from the
 * 1.12.2 {@code render.LaserRenderer} <b>vertex for vertex</b>; what changed is the drawing API.</p>
 *
 * <p>Drawing translation (the 1.12.2 code wrote the vertices with
 * {@code Tessellator}/{@code BufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)}):</p>
 * <ul>
 *   <li>The vertex stream becomes a {@link VertexConsumer} obtained from
 *       {@link MultiBufferSource#getBuffer(RenderType)}. The render type is
 *       {@link RenderType#debugQuads()}, chosen because it is the only vanilla type that carries
 *       position + vertex colour with <b>no texture, no lighting and no back-face culling</b> -- which
 *       is exactly the state the original set up by hand
 *       ({@code glDisable(GL_TEXTURE_2D)}, {@code disableStandardItemLighting}, {@code
 *       glDepthMask(false)}, {@code glDisable(GL_CULL_FACE)}).</li>
 *   <li>The block centre comes from {@link AbsOpticzTESR} ({@code glTranslatef(x + 0.5F, ...)} ->
 *       {@code poseStack.translate(0.5F, 0.5F, 0.5F)}), so the local coordinates below are unchanged.
 *       A quad face is emitted as the two triangles that cover it, in the same winding order the
 *       four original {@code endVertex()} calls described.</li>
 *   <li>The original wrote no normals at all. {@code RenderType.debugQuads()} uses the
 *       {@code position_color_normal} vertex format, so a normal <i>is</i> written per vertex; it is
 *       the geometric normal of the quad face (constant per face, so shading is flat, matching the
 *       1.12.2 {@code GL_SMOOTH} state that was never given per-vertex normals).</li>
 *   <li>Depth-mask and blending are properties of the render type and are no longer pushed/popped
 *       around the draw; {@code glPushAttrib(GL_ALL_ATTRIB_BITS)} has no counterpart at all.</li>
 * </ul>
 *
 * <p><b>View bounds.</b> 1.12.2 made this renderer global ({@code isGlobalRenderer} -&gt; true) and had
 * the block entity answer {@code getRenderBoundingBox()} with {@code INFINITE_EXTENT_AABB} plus
 * {@code getMaxRenderDistanceSquared()} = 65535. In 1.21.1 those two {@code BlockEntity} methods are
 * gone and the renderer answers instead: {@link #shouldRenderOffScreen} carries the "global" flag and
 * {@link #getViewDistance} carries the reach. The view distance is expressed in <b>blocks</b> by the
 * 1.21.1 contract (the old value was a squared distance: 65535 -&gt; sqrt = exactly 256 blocks), so
 * {@code 256} preserves the original reach; it is deliberately not copied as {@code 65535}, which
 * would have been a 65535-block radius.</p>
 */
public class LaserRenderer extends AbsOpticzTESR {

    /** {@code sqrt(65535.0)} -- the 1.12.2 {@code getMaxRenderDistanceSquared()}, in blocks. */
    private static final int VIEW_DISTANCE = 256;

    /**
     * 1.12.2 shape kept for call-site parity ({@code new LaserRenderer()} in
     * {@code proxy/ClientProxy}).
     */
    public LaserRenderer() {
    }

    /** The 1.21.1 binding constructor FML expects on a {@code BlockEntityRendererProvider}. */
    public LaserRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context) {
        this();
    }

    /** 1.12.2 {@code isGlobalRenderer(TileEntity)} returned true for every device. */
    @Override
    public boolean shouldRenderOffScreen(AbsOpticzDev blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    @Override
    public void renderDevice(AbsOpticzDev blockEntity, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugQuads());

        final double radius = 0.0625 / 2;
        for (Direction facing : Direction.values()) {
            Vec3i veci = blockEntity.getOutpoint(facing);
            if (veci != null) {
                IOpticzDev.Color4 color = blockEntity.colorOf(facing);
                putLight(veci, radius, buffer, poseStack, color);
            }
        }
    }

    /**
     * One beam: four quad faces around the segment from the local origin to {@code (x, y, z)}, which is
     * the 1.12.2 {@code putLight} body unchanged apart from the vertex emission itself.
     */
    private void putLight(Vec3i vec3i, double radius, VertexConsumer buffer, PoseStack poseStack,
                          IOpticzDev.Color4 color) {
        int x = vec3i.getX();
        int y = vec3i.getY();
        int z = vec3i.getZ();
        if (x != 0 && y == 0 && z == 0) {
            quad(buffer, poseStack, color,
                    0, 0 + radius, 0 + radius,
                    x, y + radius, z + radius,
                    x, y + radius, z - radius,
                    0, 0 + radius, 0 - radius);

            quad(buffer, poseStack, color,
                    0, 0 + radius, 0 - radius,
                    x, y + radius, z - radius,
                    x, y - radius, z - radius,
                    0, 0 - radius, 0 - radius);

            quad(buffer, poseStack, color,
                    0, 0 - radius, 0 - radius,
                    x, y - radius, z - radius,
                    x, y - radius, z + radius,
                    0, 0 - radius, 0 + radius);

            quad(buffer, poseStack, color,
                    0, 0 - radius, 0 + radius,
                    x, y - radius, z + radius,
                    x, y + radius, z + radius,
                    0, 0 + radius, 0 + radius);
        } else if (x == 0 && y != 0 && z == 0) {
            quad(buffer, poseStack, color,
                    0 + radius, 0, 0 + radius,
                    x + radius, y, z + radius,
                    x + radius, y, z - radius,
                    0 + radius, 0, 0 - radius);

            quad(buffer, poseStack, color,
                    0 + radius, 0, 0 - radius,
                    x + radius, y, z - radius,
                    x - radius, y, z - radius,
                    0 - radius, 0, 0 - radius);

            quad(buffer, poseStack, color,
                    0 - radius, 0, 0 - radius,
                    x - radius, y, z - radius,
                    x - radius, y, z + radius,
                    0 - radius, 0, 0 + radius);

            quad(buffer, poseStack, color,
                    0 - radius, 0, 0 + radius,
                    x - radius, y, z + radius,
                    x + radius, y, z + radius,
                    0 + radius, 0, 0 + radius);
        } else if (x == 0 && y == 0 && z != 0) {
            quad(buffer, poseStack, color,
                    0 + radius, 0 + radius, 0,
                    x + radius, y + radius, z,
                    x + radius, y - radius, z,
                    0 + radius, 0 - radius, 0);

            quad(buffer, poseStack, color,
                    0 + radius, 0 - radius, 0,
                    x + radius, y - radius, z,
                    x - radius, y - radius, z,
                    0 - radius, 0 - radius, 0);

            quad(buffer, poseStack, color,
                    0 - radius, 0 - radius, 0,
                    x - radius, y - radius, z,
                    x - radius, y + radius, z,
                    0 - radius, 0 + radius, 0);

            quad(buffer, poseStack, color,
                    0 - radius, 0 + radius, 0,
                    x - radius, y + radius, z,
                    x + radius, y + radius, z,
                    0 + radius, 0 + radius, 0);
        }
    }

    /**
     * Emits one 1.12.2 quad as two triangles, with the face's own winding normal.
     *
     * <p>The four corners {@code (a, b, c, d)} are the four {@code endVertex()} calls of the original
     * quad, in order; the triangles are {@code a-b-c} and {@code a-c-d}, which is the standard fan
     * triangulation and therefore covers exactly the same surface with the same winding and the same
     * per-vertex colours. The 1.12.2 vertex stream carried no normal at all, so the value written is
     * derived from the corner order instead of being hand-picked: it is the geometric normal of the
     * face, constant across its six vertices, which keeps the shading flat. Note that the original
     * corner order makes some faces wind inward (the -X face of the Z branch, for example); that is
     * harmless here because {@code RenderType.debugQuads()} does not cull, exactly as the 1.12.2
     * renderer ran with {@code glDisable(GL_CULL_FACE)}.</p>
     */
    private static void quad(VertexConsumer buffer, PoseStack poseStack, IOpticzDev.Color4 color,
                             double ax, double ay, double az,
                             double bx, double by, double bz,
                             double cx, double cy, double cz,
                             double dx, double dy, double dz) {
        float[] normal = windingNormal(ax, ay, az, bx, by, bz, cx, cy, cz);

        vertex(buffer, poseStack, color, normal[0], normal[1], normal[2], ax, ay, az);
        vertex(buffer, poseStack, color, normal[0], normal[1], normal[2], bx, by, bz);
        vertex(buffer, poseStack, color, normal[0], normal[1], normal[2], cx, cy, cz);

        vertex(buffer, poseStack, color, normal[0], normal[1], normal[2], ax, ay, az);
        vertex(buffer, poseStack, color, normal[0], normal[1], normal[2], cx, cy, cz);
        vertex(buffer, poseStack, color, normal[0], normal[1], normal[2], dx, dy, dz);
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
        if (length == 0.0) return new float[] { 0.0F, 1.0F, 0.0F };
        return new float[] { (float) (nx / length), (float) (ny / length), (float) (nz / length) };
    }

    private static void vertex(VertexConsumer buffer, PoseStack poseStack, IOpticzDev.Color4 color,
                               float nx, float ny, float nz,
                               double x, double y, double z) {
        buffer.addVertex(poseStack.last(), (float) x, (float) y, (float) z)
                .setColor(color.r, color.g, color.b, color.a)
                .setNormal(poseStack.last(), nx, ny, nz);
    }
}

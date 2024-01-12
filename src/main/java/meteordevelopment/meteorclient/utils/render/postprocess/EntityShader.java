package meteordevelopment.meteorclient.utils.render.postprocess;

import meteordevelopment.meteorclient.mixin.accessor.WorldRendererAccessor;
import net.minecraft.client.gl.Framebuffer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public abstract class EntityShader extends PostProcessShader {
    private Framebuffer prevBuffer;

    @Override
    protected void preDraw() {
        prevBuffer = mc.worldRenderer.getEntityOutlinesFramebuffer();
        ((WorldRendererAccessor) mc.worldRenderer).setEntityOutlinesFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        if (prevBuffer == null) return;

        ((WorldRendererAccessor) mc.worldRenderer).setEntityOutlinesFramebuffer(prevBuffer);
        prevBuffer = null;
    }

    public void endRender() {
        endRender(() -> vertexConsumerProvider.draw());
    }
}

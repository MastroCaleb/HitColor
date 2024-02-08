package caleb.keep.xp.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;

import caleb.keep.xp.HitColorClient;
import caleb.keep.xp.interfaces.Updateable;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.awt.*;

@Mixin(OverlayTexture.class)
public class HitColorMixin implements Updateable{

	@Shadow @Final private NativeImageBackedTexture texture;
    private static Integer processedColor;

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V", ordinal = 0), index = 2)
    private int hitColor(int x) {
        return getColor();
    }

    public int getColor() {
        if (processedColor == null) {
            updateColor();
        }
        return processedColor;
    }

    public void updateColor() {
        Color rgbColor;
        try {
            rgbColor = new Color(Integer.parseInt(HitColorClient.color.substring(1), 16));
        } 
        catch (NumberFormatException e) {
            e.printStackTrace();
            if (processedColor == null) {
                processedColor = -1308622593;
            }
            return;
        }
        processedColor = rgbColor.getRed() | rgbColor.getGreen() << 8 | rgbColor.getBlue() << 16 | Math.round(0xFF * (1 - HitColorClient.alpha / 100)) << 24;
    }

    public void updateOverlay(){
        updateColor();

        NativeImage nativeImage = texture.getImage();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (i < 8) {
                    nativeImage.setColor(j, i, processedColor);
                    continue;
                }
                int k = (int)((1.0f - (float)j / 15.0f * 0.75f) * 255.0f);
                nativeImage.setColor(j, i, k << 24 | 0xFFFFFF);
            }
        }
        RenderSystem.activeTexture(GlConst.GL_TEXTURE1);
        texture.bindTexture();
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, true, false, false);
        RenderSystem.activeTexture(GlConst.GL_TEXTURE0);
    }
}

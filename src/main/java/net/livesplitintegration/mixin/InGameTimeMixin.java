package net.livesplitintegration.mixin;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.livesplitintegration.LivesplitIntegration;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class InGameTimeMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        try {

            // minecraft client instance (for reading coordinates) and the ingame time output from srigt. flows through LivesplitIntegration
            MinecraftClient client = MinecraftClient.getInstance();
            long igt = InGameTimer.getInstance().getInGameTime(true);

            LivesplitIntegration.get().onGameTick(client, igt);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
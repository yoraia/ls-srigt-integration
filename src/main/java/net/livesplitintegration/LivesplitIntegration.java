package net.livesplitintegration;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;

public class LivesplitIntegration implements ModInitializer {

    private static LivesplitIntegration INSTANCE;

    private final LivesplitController liveSplitController = new LivesplitController(); // communication with livespilt (start, split, time updates)
    private final SplitManager splitManager = new SplitManager(liveSplitController); // auto splitter
    //private final KeyHandler keyHandler = new KeyHandler();
    public static LivesplitIntegration get() {
        return INSTANCE;
    }


    @Override
    public void onInitialize() {
        INSTANCE = this;

        //uses srigt to do the final split when a run is completed
        InGameTimer.onComplete(timer -> {
            liveSplitController.split();

        });
    }

    // updates igt and sends the client state (for coords to use autosplit) each tick 
    public void onGameTick(MinecraftClient client, long igt) {
        liveSplitController.updateGameTime(igt);
        splitManager.handle(client);
        //keyHandler.tick(client);
    }


    public SplitManager getSplitManager() {
    return splitManager;
    }

    public LivesplitController getLiveSplitController() {
        return liveSplitController;
    }

    public void reloadSplits() {
        splitManager.reload();
    }

}
package net.livesplitintegration;

import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;

import java.util.ArrayList;
import java.util.Collection;

public class SrIGTMenu implements SpeedRunIGTApi {

    @Override
    public Collection<OptionButtonFactory> createOptionButtons() {
        ArrayList<OptionButtonFactory> factories = new ArrayList<>();

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20,
                                getConnectionText(),
                                (button) -> {
                                    LivesplitController controller = LivesplitIntegration.get().getLiveSplitController();
                                    if (controller.isConnected()) {
                                        controller.disconnect();
                                    } else {
                                        controller.connect();
                                    }
                                    button.message = getConnectionText();
                                })
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20,
                                "Reload Splits",
                                (button) -> LivesplitIntegration.get().reloadSplits())
                )
                .setCategory("speedrunigt.option.category.general")
        );

        return factories;
    }

    private static String getConnectionText() {
        LivesplitIntegration instance = LivesplitIntegration.get();
        if (instance == null) return "Connect to Livesplit";
        return instance.getLiveSplitController().isConnected()
                ? "Disconnect from Livesplit"
                : "Connect to Livesplit";
    }
}

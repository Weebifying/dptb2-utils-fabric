package weebify.dptb2utils.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ButtonTimerManager {
    public static int buttonTimer = -1;

    public static boolean isMayhem;
    public static boolean isDisabled;
    public static boolean isChaos;
    public static int chaosCounter = 0;

    public static Text tickToTime(int ticks) {
        if (ticks < 0) {
            return Text.of("N/A");
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeString = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
        MutableText timeText = Text.literal(timeString);
        if (ticks >= 230) isChaos = false;

        if (isMayhem) return timeText.formatted(Formatting.RED);
        if (isChaos) {
            if (ticks >= 140) return timeText.formatted(Formatting.DARK_PURPLE);
            if (ticks >= 120) return timeText.formatted(Formatting.LIGHT_PURPLE);
            if (ticks >= 100) return timeText.formatted(Formatting.DARK_AQUA);
        }
        if (isDisabled) return timeText;

        if (ticks >= 300) return timeText.formatted(Formatting.RED);
        else if (ticks >= 240) return timeText.formatted(Formatting.GOLD);
        else if (ticks >= 200) return timeText.formatted(Formatting.YELLOW);
        return timeText;
    }
}

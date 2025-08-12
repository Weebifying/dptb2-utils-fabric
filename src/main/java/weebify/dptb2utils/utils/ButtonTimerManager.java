package weebify.dptb2utils.utils;

public class ButtonTimerManager {
    public static int buttonTimer = -1;

    public static boolean isMayhem;
    public static boolean isDisabled;
    public static boolean isChaos;
    public static int chaosCounter = 0;

    public static String tickToTime(int ticks) {
        if (ticks < 0) {
            return "N/A";
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeText = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
        if (ticks >= 230) isChaos = false;

        if (isMayhem) return "§c" + timeText;
        if (isChaos) {
            if (ticks >= 140) return "§5" + timeText;
            if (ticks >= 120) return "§d" + timeText;
            if (ticks >= 100) return "§3" + timeText;
        }
        if (isDisabled) return timeText;

        if (ticks >= 300) return "§c" + timeText;
        else if (ticks >= 240) return "§6" + timeText;
        else if (ticks >= 200) return "§e" + timeText;
        return timeText;
    }
}

package weebify.dptb2utils.utils;

public class DelayedTask {
    private int ticksLeft;
    private final Runnable task;

    public DelayedTask(int delayTicks, Runnable task) {
        this.ticksLeft = delayTicks;
        this.task = task;
    }

    public boolean tick() {
        if (--ticksLeft <= 0) {
            task.run();
            return true;
        }
        return false;
    }
}

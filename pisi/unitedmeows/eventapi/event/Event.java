package pisi.unitedmeows.eventapi.event;


public class Event {

    private boolean async;
    private boolean canceled;
    private boolean stopped;

    public enum Time {
        BEFORE,
        ON,
        AFTER
    }

    public enum Weight {
        MASTER(10),
        HIGHEST(5),
        MEDIUM(3),
        LOW(2),
        LOWEST(1),
        SLAVE(-9),
        MONITOR(-10);

        private int weight;
        Weight(int _weight) {
            weight = _weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isAsync() {
        return async;
    }

    public void stop() {
        stopped = true;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isStopped() {
        return stopped;
    }
}

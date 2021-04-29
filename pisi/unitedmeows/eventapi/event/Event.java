package pisi.unitedmeows.eventapi.event;


public class Event {

    private boolean canceled;
    protected Type type = Type.SYNC;
    private boolean stopped;

    public enum Time {
        BEFORE,
        ON,
        AFTER
    }

    public enum Type {
        ASYNC,
        SYNC,
        AWAIT_ASYNC,
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }


    public void stop() {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }
}

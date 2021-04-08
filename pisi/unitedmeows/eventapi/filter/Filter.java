package pisi.unitedmeows.eventapi.filter;

public interface Filter<X> {
    public boolean check(X event);
}

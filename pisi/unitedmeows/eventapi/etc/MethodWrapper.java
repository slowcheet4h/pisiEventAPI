package pisi.unitedmeows.eventapi.etc;


import pisi.unitedmeows.eventapi.Listener;
import pisi.unitedmeows.eventapi.filter.Filter;

import java.lang.reflect.Method;

public class MethodWrapper {

    public final Object source;
    public final Listener listener;
    public final Method target;
    private Filter filter;
    public boolean paused;

    public MethodWrapper(Object _source, Method _target, Listener _listener, Filter _filter)
    {
        listener = _listener;
        filter = _filter;
        source = _source;
        target = _target;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}

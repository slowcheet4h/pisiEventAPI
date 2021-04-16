package pisi.unitedmeows.eventapi.etc;


import pisi.unitedmeows.eventapi.Listener;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.filter.Filter;

import java.lang.reflect.Method;

public abstract class ActionWrapper implements IActionWrapper<Event> {

    public String label;
    public Class<? extends Event>[] events;
    public Event.Weight weight;
    public boolean ignoreCanceled;
    public boolean autoRegister;

    private Filter filter;
    public boolean paused;
    private Object source;

    public ActionWrapper(Object _source, String _label, Class<? extends Event>[] _events, Event.Weight _weight, boolean _autoRegister, boolean _ignoreCanceled, Filter _filter)
    {
        source = _source;
        filter = _filter;
        label = _label;
        events = _events;
        weight = _weight;
        autoRegister = _autoRegister;
        ignoreCanceled = _ignoreCanceled;
    }


    public boolean autoRegister() {
        return autoRegister;
    }

    public boolean ignoreCanceled() {
        return ignoreCanceled;
    }

    public Event.Weight weight() {
        return weight;
    }

    public Class<? extends Event>[] events() {
        return events;
    }

    public String label() {
        return label;
    }

    public Object source() {
        return source;
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

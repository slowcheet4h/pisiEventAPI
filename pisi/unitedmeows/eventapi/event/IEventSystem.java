package pisi.unitedmeows.eventapi.event;


import pisi.unitedmeows.eventapi.filter.Filter;

public interface IEventSystem {

    void register(Object source, Filter filter, String label);
    void register(Object source, String label);
    void unregister(String label);
    void unregisterAll(Object o);
    void registerAll(Object o);
    void fire(Event event);
    void __setup();
    boolean setFilter(String listenerName, Class<? extends Event> event, Filter filter);
    void close();
}

package pisi.unitedmeows.eventapi.system;

import pisi.unitedmeows.eventapi.Listener;
import pisi.unitedmeows.eventapi.etc.MethodWrapper;
import pisi.unitedmeows.eventapi.etc.Tuple;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.IEventSystem;
import pisi.unitedmeows.eventapi.filter.Filter;
import static pisi.unitedmeows.meowlib.async.Async.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class DefaultEventSystem implements IEventSystem {

    public static Filter NO_FILTER = new Filter() {
        @Override
        public boolean check(Object event) {
            return false;
        }
    };


    public DefaultEventSystem() {
        setup();
    }

    public void registerEvent(Class<? extends Event> eventClass) {
        registeredEvents.put(eventClass, new CopyOnWriteArrayList<>());
    }


    public abstract void setup();

    public boolean setFilter(String listenerName, Class<? extends Event> event, Filter filter) {
        final MethodWrapper wrapper = getListener(event, listenerName);

        if (wrapper == null) {
            return false;
        }

        wrapper.setFilter(filter);
        return true;
    }

    public void registerAll(Object o) {
        for (Method method : o.getClass().getDeclaredMethods()) {
            Listener listener = method.getAnnotation(Listener.class);
            if (listener != null) {
                if (listener.autoRegister()) {
                    MethodWrapper wrapper = new MethodWrapper(o, method, listener, NO_FILTER);
                    for (Class<? extends Event> eventType : listener.events()) {
                        registeredEvents.get(eventType).add(wrapper);
                    }
                }
            }
        }
    }

    public void unregisterAll(Object o) {
        for (List<MethodWrapper> wrapperList : registeredEvents.values()) {
            wrapperList.removeIf(x-> x.source.getClass() == o.getClass());
        }
    }


    public void fire(Event event) {
        for (MethodWrapper wrapper : registeredEvents.get(event.getClass())) {
            if (!wrapper.isPaused()) {

                if (event.isCanceled() && wrapper.listener.ignoreCanceled()) {
                    continue;
                }


                if (wrapper.getFilter() == NO_FILTER || !wrapper.getFilter().check(event)) {

                    if (event.isAsync()) {
                        async((u)-> {
                            try {
                                wrapper.target.invoke(wrapper.source, event);
                            } catch (Exception ex) { }
                        });
                    } else {
                        try {
                            wrapper.target.invoke(wrapper.source, event);
                        } catch (IllegalAccessException | InvocationTargetException ex) { }
                    }

                    if (event.isStopped()) {
                        break;
                    }

                }
            }
        }
    }



    public MethodWrapper getListener(Class<? extends Event> event, String listenerName) {
        for (MethodWrapper wrapper : registeredEvents.get(event)) {
            if (wrapper.listener.label().equals(listenerName)) {
                return wrapper;
            }
        }
        return null;
    }



    public void unregister(String label) {
        for (List<MethodWrapper> wrapperList : registeredEvents.values()) {
            wrapperList.removeIf(x -> x.listener.label().equals(label));
        }
    }

    @Override
    public void register(Object source, String label) {
        register(source, NO_FILTER, label);
    }

    public void register(Object source, Filter filter, String label) {
        Tuple<Method, Listener> values = getEventMethod(source.getClass(), label);
        MethodWrapper wrapper = new MethodWrapper(source, values.getFirst(), values.getSecond(), filter);

        for (Class<? extends Event> event : values.getSecond().events()) {
            registeredEvents.get(event).add(wrapper);

            registeredEvents.get(event).sort(new Comparator<MethodWrapper>() {
                @Override
                public int compare(MethodWrapper o1, MethodWrapper o2) {
                    return o2.listener.weight().getWeight() - o1.listener.weight().getWeight();
                }
            });
        }
    }

    public Tuple<Method, Listener> getEventMethod(Class clazz, String label) {
        for (Method method : clazz.getDeclaredMethods()) {
            Listener listener = method.getAnnotation(Listener.class);
            if (listener != null && listener.label().equals(label)) {
                return new Tuple<>(method, listener);
            }
        }
        return null;
    }

    @Override
    public void close() {
        registeredEvents.clear();
    }

    private HashMap<Class<? extends Event>, CopyOnWriteArrayList<MethodWrapper>> registeredEvents = new HashMap<>();



}

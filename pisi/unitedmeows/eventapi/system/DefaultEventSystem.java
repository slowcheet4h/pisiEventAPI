package pisi.unitedmeows.eventapi.system;

import pisi.unitedmeows.eventapi.Listener;
import pisi.unitedmeows.eventapi.etc.IActionWrapper;
import pisi.unitedmeows.eventapi.etc.ActionWrapper;
import pisi.unitedmeows.eventapi.etc.MethodWrapper;
import pisi.unitedmeows.eventapi.etc.Tuple;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.IEventSystem;
import pisi.unitedmeows.eventapi.filter.Filter;
import static pisi.unitedmeows.meowlib.async.Async.*;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
        final ActionWrapper wrapper = getListener(event, listenerName);

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
                    if (listener.events().length > 0) {
                        for (Class<? extends Event> eventType : listener.events()) {
                            registeredEvents.get(eventType).add(wrapper);
                            registeredEvents.get(eventType).sort(new Comparator<ActionWrapper>() {
                                @Override
                                public int compare(ActionWrapper o1, ActionWrapper o2) {
                                    return o2.weight().getWeight() - o1.weight().getWeight();
                                }
                            });
                        }
                    } else if (wrapper.getMethod().getParameterTypes().length > 0){
                        Class<? extends Event> event = (Class<? extends Event>) wrapper.getMethod().getParameterTypes()[0];

                        registeredEvents.get(event).add(wrapper);

                        registeredEvents.get(event).sort(new Comparator<ActionWrapper>() {
                            @Override
                            public int compare(ActionWrapper o1, ActionWrapper o2) {
                                return o2.weight().getWeight() - o1.weight().getWeight();
                            }
                        });
                    }
                }
            }
        }
    }

    public void unregisterAll(Object o) {
        for (List<ActionWrapper> wrapperList : registeredEvents.values()) {
            wrapperList.removeIf(x-> x.source().getClass() == o.getClass());
        }
    }


    public void fire(Event event) {
        for (ActionWrapper wrapper : registeredEvents.get(event.getClass())) {
            if (!wrapper.isPaused()) {

                if (event.isCanceled() && wrapper.ignoreCanceled()) {
                    continue;
                }


                if (wrapper.getFilter() == NO_FILTER || !wrapper.getFilter().check(event)) {

                    if (event.isAsync()) {
                        async((u)-> wrapper.run(event));
                    } else {
                        wrapper.run(event);
                    }

                    if (event.isStopped()) {
                        break;
                    }

                }
            }
        }
    }



    public ActionWrapper getListener(Class<? extends Event> event, String listenerName) {
        for (ActionWrapper wrapper : registeredEvents.get(event)) {
            if (wrapper.label().equals(listenerName)) {
                return wrapper;
            }
        }
        return null;
    }



    public void unregister(String label) {
        for (List<ActionWrapper> wrapperList : registeredEvents.values()) {
            wrapperList.removeIf(x -> x.label().equals(label));
        }
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event>[] events, Filter<?> filter, final String label) {
        ActionWrapper actionWrapper = new ActionWrapper(null, label, events,
                Event.Weight.MEDIUM, true, false, filter) {
            @Override
            public void run(Event event) {
                action.run(event);
            }
        };
        for (Class<? extends Event> event : events) {
            registeredEvents.get(event).add(actionWrapper);
            registeredEvents.get(event).sort(new Comparator<ActionWrapper>() {
                @Override
                public int compare(ActionWrapper o1, ActionWrapper o2) {
                    return o2.weight().getWeight() - o1.weight().getWeight();
                }
            });
        }
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event> _event, Filter<?> filter, final String label) {
        ActionWrapper actionWrapper = new ActionWrapper(null, label, new Class[]{_event},
                Event.Weight.MEDIUM, true, false, filter) {
            @Override
            public void run(Event event) {
                action.run(event);
            }
        };
        registeredEvents.get(_event).add(actionWrapper);
        registeredEvents.get(_event).sort(new Comparator<ActionWrapper>() {
            @Override
            public int compare(ActionWrapper o1, ActionWrapper o2) {
                return o2.weight().getWeight() - o1.weight().getWeight();
            }
        });
    }



    public void on(IActionWrapper<Event> action, Class<? extends Event> _event, Filter<?> filter) {
        on(action, _event, filter, "empty_on");
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event> _event) {
        on(action, _event, NO_FILTER, "empty_on");
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event> _event, String _label) {
        on(action, _event, NO_FILTER, _label);
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event> _events[], Filter<?> filter) {
        on(action, _events, filter, "empty_on");
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event> _events[]) {
        on(action, _events, NO_FILTER, "empty_on");
    }
    public void on(IActionWrapper<Event> action, Class<? extends Event> _events[], final String _label) {
        on(action, _events, NO_FILTER, _label);
    }



    @Override
    public void register(Object source, String label) {
        register(source, NO_FILTER, label);
    }

    public void register(Object source, Filter filter, String label) {
        Tuple<Method, Listener> values = getEventMethod(source.getClass(), label);
        MethodWrapper wrapper = new MethodWrapper(source, values.getFirst(), values.getSecond(), filter);
        if (values.getSecond().events().length > 0) {
            for (Class<? extends Event> event : values.getSecond().events()) {
                registeredEvents.get(event).add(wrapper);

                registeredEvents.get(event).sort(new Comparator<ActionWrapper>() {
                    @Override
                    public int compare(ActionWrapper o1, ActionWrapper o2) {
                        return o2.weight().getWeight() - o1.weight().getWeight();
                    }
                });
            }
        } else if (wrapper.getMethod().getParameterTypes().length > 0){
            Class<? extends Event> event = (Class<? extends Event>) wrapper.getMethod().getParameterTypes()[0];

            registeredEvents.get(event).add(wrapper);

            registeredEvents.get(event).sort(new Comparator<ActionWrapper>() {
                @Override
                public int compare(ActionWrapper o1, ActionWrapper o2) {
                    return o2.weight().getWeight() - o1.weight().getWeight();
                }
            });
        }
    }


    public Tuple<Method, Listener> getEventMethod(Class<?> clazz, String label) {
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

    private HashMap<Class<? extends Event>, CopyOnWriteArrayList<ActionWrapper>> registeredEvents = new HashMap<>();

}
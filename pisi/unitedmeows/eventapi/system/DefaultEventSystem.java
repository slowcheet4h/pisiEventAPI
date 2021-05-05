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
            if (!method.isAccessible()) {
                method.setAccessible(true); // thanks to ipana for this bug fix
            }
            Listener listener = method.getAnnotation(Listener.class);
            if (listener != null) {
                if (listener.autoRegister()) {
                    MethodWrapper wrapper = new MethodWrapper(o, method, listener, null);
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


    public void setPaused(Object instance, String listenerName, boolean state) {
        for (CopyOnWriteArrayList<ActionWrapper> value : registeredEvents.values()) {
            for (ActionWrapper actionWrapper : value) {
                if (actionWrapper.source() == instance && actionWrapper.label.equalsIgnoreCase(listenerName)) {
                    actionWrapper.setPaused(state);
                }
            }
        }
    }
    public void setPaused(String listenerName, boolean state) {
        for (CopyOnWriteArrayList<ActionWrapper> value : registeredEvents.values()) {
            for (ActionWrapper actionWrapper : value) {
                if (actionWrapper.label.equalsIgnoreCase(listenerName)) {
                    actionWrapper.setPaused(state);
                }
            }
        }
    }

    /** requires meowlib
     * sets pause state to 'state' arg for 'time'
     * then sets back old value
     * **/
    public void setPauseStateFor(Object instance, String listenerName, boolean state, long time) {
        HashMap<ActionWrapper, Boolean> states = new HashMap<>();
        for (CopyOnWriteArrayList<ActionWrapper> value : registeredEvents.values()) {
            for (ActionWrapper actionWrapper : value) {
                if (actionWrapper.source() == instance && actionWrapper.label.equalsIgnoreCase(listenerName)) {
                    states.put(actionWrapper, actionWrapper.isPaused());
                    actionWrapper.setPaused(state);
                }
            }
        }

        async_w(u -> {
            states.forEach((actionWrapper, oldState) -> actionWrapper.setPaused(oldState.booleanValue()));
        }, time);
    }

    /** requires meowlib
     * sets pause state to 'state' arg for 'time'
     * then sets back old value
     * (no instance check (if there is multiple instances of same listener all would be affected))
     * **/
    public void setPauseStateFor(String listenerName, boolean state, long time) {
        HashMap<ActionWrapper, Boolean> states = new HashMap<>();
        for (CopyOnWriteArrayList<ActionWrapper> value : registeredEvents.values()) {
            for (ActionWrapper actionWrapper : value) {
                if (actionWrapper.label.equalsIgnoreCase(listenerName)) {
                    states.put(actionWrapper, actionWrapper.isPaused());
                    actionWrapper.setPaused(state);
                }
            }
        }

        async_w(u -> {
            states.forEach((actionWrapper, oldState) -> actionWrapper.setPaused(oldState.booleanValue()));
        }, time);
    }

    public void setPausedFor(String listenerName, long time) {
       setPaused(listenerName, true);
        async_w((u-> {
            setPaused(listenerName, false);
        }), time);
    }

    public void setPausedFor(Object instance, String listenerName, long time) {
       setPaused(instance, listenerName, true);
        async_w((u-> {
            setPaused(instance, listenerName, false);
        }), time);
    }
    public void setEnabledFor(String listenerName, long time) {
       setPaused(listenerName, false);
        async_w((u-> {
            setPaused(listenerName, true);
        }), time);
    }

    public void setEnabledFor(Object instance, String listenerName, long time) {
       setPaused(instance, listenerName, false);
        async_w((u-> {
            setPaused(instance, listenerName, true);
        }), time);
    }

    public void fire(Event event) {
        for (ActionWrapper wrapper : registeredEvents.get(event.getClass())) {
            if (!wrapper.isPaused()) {

                if (event.isCanceled() && wrapper.ignoreCanceled()) {
                    continue;
                }


                if (wrapper.getFilter() == null || wrapper.getFilter() == NO_FILTER || !wrapper.getFilter().check(event)) {

                    switch (event.getType()) {
                        case SYNC: {
                            wrapper.run(event);
                            break;
                        }
                        case ASYNC: {
                            async((u) -> wrapper.run(event));
                            break;
                        }
                        case AWAIT_ASYNC: {
                            await(async((u) -> wrapper.run(event)));
                            break;
                        }
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
        on(action, _event, null, "empty_on");
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event> _event, String _label) {
        on(action, _event, null, _label);
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event> _events[], Filter<?> filter) {
        on(action, _events, filter, "empty_on");
    }

    public void on(IActionWrapper<Event> action, Class<? extends Event> _events[]) {
        on(action, _events, null, "empty_on");
    }
    public void on(IActionWrapper<Event> action, Class<? extends Event> _events[], final String _label) {
        on(action, _events, null, _label);
    }



    @Override
    public void register(Object source, String label) {
        register(source, null, label);
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
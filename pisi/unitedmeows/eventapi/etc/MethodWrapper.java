package pisi.unitedmeows.eventapi.etc;

import pisi.unitedmeows.eventapi.Listener;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.filter.Filter;

import java.lang.reflect.Method;

public class MethodWrapper extends ActionWrapper
{

    private Method method;

    public MethodWrapper(Object o, Method method, Listener listener, Filter filter) {
        super(o, listener.label(), listener.events(), listener.weight(), listener.autoRegister(), listener.ignoreCanceled()
        , filter);
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public void run(Event event) {
        try {
            method.invoke(source(), event);
        } catch (Exception ex) {}
    }
}

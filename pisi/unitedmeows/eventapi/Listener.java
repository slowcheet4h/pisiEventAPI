package pisi.unitedmeows.eventapi;


import pisi.unitedmeows.eventapi.event.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {
    public String label() default "empty_event";
    public Class<? extends Event>[] events() default {};
    public Event.Weight weight() default Event.Weight.MEDIUM;
    public boolean ignoreCanceled() default false;
    public boolean autoRegister() default true;
}

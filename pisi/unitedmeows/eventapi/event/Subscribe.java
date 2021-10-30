package pisi.unitedmeows.eventapi.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Subscribe {
	public Event.Weight weight() default Event.Weight.MEDIUM;
	public Class<? extends Event>[] events() default {};
	public boolean ignoreCanceled() default false;
	public boolean autoRegister() default true;
}

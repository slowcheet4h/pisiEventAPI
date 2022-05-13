package pisi.unitedmeows.eventapi.event.listener;


import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.IFunction;
import pisi.unitedmeows.eventapi.event.utils.TypeResolver;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

public class Listener<X extends Event> {

	protected ArrayList<Predicate<X>> filters;

	protected IFunction<X> function;

	protected List<Class<?>> listeningEvents;


	protected boolean ignoreCanceled;
	protected Event.Weight weight;


	private Class<X> target;

	private Object declaredObject;
	private boolean paused;


	public Listener(IFunction<X> event) {
		this.function = event;
		this.filters = new ArrayList<>(1);
		listeningEvents = new ArrayList<>(1);
		weight = Event.Weight.MEDIUM;
		this.target = (Class<X>) TypeResolver.resolveRawArgument(IFunction.class, function.getClass());
		listen(target);

	}


	private Type getGenericClassType(int index) {
		// To make it use generics without supplying the class type
		Type type = getClass().getGenericSuperclass();

		while (!(type instanceof ParameterizedType)) {
			if (type instanceof ParameterizedType) {
				type = ((Class<?>) ((ParameterizedType) type).getRawType()).getGenericSuperclass();
			} else {
				type = ((Class<?>) type).getGenericSuperclass();
			}
		}

		return ((ParameterizedType) type).getActualTypeArguments()[index];
	}

	public void __setup(Object _declaredObject) {
		declaredObject = _declaredObject;
	}

	public void call(Event event) {
		if (preCheck(event)) {
			if (!filters.isEmpty()) {
				for (Predicate<X> predicate : filters) {
					if (!predicate.test((X) event)) {
						return;
					}
				}
			}

			this.function.call((X) event);
		}
	}


	public boolean preCheck(Event event) {
		return (!ignoreCanceled || !event.isCanceled()) && !paused;
	}

	public Object declared() { return declaredObject; }

	public void stopListening(Class<?> event) {
		int i = 0;
		boolean remove = false;
		for (Class<?> listeningEvent : listeningEvents) {
			if (listeningEvent == event) {
				remove = true;
				break;
			}
			i++;
		}

		if (remove) {
			listeningEvents.remove(i);
		}
	}

	public void setPaused(boolean state) {
		paused = state;
	}

	public Listener<X> pause() {
		paused = true;
		return this;
	}

	public Listener<X> resume() {
		paused = false;
		return this;
	}

	public boolean paused() {
		return paused;
	}

	public boolean isIgnoreCanceled() {
		return ignoreCanceled;
	}

	public List<Class<?>> listeningEvents() {
		return listeningEvents;
	}

	public IFunction<X> function() {
		return function;
	}

	public ArrayList<Predicate<X>> filters() {
		return filters;
	}

	public Listener<X> filter(Predicate<X> filter) {
		filters.add(filter);
		return this;
	}

	public Listener<X> weight(Event.Weight weight) {
		this.weight = weight;
		return this;
	}

	public Listener<X> listen(Class<?>... events) {
		listeningEvents.addAll(Arrays.asList(events));
		return this;
	}

	public Listener<X> ignoreCanceled() {
		ignoreCanceled = true;
		return this;
	}

	public Listener<X> ignoreCanceled(boolean state) {
		ignoreCanceled = state;
		return this;
	}




	public Event.Weight getWeight() {
		return weight;
	}
}

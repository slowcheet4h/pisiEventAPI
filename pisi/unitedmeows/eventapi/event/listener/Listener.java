package pisi.unitedmeows.eventapi.event.listener;


import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.IFunction;
import pisi.unitedmeows.eventapi.event.utils.TypeResolver;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

public class Listener<X extends Event> {

	protected Predicate<X>[] filters;

	protected IFunction<X> function;

	protected List<Class<X>> listeningEvents;

	protected UUID uuid;

	protected boolean ignoreCanceled;
	protected Event.Weight weight;


	private Class<X> target;

	private Object declaredObject;
	protected static volatile Random random = new Random();
	private boolean paused;


	public Listener(IFunction<X> event, Predicate<X>... filters) {
		this.function = event;
		this.filters = filters;
		listeningEvents = new ArrayList<>(1);

		this.target = (Class<X>) TypeResolver.resolveRawArgument(IFunction.class, function.getClass());
		listen(target);

		uuid = new UUID(random.nextLong(), random.nextLong());

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


	public void listen(Class<X> event) {
		listeningEvents.add(event);
	}

	public void __setup(Object _declaredObject, Class<? extends Event>[] _listeningEvents, Event.Weight _weight, boolean _ignoreCanceled) {
		if (_listeningEvents.length != 0) {
			listeningEvents.clear();
			for (Class<? extends Event> event : _listeningEvents) {
				listeningEvents.add((Class<X>) event);
			}
		}
		declaredObject = _declaredObject;
		weight = _weight;
		ignoreCanceled = _ignoreCanceled;
	}

	public void call(Event event) {
		if (preCheck(event)) {
			if (filters.length != 0) {
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
		for (Class<X> listeningEvent : listeningEvents) {
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

	public void pause() {
		paused = true;
	}

	public void resume() {
		paused = false;
	}

	public boolean paused() {
		return paused;
	}

	public boolean ignoreCanceled() {
		return ignoreCanceled;
	}

	public List<Class<X>> listeningEvents() {
		return listeningEvents;
	}

	public IFunction<X> function() {
		return function;
	}

	public Predicate<X>[] filters() {
		return filters;
	}


	public UUID uuid() {
		return uuid;
	}

	public Event.Weight weight() {
		return weight;
	}
}

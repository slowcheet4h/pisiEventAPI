package pisi.unitedmeows.eventapi.system;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;
import pisi.unitedmeows.eventapi.event.Subscribe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class BasicEventSystem implements IEventSystem
{
	@Override
	public void subscribeAll(Object instance) {
		/* find field from class */
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (Listener.class.isAssignableFrom(field.getType())) {

				/* make field accessiable */
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				try {
					/* subscribe listener */
					subscribe(instance, (Listener<?>) field.get(instance), field);
				} catch (IllegalAccessException e) {

				}
			}
		}
	}

	@Override
	public void subscribeAll(Object instance, Listener<?>... listeners) {
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (Listener.class.isAssignableFrom(field.getType())) {

				/* make field accessiable */
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				try {
					/* check if listeners contains */
					Listener<?> listener = (Listener<?>) field.get(instance);
					if (Arrays.stream(listeners).anyMatch(x -> x.uuid() == listener.uuid())) {
						/* subscribe listener */
						subscribe(instance, (Listener<?>) field.get(instance), field);
					}

				} catch (IllegalAccessException e) {

				}
			}
		}
	}


	@Override
	public void subscribe(Listener<?> listener, Object instance) {
		/* find field from class */
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (Listener.class.isAssignableFrom(field.getType())) {

				/* make field accessiable */
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				try {
					/* check if field uuid is same */
					if (((Listener<?>)field.get(instance)).uuid().equals(listener.uuid())) {

						/* subscribe listener */
						subscribe(instance, listener, field);
						break;
					}
				} catch (IllegalAccessException e) {

				}
			}
		}
	}

	@Override
	public void subscribe(Object instance, Listener<?> listener, Field field) {

		final Subscribe subscribe = field.getAnnotation(Subscribe.class);

		/* setup field with annotation values */
		listener.__setup(instance, subscribe.events(), subscribe.weight(), subscribe.ignoreCanceled());

		/* set field accessiable */
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}

		for (Class<?> listeningEvent : listener.listeningEvents()) {
			if (!_LISTENERS.containsKey(listeningEvent)) {
				CopyOnWriteArrayList<Listener<?>> listenerList = new CopyOnWriteArrayList<>();
				listenerList.add(listener);

				_LISTENERS.put(listeningEvent, listenerList);

			} else {
				CopyOnWriteArrayList listeners = _LISTENERS.get(listeningEvent);
				listeners.add(listener);
				listeners.sort(new Comparator<Listener>() {
					@Override
					public int compare(Listener o1, Listener o2) {
						return Integer.compare(o2.weight().value(), o1.weight().value());
					}
				});
			}
		}
	}

	@Override
	public void unsubscribe(Listener listener) {
		for (Object listeningEvent : listener.listeningEvents()) {
			Class<?> clazz = (Class<?>) listeningEvent;
			CopyOnWriteArrayList<Listener<?>> listeners = _LISTENERS.getOrDefault(clazz, null);
			if (listeners != null) {
				listeners.remove(listener);
			}
		}
	}

	@Override
	public void unsubscribeAll(Object listenerClass) {
		for (CopyOnWriteArrayList<Listener<?>> value : _LISTENERS.values()) {
			value.removeIf(x-> x.declared() == listenerClass);
		}
	}

	@Override
	public void fire(Event event) {
		CopyOnWriteArrayList<Listener<?>> listeners = _LISTENERS.getOrDefault(event.getClass(), null);
		if (listeners != null) {
			for (Listener<?> listener : listeners) {
				listener.call( event );

				if (event.isStopped())
					break;
			}
		}
	}

	private HashMap<Class<?>, CopyOnWriteArrayList<Listener<?>>> _LISTENERS = new HashMap<>();
}

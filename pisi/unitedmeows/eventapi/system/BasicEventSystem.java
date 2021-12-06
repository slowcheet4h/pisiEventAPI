package pisi.unitedmeows.eventapi.system;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.lang.reflect.Field;
import java.util.*;

public class BasicEventSystem implements IEventSystem
{
	@Override
	public void subscribeAll(Object instance) {
		/* find field from class */
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (Listener.class.isAssignableFrom(field.getType())) {

				/* make field accessible */
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

				/* make field accessible */
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				try {
					/* check if listeners contains */
					Listener<?> listener = (Listener<?>) field.get(instance);
					if (Arrays.stream(listeners).anyMatch(x -> x == listener)) {
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

				/* make field accessible */
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				try {
					/* check if field uuid is same */
					if (((Listener<?>)field.get(instance)) == listener) {

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
		listener.__setup(instance);

		/* set field accessible */
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}

		for (Class<?> listeningEvent : listener.listeningEvents()) {
			if (!_LISTENERS.containsKey(listeningEvent)) {
				ArrayList<Listener<?>> listenerList = new ArrayList<>();
				listenerList.add(listener);

				_LISTENERS.put(listeningEvent, listenerList);

			} else {
				ArrayList<Listener<?>> listeners = _LISTENERS.get(listeningEvent);
				if (listeners != null) {
					listeners.add(listener);
					try {
						listeners.sort(new Comparator<Listener>() {
							@Override
							public int compare(Listener o1, Listener o2) {
								return Integer.compare(o2.getWeight().value(), o1.getWeight().value());
							}
						});
					} catch (ConcurrentModificationException ex) {

					}
				}
			}
		}
	}

	@Override
	public void unsubscribe(Listener listener) {
		Iterator<Object> listeningEvents = listener.listeningEvents().iterator();

		while (listeningEvents.hasNext()) {
			Class<?> clazz = (Class<?>) listeningEvents.next();
			ArrayList<Listener<?>> listeners = _LISTENERS.getOrDefault(clazz, null);
			if (listeners == null) {
				return;
			}
			Iterator<Listener<?>> iterator = listeners.iterator();
			while (iterator.hasNext()) {
				if (listener == iterator.next()) {
					iterator.remove();
					return;
				}
			}

		}

	}

	@Override
	public void unsubscribeAll(Object listenerClass) {
		for (ArrayList<Listener<?>> value : _LISTENERS.values()) {
			value.removeIf(x-> x.declared() == listenerClass);
		}
	}

	@Override
	public void fire(Event event) {

		ArrayList<Listener<?>> listeners = _LISTENERS.getOrDefault(event.getClass(), null);
		if (listeners == null) {
			return;
		}
		try {
			Iterator<Listener<?>> iterator = listeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().call(event);

				if (event.isStopped()) {
					break;
				}
			}
		} catch (ConcurrentModificationException ex) {}

	}

	private HashMap<Class<?>, ArrayList<Listener<?>>> _LISTENERS = new HashMap<>();
}

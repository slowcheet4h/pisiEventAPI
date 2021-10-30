package pisi.unitedmeows.eventapi.event.listener;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.IFunction;


import java.util.function.Predicate;

public class AsyncListener<X extends Event> extends Listener<X> {

	public AsyncListener(IFunction<X> event, Predicate<X>... filters) {
		super(event, filters);
	}

	@Override
	public void call(Event event) {
		if (preCheck(event)) {
			if (filters.length != 0) {
				for (Predicate<X> predicate : filters) {
					if (!predicate.test((X) event)) {
						return;
					}
				}
			}

			pisi.unitedmeows.meowlib.async.Async.async(f -> {
				this.function.call((X) event);
			});
		}
	}
}

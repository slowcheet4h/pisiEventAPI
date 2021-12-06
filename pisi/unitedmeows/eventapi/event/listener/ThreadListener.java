package pisi.unitedmeows.eventapi.event.listener;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.IFunction;

import java.util.function.Predicate;

public class ThreadListener<X extends Event> extends Listener<X> {

	private Thread thread;

	public ThreadListener(IFunction<X> event, Predicate<X>... filters) {
		super(event);
		for (Predicate<X> _filter : filters) {
			filter(_filter);
		}
	}

	@Override
	public void call(Event event) {
		if (preCheck(event)) {
			if (!filters.isEmpty()) {
				for (Predicate<X> predicate : filters) {
					if (!predicate.test((X) event)) {
						return;
					}
				}
			}


			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					function.call((X) event);
				}
			});


			thread.start();
		}
	}
}
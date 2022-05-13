package test;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;
import pisi.unitedmeows.eventapi.system.BasicEventSystem;

import java.util.HashMap;

public class PisiTest {

	public static BasicEventSystem basicEventSystem = new BasicEventSystem();

	public Listener<PisiTestEvent> listener = new Listener<PisiTestEvent>(event -> {
		/* do something */
		System.out.println("test");
	}).listen(PisiTestEvent2.class, PisiTestEvent.class);





	public Listener<PisiTestEvent> listener2 = new Listener<PisiTestEvent>(event -> {

	}).ignoreCanceled().weight(Event.Weight.MEDIUM);




	public static void main(String[] args) {
		PisiTest pisiTest = new PisiTest();
		basicEventSystem.subscribeAll(pisiTest);
		startWatcher();
		for (int i = 5; i > 0; i--) {
			basicEventSystem.fire(new PisiTestEvent2());
		}
		basicEventSystem.fire(new PisiTestEvent());
		System.out.print("1M call took ");
		stopWatcher();

	}

	public static long time;

	public static void startWatcher() {
		time = System.currentTimeMillis();
	}

	public static void stopWatcher() {
		long lastTime = System.currentTimeMillis();
		System.out.println(lastTime - time + "ms");
	}

}

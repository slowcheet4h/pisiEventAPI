package test;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.IFunction;
import pisi.unitedmeows.eventapi.event.listener.Listener;
import pisi.unitedmeows.eventapi.system.BasicEventSystem;
import pisi.unitedmeows.meowlib.async.Async;
import pisi.unitedmeows.meowlib.thread.kThread;

import java.util.Random;

public class PisiTest {

	public static BasicEventSystem basicEventSystem = new BasicEventSystem();

	public Listener<PisiTestEvent> listener = new Listener<PisiTestEvent>(event -> {

	}).ignoreCanceled().weight(Event.Weight.HIGHEST);


	public Listener<PisiTestEvent> listener2 = new Listener<PisiTestEvent>(event -> {

	}).ignoreCanceled().weight(Event.Weight.MEDIUM);

	public static void main(String[] args) {
		PisiTest pisiTest = new PisiTest();
		basicEventSystem.subscribeAll(pisiTest);
		startWatcher();
		for (int i = 1000000; i > 0; i--) {
			basicEventSystem.fire(new PisiTestEvent());
		}
		stopWatcher();

	}

	public static long time;

	public static void startWatcher() {
		time = System.currentTimeMillis();
	}

	public static void stopWatcher() {
		long lastTime = System.currentTimeMillis();
		System.out.println(lastTime - time + " ms took");
	}

}

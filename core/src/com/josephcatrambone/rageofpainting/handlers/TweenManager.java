package com.josephcatrambone.rageofpainting.handlers;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class TweenManager {
	private static LinkedList <Tween> activeTweens;
	static {
		activeTweens = new LinkedList<Tween>();
	}
	
	/*** addTween
	 * Gradually adjust the value of an object from the 'from' value to the 'to' value over the time specified.
	 * @param object
	 * @param attribute
	 * @param from
	 * @param to
	 * @param time
	 */
	public static void addTween(Object object, String attribute, float from, float to, float time) {
		Class <?> c = object.getClass();
		Tween t = new Tween();
		
		t.obj = object;
		try {
			t.f = c.getField(attribute);
		} catch(NoSuchFieldException nsfe) {
			System.err.println("ERROR: Tried to make tween on " + c.getName() + " class field " + attribute + ".  Tween not created.");
			return;
		}
		
		t.from = from;
		t.to = to;
		t.time = time;
		t.accumulatedTime = 0f;
		t.completed = false;
		
		activeTweens.add(t);
	}
	
	public static void update(float dt) {
		LinkedList <Tween> expiredTweens = new LinkedList <Tween>();
		
		// Update the active tweens and remove the dead ones.
		for(Tween t : activeTweens) {
			if(t.completed) { continue; }
			
			// Add the time delta to the tweens.
			t.accumulatedTime += dt;
			if(t.accumulatedTime > t.time) {
				t.completed = true;
				expiredTweens.push(t);
			} else {
				// If the tween isn't expired, go ahead and calculate the update value.
				float timeFraction = t.accumulatedTime/t.time;
				float setValue = (t.to - t.from)*timeFraction + t.from;
				
				// Then do the actual update with error checking.
				try {
					t.f.setFloat(t.obj, setValue);
				} catch (IllegalArgumentException e) {
					System.err.println("Critical error when processing tween on field " + t.f.getName() + ". Marking complete.");
					e.printStackTrace();
					t.completed = true;
					expiredTweens.push(t);
				} catch (IllegalAccessException e) {
					System.err.println("Critical error when processing tween on field " + t.f.getName() + ". Marking complete.");
					e.printStackTrace();
					t.completed = true;
					expiredTweens.push(t);
				}
			}
		}
		
		// Clear the dead tweens.
		activeTweens.removeAll(expiredTweens);
	}
}

class Tween {
	Object obj; // The actual object.
	Field f; // The field to be changed.
	float from; // The start-value at time 0.
	float to; // The end-value at time TIME.
	float time; // How long it should take to perform.
	float accumulatedTime;
	int behavior;
	boolean completed;
}

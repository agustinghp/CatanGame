package org.skiBums.Observers;

import java.util.ArrayList;
import java.util.List;

public class EventBus implements IGame{
    private static final EventBus INSTANCE = new EventBus();
    private final List<IGameObserver> observers = new ArrayList<>();

    public static EventBus getInstance() {
        return INSTANCE;
    }

    @Override
    public void attach(IGameObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(IGameObserver observer) {
        observers.remove(observer);
    }

    public void postMessage(String message) {
        for (IGameObserver observer: observers){
            observer.update(message);
        }
    }
}

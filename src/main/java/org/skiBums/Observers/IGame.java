package org.skiBums.Observers;

public interface IGame {
    void attach(IGameObserver observer);
    default void detach(IGameObserver observer) {
        EventBus.getInstance().detach(observer);
    }
}
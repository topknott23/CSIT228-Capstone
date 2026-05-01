package doboard.core.util;

import java.util.ArrayList;
import java.util.List;

public class AppEventBus {
    private static AppEventBus instance;
    private List<Observer> observers = new ArrayList<>();

    private AppEventBus() {}

    public static AppEventBus getInstance() {
        if (instance == null) {
            instance = new AppEventBus();
        }
        return instance;
    }

    public void attach(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    public void detach(Observer o) {
        observers.remove(o);
    }

    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}

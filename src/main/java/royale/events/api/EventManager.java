package royale.events.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import royale.events.api.EventHandler;
import royale.events.api.events.Event;
import royale.events.api.events.EventStoppable;
import royale.events.api.types.Priority;

public final class EventManager {
    private static final Map<Class<? extends Event>, List<MethodData>> REGISTRY_MAP = new HashMap<Class<? extends Event>, List<MethodData>>();

    public static void register(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (EventManager.isMethodBad(method)) continue;
            EventManager.register(method, object);
        }
    }

    public static void register(Object object, Class<? extends Event> eventClass) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (EventManager.isMethodBad(method, eventClass)) continue;
            EventManager.register(method, object);
        }
    }

    public static void unregister(Object object) {
        for (List<MethodData> dataList : REGISTRY_MAP.values()) {
            dataList.removeIf(data -> data.source().equals(object));
        }
        EventManager.cleanMap(true);
    }

    public static void unregister(Object object, Class<? extends Event> eventClass) {
        if (REGISTRY_MAP.containsKey(eventClass)) {
            REGISTRY_MAP.get(eventClass).removeIf(data -> data.source().equals(object));
            EventManager.cleanMap(true);
        }
    }

    private static void register(Method method, Object object) {
        Class<? extends Event> indexClass = (Class<? extends Event>) method.getParameterTypes()[0];
        MethodData data = new MethodData(object, method, method.getAnnotation(EventHandler.class).value());
        if (!data.target().canAccess(data.source())) {
            data.target().setAccessible(true);
        }
        if (REGISTRY_MAP.containsKey(indexClass)) {
            if (!REGISTRY_MAP.get(indexClass).contains(data)) {
                REGISTRY_MAP.get(indexClass).add(data);
                EventManager.sortListValue(indexClass);
            }
        } else {
            REGISTRY_MAP.put(indexClass, new CopyOnWriteArrayList());
            REGISTRY_MAP.get(indexClass).add(data);
        }
    }

    public static void removeEntry(Class<? extends Event> indexClass) {
        REGISTRY_MAP.entrySet().removeIf(entry -> ((Class)entry.getKey()).equals(indexClass));
    }

    public static void cleanMap(boolean onlyEmptyEntries) {
        if (onlyEmptyEntries) {
            REGISTRY_MAP.entrySet().removeIf(entry -> ((List)entry.getValue()).isEmpty());
        } else {
            REGISTRY_MAP.clear();
        }
    }

    private static void sortListValue(Class<? extends Event> indexClass) {
        CopyOnWriteArrayList<MethodData> sortedList = new CopyOnWriteArrayList<MethodData>();
        for (byte priority : Priority.VALUE_ARRAY) {
            for (MethodData data : REGISTRY_MAP.get(indexClass)) {
                if (data.priority() != priority) continue;
                sortedList.add(data);
            }
        }
        REGISTRY_MAP.put(indexClass, sortedList);
    }

    private static boolean isMethodBad(Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventHandler.class);
    }

    private static boolean isMethodBad(Method method, Class<? extends Event> eventClass) {
        return EventManager.isMethodBad(method) || !method.getParameterTypes()[0].equals(eventClass);
    }

    public static Event callEvent(Event event) {
        block6: {
            List<MethodData> dataList = REGISTRY_MAP.get(event.getClass());
            if (dataList == null) break block6;
            if (event instanceof EventStoppable) {
                EventStoppable stoppable = (EventStoppable)event;
                for (MethodData data : dataList) {
                    EventManager.invoke(data, event);
                    if (!stoppable.isStopped()) continue;
                    break;
                }
            } else {
                for (MethodData data : dataList) {
                    try {
                        EventManager.invoke(data, event);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return event;
    }

    private static void invoke(MethodData data, Event argument) {
        try {
            data.target().invoke(data.source(), argument);
        }
        catch (IllegalAccessException e) {
            Object errorMessage = "Illegal access to method. ";
            errorMessage = (String)errorMessage + "Method: " + data.target().getName() + ", ";
            errorMessage = (String)errorMessage + "Argument: " + argument.toString() + ", ";
            errorMessage = (String)errorMessage + "Log: " + String.valueOf(e.fillInStackTrace());
            System.out.println((String)errorMessage);
        }
        catch (IllegalArgumentException e) {
            Object errorMessage = "Illegal arguments passed to method. ";
            errorMessage = (String)errorMessage + "Method: " + data.target().getName() + ", ";
            errorMessage = (String)errorMessage + "Argument: " + argument.toString() + ", ";
            errorMessage = (String)errorMessage + "Log: " + String.valueOf(e.getCause());
            System.out.println((String)errorMessage);
        }
        catch (InvocationTargetException e) {
            Object errorMessage = "Exception occurred within invoked method. ";
            errorMessage = (String)errorMessage + "Method: " + data.target().getName() + ", ";
            errorMessage = (String)errorMessage + "Argument: " + argument.toString() + ", ";
            errorMessage = (String)errorMessage + "Log: " + String.valueOf(e.getCause());
            System.out.println((String)errorMessage);
        }
    }

    private record MethodData(Object source, Method target, byte priority) {
    }
}


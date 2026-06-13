package royale.util.math;
import java.util.PriorityQueue;
import royale.modules.module.ModuleStructure;
public class TaskProcessor<T>
{
public int tickCounter;
public PriorityQueue<Task<T>> activeTasks;
public TaskProcessor() {
this.tickCounter = 0;
this.activeTasks = new PriorityQueue<>((r1, r2) -> Integer.compare(r2.priority, r1.priority));
}
public void tick(int deltaTime) {
this.tickCounter += deltaTime;
}
public void addTask(Task<T> task) {
this.activeTasks.removeIf(r -> r.provider.equals(task.provider));
task.expiresIn += this.tickCounter;
this.activeTasks.add(task);
}
public T fetchActiveTaskValue() {
while (!this.activeTasks.isEmpty() && this.activeTasks.peek() != null && (((Task)this.activeTasks.peek()).expiresIn <= this.tickCounter || !((Task)this.activeTasks.peek()).provider.isState())) {
this.activeTasks.poll();
}
if (this.activeTasks.isEmpty())
return null; 
if (this.activeTasks.peek() != null) {
return (T)((Task)this.activeTasks.peek()).value;
}
return null;
}
public static class Task<T> { private int expiresIn;
private final int priority;
public String toString() {
return "TaskProcessor.Task(expiresIn=" + this.expiresIn + ", priority=" + this.priority + ", provider=" + String.valueOf(this.provider) + ", value=" + String.valueOf(this.value) + ")";
} private final ModuleStructure provider; private final T value; public Task(int expiresIn, int priority, ModuleStructure provider, T value) {
this.expiresIn = expiresIn; this.priority = priority; this.provider = provider; this.value = value;
} }
}



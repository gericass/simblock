package SimBlock.localiser;

import java.util.Stack;

import SimBlock.task.Task;


public class TaskExecutor {
	
	private static Stack<Task> taskStack = new Stack<Task>();
	
	public static void runTask(){
		if(taskStack.size() > 0){
			Task currentTask = taskStack.pop();
			currentTask.run();
		}
  }
    
	public static void putTask(Task task){
		taskStack.push(task);
	}

	public static Task getTask() {
		if(taskStack.size() > 0) {
			return taskStack.peek();
		} else {
			return null;
		}
	}
	
}

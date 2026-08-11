
import java.util.*;

public class MyStack<E> {
	Queue<E> q1 = new LinkedList<>();
	Queue<E> q2 = new LinkedList<>();
	
	public void push(E item) {
		q1.offer(item);
	}
	public E pop() {
		while(q1.size()>1) {
			q2.offer(q1.poll());
		}
		E i = q1.poll();
		Queue<E> tmp = q1;
		q1 = q2;
		q2 = tmp;
		return i;
	}
	
}

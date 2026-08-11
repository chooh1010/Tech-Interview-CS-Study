
import java.util.*;

public class MyQueue<E> {
	Stack<E> in = new Stack<>();
	Stack<E> out = new Stack<>();
	
	public void append(E item) {
		in.push(item);
	}
	
	public E poll() {
		if(out.size()==0) {
			while(in.size()>0) {
				out.push(in.pop());
			}
		}
		return out.pop();
	}
}

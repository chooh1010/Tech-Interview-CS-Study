
public class MyLinkedList<E> {
	private Node<E> head;
	private int size;
	
	private static class Node<E> {
		E item;
		Node<E> next;
		Node(E i, Node<E> n){
			item = i;
			next = n;
		}
	}
	
	private Node<E> getNode(int index){
		Node<E> node = head;
		for(int i=0;i<index;i++) {
			node = node.next;
		}
		return node;
	}
	
	public E get(int index) {
		return getNode(index).item;
	}
	
	public void add(int index, E item) {
		if(index==0) {
			head = new Node<>(item, null);
		} else {
			Node<E> node = getNode(index-1);
			Node<E> nextNode = node.next;
			Node<E> newNode = new Node<>(item, nextNode);
			node.next = newNode;
		}
		size++;
	} 
	
	public Node<E> remove(int index){
		Node<E> node = getNode(index);
		Node<E> beforeNode = getNode(index-1);
		beforeNode.next = node.next;
		node.next = null;
		return node;
	}
}

package nl.han.ica.linkedList;

import nl.han.ica.datastructures.IHANStack;

public class HANStack<T> implements IHANStack<T> {
    public HANLinkedList<T> list;

    public HANStack() {
        list = new HANLinkedList<>();
    }

    @Override
    public void push(T value) {
        list.addFirst(value);  // Push is equivalent to adding at the front of the list
    }

    @Override
    public T pop() {
        if (list.getSize() == 0) {
            throw new IllegalStateException("Stack is empty");
        }
        T value = list.getFirst();
        list.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        if (list.getSize() == 0) {
            throw new IllegalStateException("Stack is empty");
        }
        return list.getFirst();  // Peek at the top of the stack (first element in list)
    }

    public int getSize() {
        return list.getSize();  // Optional helper method to get the size of the stack
    }

    public boolean isEmpty() {
        return list.getSize() == 0;  // Optional helper method to check if the stack is empty
    }
}

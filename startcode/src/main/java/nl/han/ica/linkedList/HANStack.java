package nl.han.ica.linkedList;

import nl.han.ica.datastructures.IHANStack;

import java.util.NoSuchElementException;

public class HANStack<T> implements IHANStack {
    private Node<T> top;
    private int size;

    public HANStack() {
        this.top = null;
        this.size = 0;
    }
    @Override
    public void push(Object value) {
        Node<T> newNode = new Node<>((T) value);
        newNode.next = top;
        top = newNode;
        size++;
    }

    @Override
    public T pop() {
        if (top == null) {
            throw new NoSuchElementException("Stack is empty");
        }
        T value = top.data;
        size--;
        return value;
    }

    @Override
    public Object peek() {
        if (top == null) {
            throw new NoSuchElementException("Stack is empty");
        }
        return top.data;
    }
    public int getSize() {
        return size;
    }
    public boolean isEmpty() {
        return size == 0;
    }
}

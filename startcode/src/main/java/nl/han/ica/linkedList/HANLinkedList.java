package nl.han.ica.linkedList;


import nl.han.ica.datastructures.IHANLinkedList;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    // Header node (does not hold actual data)
    private Node<T> header;
    private int size;

    public HANLinkedList() {
        this.header = new Node<>(null, null);  // Header node
        this.size = 0;
    }

    @Override
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value, header.next);
        header.next = newNode;
        size++;
    }

    @Override
    public void clear() {
        header.next = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        Node<T> current = header;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        Node<T> newNode = new Node<>(value, current.next);
        current.next = newNode;
        size++;
    }

    @Override
    public void delete(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        Node<T> current = header;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }

        current.next = current.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        Node<T> current = header.next;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }

        return current.data;
    }

    @Override
    public void removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException("List is empty");
        }
        header.next = header.next.next;
        size--;
    }

    @Override
    public T getFirst() {
        if (size == 0) {
            throw new NoSuchElementException("List is empty");
        }
        return header.next.data;
    }

    @Override
    public int getSize() {
        return size;
    }


    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    // Inner iterator class for traversing the linked list
    private class LinkedListIterator implements Iterator<T> {
        private Node<T> current = header.next;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            T value = current.data;
            current = current.next;
            return value;
        }
    }
}

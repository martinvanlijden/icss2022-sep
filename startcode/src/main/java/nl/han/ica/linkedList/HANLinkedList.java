package nl.han.ica.linkedList;

import nl.han.ica.datastructures.IHANLinkedList;

import java.util.NoSuchElementException;

public class HANLinkedList<T> implements IHANLinkedList {
        Node<T> head;
        private int size;
        public HANLinkedList(){
            head = null;
            size= 0;
    }

    @Override
    public void addFirst(Object value) {
        Node<T> newNode = new Node<T>((T) value);
        newNode.next = head;
        head = newNode;
        size++;
    }

    @Override
    public void clear() {
        head = null;
        size = 0;
    }

    @Override
    public void insert(int index, Object value) {
        checkPos(index, index > size, "Index out of bounds");
        if(index == 0) {
            addFirst(value);
            return;
        }

        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        Node<T> newNode = new Node<T>((T) value);
        newNode.next = current.next;
        current.next = newNode;

        size++;
    }

    @Override
    public void delete(int pos) {
        checkPos(pos, pos >= size, "Position out of bounds");
        if (pos == 0) {
            removeFirst();
            size--;
        }
        Node<T> current = head;
        for (int i = 0; i < pos - 1; i++) {
            current = current.next;
        }
        Node<T> nodeToDelete = current.next;
        current.next = nodeToDelete.next;

        size--;
    }

    @Override
    public Object get(int pos) {
        checkPos(pos, pos >= size, "Position out of bounds");

        Node<T> current = head;

        for (int i = 0; i < pos; i++) {
            current = current.next;
        }
        return current.data;
    }

    @Override
    public void removeFirst() {
        if (head == null) {
            return;
        }
        head=head.next;
        size--;
    }

    @Override
    public Object getFirst() {
            if (head== null){
                throw new NoSuchElementException("this list is empty");
            }
        return head.data;
    }

    @Override
    public int getSize() {
        return size;
    }

    private void checkPos(int pos, boolean pos1, String Position_out_of_bounds) {
        if (pos < 0 || pos1) {
            throw new IndexOutOfBoundsException(Position_out_of_bounds);
        }
    }
}

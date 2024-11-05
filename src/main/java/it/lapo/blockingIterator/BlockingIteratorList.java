package it.lapo.blockingIterator;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class BlockingIteratorList<E> extends AbstractList<E> implements AutoCloseable {

    private final transient ReentrantLock lock = new ReentrantLock();
    private final transient Condition newData = lock.newCondition();
    private final transient AtomicBoolean complete = new AtomicBoolean(false);
    private final List<E> data = Collections.synchronizedList(new ArrayList<E>());

    @Override
    public boolean add(E e) {
        boolean x = super.add(e);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            newData.signalAll();
        } finally {
            lock.unlock();
        }
        return x;
    }

    @Override
    public E get(int index) {
        return data.get(index);
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int cursor = 0;
            @Override
            public boolean hasNext() {
                //TODO broken, because this would give error on last element
                return cursor < size() || !complete.get();
            }
            @Override
            public E next() {
                return get(cursor++);
            }
        };
    }

//    @Override
//    public ListIterator<E> listIterator() {
//        return super.listIterator();
//    }
//
//    @Override
//    public ListIterator<E> listIterator(int index) {
//        return super.listIterator(index);
//    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        super.forEach(action);
    }

    @Override
    public void close() throws Exception {
        complete.set(true);
    }
}

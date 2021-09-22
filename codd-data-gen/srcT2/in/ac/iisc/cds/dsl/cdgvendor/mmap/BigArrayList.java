package in.ac.iisc.cds.dsl.cdgvendor.mmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @cite adapted from https://github.com/ashkrit/blog/tree/master/src/main/java/bigarraylist
 * @author dsladmin
 *
 */
public class BigArrayList<T> implements List<T> {

    private static final String           BACK_FILE_LOCATION   = "/tmp";
    private static final String           BACK_FILENAME_PREFIX = "BigArrayList_";

    private final File                    memoryMappedFile;
    private RandomAccessFile              memoryMappedRAFile;

    private MappedByteBuffer              writerBuffer;
    private MappedByteBuffer              readerBuffer;

    private final BigArrayReaderWriter<T> readerWriter;

    private final int                     noOfMessage;
    private final int                     mask;
    private final long                    bytesInMemory;

    private int                           size                 = 0;
    private long                          currentPosition;
    private long                          startIndex, endIndex;

    public BigArrayList(String listname, BigArrayReaderWriter<T> readerWriter, int expectedCapacity) {
        expectedCapacity = Integer.min(expectedCapacity, Integer.MAX_VALUE / 1000);
        this.noOfMessage = powerOfTwo(expectedCapacity);
        this.mask = expectedCapacity - 1;
        this.bytesInMemory = readerWriter.messageSize() * (long) expectedCapacity;
        this.readerWriter = readerWriter;

        this.memoryMappedFile = new File(BACK_FILE_LOCATION, BACK_FILENAME_PREFIX + listname);
        if (this.memoryMappedFile.exists()) {
            this.memoryMappedFile.delete();
        }
        initRAF();
        allocateWriteBuffer(0);
        allocateReadBuffer(0);
    }

    public void close() {
        this.memoryMappedFile.delete();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    private void initRAF() {
        try {
            this.memoryMappedRAFile = new RandomAccessFile(this.memoryMappedFile, "rw");
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Unable to create RandomAccessFile", ex);
        }
    }

    private void allocateReadBuffer(long pos) throws RuntimeException {
        try {
            this.readerBuffer = memoryMappedRAFile.getChannel().map(FileChannel.MapMode.READ_ONLY, pos, bytesInMemory);
            this.readerBuffer.order(ByteOrder.nativeOrder());
            this.startIndex = pos / readerWriter.messageSize();
            this.endIndex = startIndex + noOfMessage - 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void allocateWriteBuffer(long pos) throws RuntimeException {
        try {
            this.currentPosition = pos;
            this.writerBuffer = memoryMappedRAFile.getChannel().map(FileChannel.MapMode.READ_WRITE, currentPosition, bytesInMemory);

            this.writerBuffer.order(ByteOrder.nativeOrder());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean add(T e) {
        if (writerBuffer.remaining() < readerWriter.messageSize()) {
            allocateWriteBuffer(currentPosition + bytesInMemory);
        }
        readerWriter.write(e, writerBuffer);
        size++;
        return true;
    }

    @Override
    public T set(int index, T e) {
        long oldPos = currentPosition;
        int currPos = writerBuffer.position();
        allocateWriteBuffer(index * readerWriter.messageSize());
        readerWriter.write(e, writerBuffer);
        allocateWriteBuffer(oldPos);
        writerBuffer.position(currPos);
        return e;
    }

    @Override
    public T get(final int index) {
        long pointer = index;
        if (pointer >= startIndex && pointer <= endIndex) {

        } else {
            allocateReadBuffer(pointer * readerWriter.messageSize());
        }
        pointer = index & mask;
        pointer = pointer * readerWriter.messageSize();
        readerBuffer.position((int) pointer);
        T v = readerWriter.read(readerBuffer);
        return v;

    }

    private int powerOfTwo(int x) {
        return 1 << 32 - Integer.numberOfLeadingZeros(x - 1);
    }

    @Override
    public boolean contains(Object o) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public Iterator<T> iterator() {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public Object[] toArray() {
        throw new IllegalArgumentException("Not supported");
    }

    @SuppressWarnings("hiding")
    @Override
    public <T> T[] toArray(T[] a) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public boolean remove(Object o) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public void clear() {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public void add(int index, T element) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public T remove(int index) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public int indexOf(Object o) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new IllegalArgumentException("Not supported");
    }

}

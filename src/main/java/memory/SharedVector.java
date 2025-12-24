package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        readLock();
        try{
            if(index<0 || index>=vector.length)
                throw new IndexOutOfBoundsException("[Sharedvector:Get]: Index out of bounds");
            double output = vector[index];
            return output;
        }
        finally{
            readUnlock();
        }
    }

    public int length() {
        // TODO: return vector length
        readLock();
        try{
            int len = vector.length;
            return len;
        }
        finally{
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        readLock();
        try{
            VectorOrientation orient=orientation;
            return orient;
        }
        finally{
            readUnlock();
        }
    }

    public void writeLock() {
        // TODO: acquire write lock
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        lock.readLock().unlock();
    }

    public void transpose() {
        // TODO: transpose vector
        writeLock();
        try{
           VectorOrientation opposite = (this.orientation == VectorOrientation.ROW_MAJOR) ? VectorOrientation.COLUMN_MAJOR : VectorOrientation.ROW_MAJOR;      
            this.orientation=opposite;
        }
        finally{
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        if (this==other){   
            throw new IllegalArgumentException("[Add]: Cannot add a vector to itself");                              
        }
        int h1 = System.identityHashCode(this);
        int h2 = System.identityHashCode(other);
        if (h1>h2) {
            other.readLock();
            writeLock();
        }
        else {
            writeLock();
            other.readLock();
        }
        try{
            if (this.vector.length != other.vector.length) {
                throw new IllegalArgumentException("[Add]: Vectors must be of the same length");
            }
            if (orientation != other.orientation) {
                throw new IllegalArgumentException("[Add]: Vectors must have the same orientation");
            }
            for (int i = 0; i < vector.length; i++) {
                vector[i] += other.vector[i];
            }
        }
        finally{
            writeUnlock();
            other.readUnlock();
        }
    }

    public void negate() {
        // TODO: negate vector
        writeLock();
        try{
            for (int i = 0; i < vector.length; i++) {
                vector[i] = -1*vector[i];
            }
        }
        finally{
            writeUnlock();
        }
    }

    public double dot(SharedVector other) { //Note: Might cause deadlock if a*b and b*a are executed at the same time, yet-
        double result=0;                        //This situation wont happen in LAE implementation
        if (this==other){                            
          throw new IllegalArgumentException("[dot]: Cannot multiply vectors with same Orientation");                             
        }  
        other.readLock();
        readLock();
        try{
            if(vector.length!=other.vector.length){
                throw new IllegalArgumentException("[dot]: Cannot multiply vectors with different sizes");
            }
            if(orientation==other.orientation){
                throw new IllegalArgumentException("[dot]: Cannot multiply vectors with same orientations");
            }
            for(int i=0;i<vector.length;i++){
                result += vector[i]*other.vector[i];
            }
            return result;
        }
        finally{
            other.readUnlock();
            readUnlock();
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
         writeLock();
        try{
            if(orientation==VectorOrientation.COLUMN_MAJOR){           
                throw new IllegalArgumentException("[VecMatMul]: vector orientation must be row");
        }
            if(matrix.get(0).orientation==VectorOrientation.ROW_MAJOR){
                if(matrix.length()!=vector.length){
                    throw new IllegalArgumentException("[VecMatMul]: Matrix length doesnt fit vector length");
                }
                double[] res=new double[matrix.get(0).length()];
                for(int i=0;i<matrix.length();i++){
                    matrix.get(i).readLock();               
                    try{
                        for(int j=0; j<matrix.get(i).length();j++){
                            res[j]+=vector[i]*matrix.get(i).get(j);
                        } 
                    }
                    finally{
                        matrix.get(i).readUnlock();
                    }
                }
                vector=res;
            }
            else{  //other matrix is column major
                if(matrix.get(0).length()!=vector.length){
                    throw new IllegalArgumentException("[VecMatMul]: matrix columns are not equal to vector's length");
                }
                double[] res=new double[matrix.length()];
                for(int i=0; i<matrix.length();i++){
                    res[i]=this.dot(matrix.get(i));
                }
                vector=res;
            }
        }
        finally{
            writeUnlock();
        }
    }  
}

**Java Multithreading Tutorial**

You will be using Java's threading library in this assignment. In the example below, we show you how to create and spawn threads:

    class MyThread extends Thread { 
        public void run() { 
            // actual method executed by this thread 
        } 
    }
    
    Thread thread = new MyThread(); 
    thread.start();  
    // thread executes its run() method concurrently 
    thread.join()
    
In order to specificy critical regions, you can use the `synchronized` keyword on a Java object. In Java, every object instantiated has a lock associated with it. If multiple threads need to modify the content of a shared Java object, they can do so safely in the following way:

    // sharedObject is some Java object accessible by many threads
    synchronized(sharedObject) {   
        // First Grab lock
        
        // Critical atomic region
        // Safely access and modify field values of sharedObject 
     
        // Release lock
    } 

Of course, as is always the case with multithreaded programming, you might run into serious concurrency bugs if you're not being careful. For example, say we have the following program: 
    
    //Thread Function 1
    synchronized(a) {
        synchronized(b) {
            // function body
        }
    }
   
    //Thread Function 2
    synchronized(b) {
        synchronized(a) {
            // function body
        }
    }
     
and the program spawns the 2 threads. If thread 1 executes `synchronized(a)` and thread 2 executes `synchronized(b)`, then both threads get stuck on the next `synchronized` statement. In other words, they deadlock. 


In multithreaded programming, it's typical for programs to follow a producer-consumer style, where some threads produce data, append it to shared
fixed-size buffer, and some other threads take out data from the queue to process them. However, without proper synchronization, the following issues 
will likely arise: 

  1. Producer threads are inserting more data into an already full buffer
  2. Consumer threads are pulling data from an empty buffer
  

In order to fix the problems above, you will need to use Java's `wait/notify` mechanism (also known as monitors) inside `synchronized{}` blocks. We provide a description for each
  of the functions that you will use in the assignment: 
  
1. `wait()`:

- When a consumer thread calls `wait()`, it's put on a wait queue and the lock is released. 
- Typically used for when a thread needs to wait for a condition to become true: 
      
      while(condition not met) {
        MyThread.wait()
      }

2. `notify()`:
 - When a producer thread puts data into the buffer, it calls `notify()` to wake up one thread from the wait queue
 - Useful to signal a blocked thread when a particular condition has been met 
 
3. `notifyAll()`:
 - Same semantics as `notify()`, but wakes up all threads in the wait queue
 
 
 Here's a simple example with 1 producer and 1 consumer thread: 
 
    LinkedList<Data> MyBuffer;
    
    // Thread 1
    void producer() {
        while(True) {
            Data newData = ProduceData();
            synchronized(MyBuffer) {
                MyBuffer.push(newData);            
                MyBuffer.notify();
            }
        }
    }
        
    // Thread 2
    void consumer() {
        while(True) {
            Data currentData;
            synchronized(MyBuffer) {
                while(MyBuffer.isEmpty()) {
                    MyBuffer.wait();
                }
               currentData = MyBuffer.pop();
            } 
            consumeData(currentData);
        }
    }
      
You're going to find these functions really useful for implementing your thread pool!


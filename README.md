# Assignment 2: Multithreaded Chat Server

**Due FIXME**

**100 points total + 20 extra credit** 

In your second programming assignment, you will use Java to implement the erver component of a simple web-based chat system.  You will have to spawn threads to handle incoming requests in parallel, use locks to protect access to the shared data structures, and use synhronization operations like conditional wait and notify to block asynchronous queries when a new message has arrived.


## Overview ##

The chat system we are asking you to implement has a very simple user interface.  The system supports multiple chat rooms, so to visit the chat room R, users navigate to the web page http://host:port/R/. (If no room is specified, the system will display the chat room named DEFAULT.) When in a chat room, users will be presented with a read-only box containing the most recent messages, a text entry field in which they can enter a message, and a `Send` button. Each time `Send` is pressed, all of the users of the room will immediately see the new message.

Your work on this assignment focuses on the server side of a chat application, so the assignment starter code contains a full implementation of a web-based chat client, written in HTML and JavaScript.  For the curious, the chat client uses [AJAX](https://en.wikipedia.org/wiki/Ajax_(programming)) to make asynchronous requests to your server, and then uses the results from those requests to update the web page.  


## The Protocol ##

Your web server must handle three types of requests: 

1. `GET /R/` – The server returns the contents of `index.html`. 
2. `POST /R/push?msg=M` – The server immediately posts the message `M` to the room `R`.
3. `POST /R/pull?last=N` – The server returns all of the messages from room `R` that have an ID larger than `N`. A response consists of one line per message, with each line of the form (id + ": " + text + "\n"). If no messages are immediately available, the response may be delayed up to 15 seconds while the server waits for additional messages to be posted to the chat room.

Chat rooms are created on demand by visiting new urls in the browser.

## What We Give You ##

__index.html__ – An HTML page with embedded JavaScript that implements the client of the chat protocol. We do not expect you to modify this file at all. We’ve tested this page on current versions of Firefox, Internet Explorer, and Chrome. 

__ChatServer.java__ – A very simple HTTP server. This class opens a server socket, accepts incoming connections, extracts the request, and sends a response. This class handles request of type (1) directly, and implements requests (2) and (3) by calling methods in the `ChatState` class. The starter code is single threaded, and you will be expected to make it multithreaded to complete the assignment. 

__ChatState.java__ – Holds the shared mutable state of a chat room. The state consists of the 32 most recent messages, and a 64-bit ID that is used by the protocol to identify which messages have already been seen. The starter code is single threaded, and you are expected to make it multi-threaded to complete the assignment. 

When you open `index.html`, you will get the following page:


![Image](pa2.png?raw=true)

## Your Task ##

Modify`ChatServer.java` to handle requests concurrently. For each request, the main thread should accept the request in handle(), and then hand off responsibility for processing the request to one of eight worker threads (you should implement a pool of threads to do this).  Once a request has been given to a thread, the thread will carry out the result of processing to completion (and return results to the client). Your server will need to synchronize threads in two ways:

1. Since multiple worker threads will need to read and write from the shared data structure __stateByName__, you will need to protect access to this data.  
2. You will need to think about how you wish to communicate work from the main thread (on which `handle()` is called) to the worker threads in the thread pool. In general it's useful to think about requests going into a work queue, with free worker threads picking up the next item in the queue.
        
You are permitted to use __ONLY__ the following locking primitives, specifically: 

- the `synchronized` keywords
- relevant methods of `java.lang.Object` (i.e. `wait()`, `notify()`, and `notifyAll()`) 
- `java.util.concurrent.Semaphore` 
- `java.util.concurrent.locks.Lock` as implemented by `java.util.concurrent.locks.ReentrantLock`
- `java.util.concurrent.locks.Condition` resulting from calls to `Lock.newCondition()` 

Do *NOT* use any high-level concurrent data structures such as Java’s Concurrent Collections classes, or anything else in `java.util.concurrent` not listed above. 

Modify `ChatState.java` to be thread-safe. The `ChatState.recentMessages()` method should *NOT* call Thread.sleep() and should instead use a proper synchronization method such as `Object.wait()` to wait __up to 15 seconds__ for new messages to arrive before returning. Note that `recentMessages()` should return as soon as new messages arrive; it should not always wait the full 15 seconds as in the starter code. 

Also modify `ChatState.addMessage()` to be thread-safe. Note that `addMessage()` is responsible for waking any blocked calls to `recentMessages()` so that they can return the newly posted messages. 

When blocking a thread for any reason, do not use `Thread.sleep()`, as this degrades responsiveness and is considered a poor concurrency practice. Instead use `Object.wait()` or another similar method to make the thread block properly. 

### Hints ###

By convention, web browsers only permit six simultaneous connections to a single server. Therefore, the chat server will not behave properly if you attempt to open more than six tabs in the same browser. To test with more than six connections, either open multiple browsers, or use multiple browser sessions (e.g. by using the `firefox -P <profile>` command-line parameter). That said, historically few students have encountered bugs with eight connections that were not already apparent with six.

## Environment Setup and Compiling ##

This assignment can be run locally. But first, make sure that Java (at least version 8) is installed. You can follow instructions here: https://www.java.com/en/download/. 

In the `src` directory, we've provided you a Makefile. Simply type `make` in your terminal to compile the 2 Java files (both __ChatState__ and __ChatServer__). 

To run the chat server, invoke __java__ with the base directory in which the compiled code lives (the current directory "." if you just ran `javac`) and the ChatServer class: 
  
    java –cp . ChatServer
    
By default, running `java –cp . ChatServer` will open a server socket on port 8080. The DNS name localhost is bound by convention to a loopback interface on the local machine, so you can get to the chat page world by navigating a browser to http://localhost:8080/world/	
Only one process at a time may bind a service to a port, so you can only run one chat server instance at a time. If the port is not available, __ChatServer__ will exit immediately with a `java.net.BindException: Address already in use`. This might happen because an older instance is still running. This might also occur because another student on the same machine is working on the assignment! On *nix you can check if the port is already bound with the netstat command. (The exact output format will vary.) 

        > netstat -na | grep -w 8080 tcp6       
        0      0 :::8080           :::*               LISTEN 
        
You can pass an optional parameter to __ChatServer__ to request that it bind to another port:

        java –cp . ChatServer 43210 
        
### Working Remotely Through SSH ###

If you aren’t compiling and running ChatServer locally, then you can use SSH port forwarding to allow your local web browser to connect to a remotely running chat server, despite firewalls. For command line SSH clients, add the parameter `-L 8080:localhost:8080` to the ssh invocation, as in: 

    ssh -L 8080:localhost:8080 yoursunetid@cardinal.stanford.edu 
    
This instructs SSH to forward all connections made to port 8080 on the local machine to the address 127.0.0.1 and port 8080 on the remote machine. You can then navigate to http://localhost:8080/world/ on your local machine and be transparently connected to the remote ChatServer instance. 

## Tips on Threading and Synchronization ##

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
    synchronized(sharedObject) {   // grab lock
    
        // Critical atomic region
        // Safely access and modify field values of sharedObject 
        
    } // release lock

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

FIXME (section about monitors, wait, notify)

## Grading ##


### Functional Correctness (50 points) ###

Since the performance of the chat server is limited by network I/O, this assignment will be graded only on correctness. We will test your application with a number of concurrent sessions by opening several browser tabs, typing into each one, and making sure the chat room behaves correctly.

Please note that we will never test your chat server with more than 8 simultaneous connections, but that even with more than 8 simultaneous connections, the server must never drop any connections and should eventually respond to all requests (perhaps with poor performance).

Example functional correctness problems we’re looking for include: 
  - Protocol errors
      - Early empty responses
      - Responses that don’t include the latest data
      - Room contents in a different order for different clients 
      
Note that when testing your chat server, we will never test with more than 8 simultaneous connections.

### Concurrency and Thread-Safety (50 points) ###

Additionally, we will manually audit your code to evaluate your use of synchronization primitives and verify the thread-safety of your algorithms and implementation. Example concurrency problems we’re looking for include: 

- Generic concurrency problems 
  - Unprotected access to mutable data o Incorrect protection 
  - (Potential) deadlocks 
  - Missed wakeups 
  
- Poor concurrency practices 
  - Busy-waiting (a.k.a. spin-waiting)
  - Use of Thread.sleep() 
  - Locks held during I/O 
  - Excessive coarse-grained locking (e.g grabbing a global lock on all chat rooms instead of a lock per chat room)

## Extra Credit ##

FIXME 

## Hand-In Instructions ## 

FIXME 

## Resources and Notes ##

FIXME

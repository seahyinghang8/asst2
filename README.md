# Assignment 2: Multithreaded Chat Server

**Due FIXME**

**100 points total + FIXME extra credit** 

In your second programming assignment, you will write the concurrent server component of a simple web-based AJAX chat system. You will have to spawn threads to handle incoming requests in parallel, use locks to protect access to the shared data structures, and use conditional wait and notify to block asynchronous queries when a new message has arrived.


## Overview ##

The chat system you are to implement has a very simple user interface. To visit the chat room R, users navigate to the web page http://host:port/R/. (If no room is specified, users will visit a chat room named DEFAULT.) There they will be presented with a read-only box containing the most recent messages, a text entry field in which they can enter a message, and a Send button. Each time Send is activated, all of the users of the room will immediately see the new message. AJAX is a popular mechanism for improving the interactivity of web applications. In its most basic form it consists of using JavaScript to perform asynchronous requests to a server, and then using the results of those requests to directly update a portion of the web page, without reloading it. This assignment focuses on the server side of a chat application, so we have already written the HTML and the JavaScript that implements the client. 

## The Protocol ##

Your web server must handle three types of requests: 

1. GET /R/ – The server returns the contents of index.html. 
2. POST /R/push?msg=M – The server immediately posts the message M to the room R.
3. POST /R/pull?last=N – The server returns all of the messages from room R that have an ID larger than N. A response consists of one line per message, with each line of the form (id + ": " + text + "\n"). If no messages are immediately available, the response may be delayed up to 15 seconds while the server waits for additional messages to be posted to the chat room.

Rooms are created on demand.

## What we give you ##

__index.html__ – An HTML page with embedded JavaScript that implements the client of the chat protocol. We do not expect you to modify this file at all. We’ve tested this page on current versions of Firefox, Internet Explorer, and Chrome. 

__ChatServer.java__ – A very simple HTTP server. This class opens a server socket, accepts incoming connections, extracts the request, and sends a response. This class handles request (1) directly, and implements requests (2) and (3) by calling methods in the ChatState class. The starter code is single threaded, and you will be expected to make it multithreaded to complete the assignment. 

__ChatState.java__ – Holds the shared mutable state of a chat room. The state consists of the 32 most recent messages, and a 64-bit ID that is used by the protocol to identify which messages have already been seen. The starter code is single threaded, and you will be expected to make it multithreaded to complete the assignment. 

## Your Task ##

Modify `ChatServer.java` so that it contains a thread pool of 8 threads.  Incoming requests should be handed off to one of the threads in the thread pool to be handled. Add locks or implement concurrent data structures as required to make the chat server thread-safe; there should be no spin-waiting anywhere in your implementation. You are permitted to use __ONLY__ the following locking primitives, specifically: 

- the `synchronized` keywords
- relevant methods of `java.lang.Object` (i.e. `wait()`, `notify()`, and `notifyAll()`) 
- `java.util.concurrent.Semaphore` 
- `java.util.concurrent.locks.Lock` as implemented by `java.util.concurrent.locks.ReentrantLock`
- `java.util.concurrent.locks.Condition` resulting from calls to `Lock.newCondition()` 

Do *NOT* use any high-level concurrent data structures such as Java’s Concurrent Collections classes, or anything else in `java.util.concurrent` not listed above. 

Modify `ChatState.java` to be thread-safe. The `ChatState.recentMessages()` method should *NOT* call Thread.sleep() and should instead use a proper synchronization method such as `Object.wait()` to wait __up to 15 seconds__ for new messages to arrive before returning. Note that `recentMessages()` should return as soon as new messages arrive; it should not always wait the full 15 seconds as in the starter code. 

Also modify `ChatState.addMessage()` to be thread-safe. Note that `addMessage()` is responsible for waking any blocked calls to `recentMessages()` so that they can return the newly posted messages. 

When blocking a thread for any reason, do not use `Thread.sleep()`, as this degrades responsiveness and is considered a poor concurrency practice. Instead use `Object.wait()` or another similar method to make the thread block properly. 

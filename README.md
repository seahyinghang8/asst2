# Assignment 2: Multithreaded Chat Server

**Due FIXME**

**100 points total + FIXME extra credit** 

In your second programming assignment, you will write the concurrent server component of a simple web-based AJAX chat system. You will have to spawn threads to handle incoming requests in parallel, use locks to protect access to the shared data structures, and use conditional wait and notify to block asynchronous queries when a new message has arrived.

** Overview **

The chat system you are to implement has a very simple user interface. To visit the chat room R, users navigate to the web page http://host:port/R/. (If no room is specified, users will visit a chat room named DEFAULT.) There they will be presented with a read-only box containing the most recent messages, a text entry field in which they can enter a message, and a Send button. Each time Send is activated, all of the users of the room will immediately see the new message. AJAX is a popular mechanism for improving the interactivity of web applications. In its most basic form it consists of using JavaScript to perform asynchronous requests to a server, and then using the results of those requests to directly update a portion of the web page, without reloading it. This assignment focuses on the server side of a chat application, so we have already written the HTML and the JavaScript that implements the client. 


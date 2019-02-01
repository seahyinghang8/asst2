Authors: Ying Hang Seah & Lim Jing
SUNetID: yinghang & jinglim2

Explanation of Implementation

To make the server run on multiple threads, we first decided to create 2 classes to manage the multi-threading capabilities: ChatThread and ChatTask. ChatServer and ChatState are then modified to allow for synchronization capabilities by preventing race conditions and deadlocks.

ChatTask is a new class created to hold all the required variables to handle a request. The tasks will be enqueue into a task queue in the server, and the ChatThreads will then dequeue the work one by one.

ChatThread is another new class created to run all the threads that will process the request and return the appropriate response. The initialized threads will first wait till there are task available in the task queue. Once a task is added, one idle thread will be notified and it will handle the task just like how the single-threaded ChatServer would have handled the task. Synchronization of chat states are handled in the ChatState class.

ChatState was modified to be able to handle synchronization. The chat history for each chat room is a shared object that is locked everytime a write is made or a read is made. This is to prevent multiple threads from writing into the same data structure / dirty reading.

ChatServer was modified and to just start up the server at port 8080 and handle any incoming socket connections. Once the connection is established and the request is parsed, ChatServer will create an instance of ChatTask and add it to the task queue. It will then notify any idle ChatThreads to handle the task. If there are no idle threads, the tasks will stay in the queue until some thread becomes free and dequeue the task.

More details of the implementation can be seen in the comments in the code.

# Multi-threaded-WebServer
## Instruction for compiling and executing Web Server:
First, assure all provided files and images are in the same directory. The specified port number is 8080 by default, if you want to change it to another port number just change the static port variable in the beginning of the HTTPWebServer.java file. Do not try to use other html files with this program except for the ones provided. If you do you will have to change the variables in program DEFAULT_FILE and FILE_NOT_FOUND to the ones you intend to use.


## Compilation Instructions:
Compile by entering command: javac HTTPWebServer.java
Run by entering command : java HTTPWebServer

Lastly, once you are done using the program you can use CTRL+C to terminate the program.

## Operation Instructions:
**Input for web browser:**
Below is what you would input into browser to see the webserver working assuming you are using the html files I provided.

http://localhost:8080/index.html

To see the example of a 404 web page just modify the above input by changing the filename to anything must make sure it end with .html

## Known Bugs:
At the random times the current thread running will result in a nullpointerexception, however the program will keep running normally and be able to serve client requests correctly.

Status code 301 is not implemented.

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;


//Reference: https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
//Reference: https://www.geeksforgeeks.org/multithreaded-servers-in-java/
//Class below is a Web Server that will serve clients and multiple requests
//using threads on PORT 8080 . Additionally, the Web Server will respond to the client using 
//only status codes 200 and 404. Also, the Web Server will only respond to GET request from the client.
public class HTTPWebServer implements Runnable
{ 
	//Definitions for html file names and port number
	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final int PORT = 8080;
	
	// verbose mode
	static final boolean verbose = true;
	
	
	private Socket connect;
	
	public HTTPWebServer(Socket c) {
		connect = c;
	}
	
	//Main method that will create Socket on specified Port number and wait and 
	//listen to the client until it recieves request additionally it will start 
	//a thread once the connection has been established with the client to serve
	//the client's request.
	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
			
			// we listen for client until user halts server execution
			while (true) 
			{
				HTTPWebServer myServer = new HTTPWebServer(serverConnect.accept());
				
				if (verbose) 
				{
					System.out.println("Connecton opened. (" + new Date() + ")");
				}
				
				// create thread to manage the client connection
				Thread thread = new Thread(myServer);
				thread.start();
			}
			
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	//Run method used to parse the infromation needed from the request 
	//using I/O Streams and string tokenization. Then the compose the response 
	//using the data fetched from the stream.
	@Override
	public void run() 
	{
		//Data types used to store the data from I/O streams in order
		//compose response
		BufferedReader in = null; 
		PrintWriter out = null; 
		BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			//we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			//we get character output stream to client (for headers)
			out = new PrintWriter(connect.getOutputStream());
			//get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			//Begin by getting the first line of the request which is where the
			//method and filename the client is requesting are. This is accomplished
			//by string tokenizing the first line of the request and storing the 
			//method and filename.			
			String input = in.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); 
			fileRequested = parse.nextToken().toLowerCase();
			
			
				
			File file = new File(WEB_ROOT, fileRequested);
			int fileLength = (int) file.length();
			String content = getContentType(fileRequested);

			//Verify that the method is GET and then begin to compose the response unless
			//the file is not found. In which case the program will throw an exception which
			//is handled.
			if (method.equals("GET")) 
			{ 
				byte[] fileData = readFileData(file, fileLength);
					
				// send HTTP Headers and flush the outputstream
				out.println("HTTP/1.1 200 OK");
				out.println("Server: Elmer Rivera's Java HTTP Server");
				out.println("Date: " + new Date());
				out.println("Content-type: " + content);
				out.println("Content-length: " + fileLength);
				out.println();
				out.flush(); 
					
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
			}
			
			//Print this message to let the user know the hmtl file was sent
			if (verbose) {
				System.out.println("File " + fileRequested + " of type " + content + " returned");
			}
				
			
		//Catch file not found exception and compose 404 status code respone by calling
		//filenotdound function	
		} catch (FileNotFoundException fnfe) {
			try{
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
			
			   System.err.println("Error with file not found exception : " + ioe.getMessage());
			}
			
		}catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				//close all I/O stream datatypes (i.e buffered reader) as well
				//as the socket opened on specified port number
				in.close();
				out.close();
				dataOut.close();
				connect.close(); 
			} catch (Exception e) {
				//Print exception error in case exception is thrown when attempting
				//to close the socket 
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			//Print this message to let the user know the tcp connection was closed with client.
			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}
		
		
	}
	


	//Function that will take the html file and the length as inputs and read the
	//data inside and store in variable of type fileinputstream named filedata. Then the method
	//returns filedata.
	private byte[] readFileData(File file, int fileLength) throws IOException 
	{
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
	
	//Method that takes the name of the requested file and checks what type it
	//is using .endswith() function. Then it returns the content type in the format
	//required by html protocol.
	private String getContentType(String fileRequested) 
	{
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
		{
			return "text/html";
		}
		// type of PNG files
        else if (fileRequested.endsWith(".png")) {
            return "image/png";
        }
		// type of JPEG files, matches jpg and jpeg extensions
        else if (fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg")) 
		{
            return "image/jpeg";
		}
		else
		{
			return "text/plain";
		}
			
	}
	


	//Method that takes the filename and outputstream variables as inputs and compose
	//the 404 status code response to sned to the client.
	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException 
	{
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);
		
		
		// send HTTP Headers and flush the outputstream
		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: Elmer Rivera's Java HTTP Server");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println();
		out.flush(); 
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		//Print this message to let the user know the hmtl file was not found
		if (verbose) 
		{
			System.out.println("File " + fileRequested + " not found");
		}
	}
	
}
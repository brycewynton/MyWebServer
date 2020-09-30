//Bryce Jensen
//09/09/2020
//openjdk 11.0.1 2018-10-16 LTS
//$ javac JokeServer.java
//$ javac JokeClient.java
//$ javac JokeClientAdmin.java
// in separate shells:
//     $ java JokeServer
//     $ java JokeClient
//     $ java JokeClientAdmin
// This program runs across multiple machines.
// If this is the case, please pass the IP  of the server to the client.
// $ java InetServer 140.192.1.22
// $ java InetClient 140.192.1.22
// Files needed to run:
//                   a. checklist.html
//                     b. JokeServer.java
//                     c. JokeClient.java
//                     d. JokeClientAdmin.java
// Notes:
//       Everything was working...
//       now I cant get any jokes to go out to my client!
//          I definitely messed up the mapping of my port numbers and screwed up royally!!!
//          ughhhhhh... I had my client and server talking seamlessly, and the admin was giving me trouble, now only the admin talks
//          at least the admin can change modes... but for nothing I SUPPOSE....


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Date;
import java.util.StringTokenizer;

public class MyWebServer implements Runnable
{
    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_IS_NOT_FOUND = "404.html";
    static final String METHOD_IS_NOT_SUPPORTED = "not_supported.html";
    static final int PORT = 8080;
    // listening port
    static final boolean verbose = true;
    // boolean mode setting for verbose
    private Socket connect;
    // socket to enable client connection

    public MyWebServer(Socket s)
    {
        connect = s;
    }

    public static void main(String args[])
    {
        try
        {
            ServerSocket servconnect = new ServerSocket(PORT);
            System.out.println("Bryce Jensen's Web Server for Clark Elliott's CSC435 Starting up...\nListening for a new connection...\n");

            while(true)
            // Keeps listening until user quits
            {
                MyWebServer bryceServer = new MyWebServer(servconnect.accept());

                if (verbose)
                {
                    System.out.println("New Connection: ("+new Date()+")");
                }

                Thread pid = new Thread(bryceServer);
                // thread to handle all client connectivity
                pid.start();
            }
        } catch (IOException x)
        {
            x.printStackTrace();
            System.err.println("Connection error on Bryce's Web Server: "+x.getMessage());
        }
    }

    @Override
    public void run()
    {
        BufferedReader in = null;
        // declare and initialize new input
        PrintWriter out = null;
        // declare and initialize our output that will write to client
        BufferedOutputStream data_out = null;
        // declare and initialize a way to send data ferom server
        String requestedFilename = null;
        //declare and initialize our client requested filename

        try
        {
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // read client input from socketstream
            out = new PrintWriter(connect.getOutputStream());
            // get client output for our web headers
            data_out = new BufferedOutputStream(connect.getOutputStream());
            // get output for client when they make data requests

            String client_input = in.readLine();
            // first line of user input request get read
            StringTokenizer requestParser = new StringTokenizer(client_input);
            // necessary parsing of client input request
            String method = requestParser.nextToken().toUpperCase();
            // captures the HTTP content as the method within users parsed input request
            requestedFilename = requestParser.nextToken().toLowerCase();
            // captures the filename of file that is being requested

            if (!method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("HEAD"))
            {
                if (verbose)
                {
                    System.out.println("501 has no implementation: "+method+"\n");
                }

                File file = new File(WEB_ROOT, METHOD_IS_NOT_SUPPORTED);
                // send file to user client
                int fileLen = (int) file.length();
                String mimeType_content = "text/html";
                byte[] file_data = readFileData(file, fileLen);
                // send content to user client

                out.println("HTTP/1.1 501 has no current implementation");
                // HTTP headers for the user to see
                out.println("SERVER: Bryce's Web Server for Clark Elliott's CSC435 : v1.0");
                out.println("Date: "+new Date());
                // include the date in our header
                out.println("Content Type: "+mimeType_content);
                // include our mime type
                out.println("Content length: "+ fileLen);
                out.println("");
                // formatting break so user can read the header clearly
                out.flush();
                // flush our buffer out to client user

                data_out.write(file_data, 0,  fileLen);
                // write requested file
                data_out.flush();
                // flush our data buffer
            }
            else
            {
                if (requestedFilename.endsWith("/"))
                // indicates a HEAD or GET function
                {
                    requestedFilename += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, requestedFilename);
                int fileLen = (int) file.length();
                String content = getContentType(requestedFilename);

                if (method.equalsIgnoreCase("GET"))
                // give the user client GET content
                {
                    byte[] file_data = readFileData(file, fileLen);

                    out.println("HTTP/1.1 200 OK");
                    // begin sending the HTTP header
                    out.println("SERVER: Bryce's Web Server for Clark Elliott's CSC435 : v1.0");
                    // include the date in our header
                    out.println("Content Type: "+content);
                    // include our mime type
                    out.println("Content length: "+ fileLen);
                    out.println("");
                    // formatting break so user can read the header clearly
                    out.flush();
                    // flush our buffer out to client user

                    data_out.write(file_data, 0, fileLen);
                    data_out.flush();
                }

                if (verbose)
                {
                    System.out.println("501 has no implementation: "+method+"\n");
                }
            }
        } catch (FileNotFoundException notFoundException)
        {
            fileNotFound(out, data_out, requestedFilename);
        } catch (IOException e)
        {
            System.err.println("Error with Web Server: "+e.getMessage());
        } finally
        {
            try
            {
                in.close();
                out.close();
                data_out.close();
                connect.close();
                // close out all of our connections
            } catch (Exception exception)
            {
                System.err.println("Error while attempting to close streams: "+exception.getMessage());
            }

            if (verbose)
            {
                System.out.println("Connection successfully closed \n");
            }
        }
    }

    private byte[] readFileData(File file, int fileLen) throws IOException
    {
        FileInputStream fileInput = null;
        // declare and initialize our file input to read
        byte[] file_data = new byte[fileLen];

        try
        {
            fileInput = new FileInputStream(file);
            // begin reading
            fileInput.read(file_data);
        } finally
        {
            if (fileInput != null)
            {
                fileInput.close();
            }
        }
        return file_data;
    }

    private String getContentType(String requestedFilename)
    {
        if (requestedFilename.endsWith(".htm") || requestedFilename.endsWith(".html"))
        {
            return "text/html";
        }
        else
        {
            return "text/plain";
        }
    }

    private void fileNotFound(PrintWriter out, OutputStream data_out, String requestedFilename)
    {
        try
        {
            File file = new File(WEB_ROOT, FILE_IS_NOT_FOUND);
            int fileLen = (int) file.length();
            String content = "text/html";
            byte[] file_data = readFileData(file, fileLen);

            out.println("HTTP/1.1 200 OK");
            // begin sending the HTTP header
            out.println("SERVER: Bryce's Web Server for Clark Elliott's CSC435 : v1.0");
            out.println("Date: "+new Date());
            // include the date in our header
            out.println("Content Type: "+content);
            // include our mime type
            out.println("Content length: "+ fileLen);
            out.println("");
            // formatting break so user can read the header clearly
            out.flush();
            // flush our buffer out to client user

            data_out.write(file_data, 0, fileLen);
            data_out.flush();

        } catch (IOException e)
        {
            e.printStackTrace();
        }


        if (verbose)
        {
            System.out.println("File "+requestedFilename+" could not be found...");
        }
    }
}

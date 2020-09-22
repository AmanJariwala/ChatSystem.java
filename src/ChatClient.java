import javax.print.DocFlavor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * This is the client class which will handle making threads for multiple
 * clients and forming a connection between the clients and the server
 *
 * @author Sultan Al-Ali and Aman Jariwala, lab sec 04
 * @version April 26, 2020
 */
final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;
    public boolean loops = true;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.print("Sorry ");
        }

        // Create your input and output streams
        while (true) {
            try {
                try {
                    sInput = new ObjectInputStream(socket.getInputStream());
                    sOutput = new ObjectOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // This thread will listen from the server for incoming messages
                Runnable r = new ListenFromServer();
                Thread t = new Thread(r);
                t.start();

                // After starting, send the clients username to the server.
                try {
                    sOutput.writeObject(username);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            } catch (Exception e) {
                System.out.println("The server is offline");
                break;
            }

        }
        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        Scanner scanner = new Scanner(System.in);
        String username = "Anonymous";
        int portNumber = 1500;
        String serverName = "localhost";
        String recipient = "";

        if (args.length == 3) {
            username = args[0];
            portNumber = Integer.parseInt(args[1]);
            serverName = args[2];
        }

        if (args.length == 2) {
            username = args[0];
            portNumber = Integer.parseInt(args[1]);
        }

        if (args.length == 1) {
            username = args[0];
        }


        // Create your client and start it
        ChatClient client = new ChatClient(serverName, portNumber, username);
        client.start();

        // Send an empty message to the server
        while (client.loops) {
            String message = scanner.nextLine();
            int messageType = 0;

            if (message.contains("/logout")) {
                messageType = 1;
                ChatMessage chatMessage = new ChatMessage(message, messageType, recipient);
                client.sendMessage(chatMessage);
                client.setLoops();
                client.closingMethod();
                break;
            }

            if (message.contains("/list")) {
                messageType = 2;
            }

            if (message.contains("/msg")) {
                messageType = 3;
                String[] elements = message.split("\\s", 3);
                recipient = elements[1];
                message = elements[2];
            }

            ChatMessage chatMessage = new ChatMessage(message, messageType, recipient);
            client.sendMessage(chatMessage);
        }
    }

    /**
     * This method closes the Input/Output streams and the socket in the case the user logs out
     *
     * @author Sultan Al-Ali and Aman Jariwala, lab sec 04
     * @version April 21, 2020
     */
    public void closingMethod() {
        try {
            this.sOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.sInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLoops() {
        loops = false;
    }


    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author Sultan Al-Ali and Aman Jariwala, lab sec 04
     * @version April 21, 2020
     */
    private final class ListenFromServer implements Runnable {
        public void run() {

            while (loops) {
                try {
                    try {
                        String msg = (String) sInput.readObject();
                        System.out.print(msg);
                    } catch (SocketException e) {
                        System.out.println("Good Bye!");
                    } catch (ClassNotFoundException e) {
                        System.out.println("The server is offline");
                    }
                } catch (Exception e) {
                    System.out.println("The server is offline");
                    break;
                }
            }
        }
    }
}
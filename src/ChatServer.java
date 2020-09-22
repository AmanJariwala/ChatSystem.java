import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

/**
 * This is the server that will be used as an intermediate between the clients help
 * send and receive messages while implementing different methods to help in that regard
 *
 * @author Aman Jariwala and Sultan Al-Ali, lab sec 04
 * @version April 26, 2020
 */
final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    public File file;
    final ArrayList<String> abc = new ArrayList<>();


    private ChatServer(int port, File file) {
        this.file = file;
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * broadcast message to all clients
     *
     * @param message
     */
    private synchronized void broadcast(String message) {
        // concurrency manage
        // send msg to all connected client
        // when broadcasting to both server and client add the date and time in message

        java.util.Date date = new java.util.Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String strDate = dateFormat.format(date);

        //appending date as per given format
        message = strDate + " " + message + "\n";
        ListIterator<ClientThread> clientsIterator = clients.listIterator();
        while (clientsIterator.hasNext()) {
            clientsIterator.next().writeMessage(message);
        }
    }

    /**
     * to remove the client from our list
     *
     * @param id
     */
    private synchronized void remove(int id) {
        // removing received client (through) id from listArray
        // handle concurrency

        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).id == id) {
                clients.remove(i);
                break;
            }
        }
    }

    /**
     * method to close
     * depends on code written in ChatClient
     */

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {

        int port = 1500;
        File file = new File("BadWords.txt");

        try {
            System.out.println("The list of bad words are:");

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String badWord;
            while ((badWord = bufferedReader.readLine()) != null) {
                System.out.println(badWord);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not locate the bad words file");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        if (args.length == 2) {
            port = Integer.parseInt(args[0]);
            file = new File(args[1]);
        }
        ChatServer server = new ChatServer(port, file);
        server.start();


    }


    /**
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     *
     * @author Sultan Al-Ali and Aman Jariwala, lab sec 04
     * @version April 21, 2020
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {

            // Read the username sent to you by client
            try {
                boolean duplicate = false;
                int count = 0;
                for (int i = 0; i < clients.size(); i++) {
                    if (username.equals("Anonymous")) {
                        continue;
                    }
                    if (username.equals(clients.get(i).username)) {
                        count++;
                    }
                }
                if (count > 1) {
                    duplicate = true;
                }
                if (duplicate == false) {
                    sOutput.writeObject("Welcome to the chat function " + username + "!\n");
                    while (true) {

                        try {
                            cm = (ChatMessage) sInput.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            System.out.println("ID Successfully removed");
                        }

                        System.out.println(username + ": " + cm.getMessage());

                        // Send message back to the client

                        // type 0 => General Message
                        // type 1 => Logout Message
                        // type 2 => list method
                        // type 3 => direct message

                        if (cm.getType() == 3) {
                            directMessage(cm.getMessage(), cm.getRecipient());
                        }

                        if (cm.getType() == 2) {
                            list();
                        }

                        if (cm.getType() == 1) {

                            //remove that id from list of clients.
                            broadcast(username + " has left the chat");

                            remove(id);
                            break;
                        } else if (cm.getType() == 0) {
                            String message = cm.getMessage() + "";

                            ChatFilter chatFilter = new ChatFilter(file.getName());

                            message = chatFilter.filter(message);

                            broadcast(username + ": " + message);
                        }
                    }
                } else {
                    sOutput.writeObject("Sorry this username is already taken");
                    remove(id);
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        private boolean writeMessage(String message) {

            //return false if socket is not connected
            //if socket is connected return true with writing actual message else return false.
            if (socket.isConnected()) {

                // Before returning true, make sure you actually write the message to the ClientThread's
                // ObjectOutputStream using the writeObject method from the ObjectOutputStream class.
                try {
                    sOutput.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            } else return false;
        }

        public void directMessage(String message, String username1) {
            java.util.Date date = new java.util.Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String strDate = dateFormat.format(date);

            ChatFilter chatFilter = new ChatFilter(file.getName());
            message = chatFilter.filter(message);

            message = strDate + " " + this.username + ": " + message + "\n";
            for (int i = 0; i < clients.size(); i++) {
                if (this.username.equals(username1)) {
                    try {
                        sOutput.writeObject("You cannot DM yourself\n");
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (clients.get(i).username.equals(username1)) {
                    clients.get(i).writeMessage(message);
                    break;
                }
            }
        }

        private void close() {
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

        public void list() {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username.equals(username)) {
                    continue;
                } else {
                    try {
                        sOutput.writeObject(clients.get(i).username + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

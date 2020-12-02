/* A simple Java UDP client and server program. Used for testing purpose.
*  Main purpose: see if mini-net VM can run UDP client/server on specified hosts.
*
*  This is the java file that contains the main() method.
*
*  Two methods, udp_echo_server() and udp_echo_client(), are for testing
*       purpose. To see if we can run udp client/server on floodlight VM.
*
*  More classes will be added for encoding/decoding a DNS message.
*
*  For compiling, put all files in one folder. Then type:
*       "javac Simple_Java_Udp.java"
*
*  The javac should able to recognize all java files.
*
*  If time allows, fix out how to write a workable Makefile. */



import java.io.IOException;
import java.net.*;
import java.util.*;

public class Simple_Java_Udp {

    /* A simple UDP "server" program. Server means that it will wait for the
     * client to sends first message.
     *
     * @params:
     *      ip_addr - a string tells the IP address socket should bind to
     *          e.g. "10.0.0.3"
     *      port - a string tells the port socket should bind to
     *          e.g. "12345".
     *      */
    public void udp_echo_server(String ip_addr, String port) {
        InetAddress server_addr; // InetAddress with ip_addr.
        int port_value; // integer value for port.
        DatagramSocket severSocket; // declare a datagram socket variable.
        byte[] msgBuffer = new byte[512]; // message buffer.
        int msgLength = msgBuffer.length; // length of message buffer.
        DatagramPacket msgPacket; // declare a datagram packet variable.

        try {
            server_addr = InetAddress.getByName(ip_addr);
        }catch (UnknownHostException u){
            System.out.println("echo server: host IP unkown");
            return;
        }

        try {
            port_value = Integer.parseInt(port);
        }catch (NumberFormatException n)
        {
            System.out.println("echo server: failed to parse port number");
            return;
        }

        // now server address and port has been set up. Prepare for socket creation
        try {
            severSocket = new DatagramSocket(port_value, server_addr);
        }catch (SocketException s)
        {
            System.out.println("echo server: failed to create socket");
            return;
        }

        // now prepare to receive and send messages
        System.out.println("**** echo sever: prepare to start");
        // declare a scanner to scan command line input.
        Scanner cmdScanner = new Scanner(System.in);
        while (true){
            msgPacket = new DatagramPacket(msgBuffer, msgLength);
            try {
                severSocket.receive(msgPacket);
            }catch (IOException i) {
                System.out.println("echo sever: error when receving message.");
                break;
            }
            String recvData = new String(msgPacket.getData(), 0, msgPacket.getLength());
            System.out.println("**** echo server received: " + recvData);

            //prepare to send back
            try {
                severSocket.send(msgPacket);// Something is wrong here... TODO: check
            }catch (IOException i2){
                System.out.println("echo server: error when sending message");
                break;
            }


            String cmdInput = cmdScanner.nextLine();
            if (cmdInput == null){
                System.out.println("echo server: no cmd input, leave loop");
                break;
            }
            if (cmdInput.equalsIgnoreCase("quit") ||
                    cmdInput.equalsIgnoreCase("exit")){
                break;
            }
        }
        System.out.println("**** echo sever: prepare to close server");
        severSocket.close(); // close socket
        cmdScanner.close(); // close scanner.
    }


    /* A simple UDP "client" program. Client means that it will send a message to
     *  the server first, then waits for a reply.
     *
     * @params:
     *      ip_addr - a string tells the IP address of server
     *          e.g. "10.0.0.3"
     *      port - a string tells the port of server
     *          e.g. "12345".
     *      */
    public void udp_echo_client(String ip_addr, String port) {
        InetAddress server_addr; // InetAddress with ip_addr.
        int port_value; // integer value for port.
        DatagramSocket clientSocket; // declare a datagram socket variable.
        byte[] recvBuffer = new byte[512]; // message buffer for receiving data.
        int recvBufferLength = recvBuffer.length; // length of message buffer.
        // Use it to send data to "server"
        DatagramPacket sendPacket; // declare a datagram packet variable.
        // Use it to recv data from "server"
        DatagramPacket recvPacket; // declare a datagram packet variable.

        // we try to get Inetaddress and port value for server
        try {
            server_addr = InetAddress.getByName(ip_addr);
        }catch (UnknownHostException u)
        {
            System.out.println("echo client: server unkown: " + ip_addr);
            return;
        }
        try {
            port_value = Integer.parseInt(port);
        }catch (NumberFormatException n){
            System.out.println("echo client: port invalid: " + port);
            return;
        }

        // now we try to create a socket
        try {
            clientSocket = new DatagramSocket();
        }catch (SocketException s)
        {
            System.out.println("echo client: socket failed");
            System.out.println(s.getMessage());
            return;
        }

        // now we begin echo process
        System.out.println("**** echo client: prepare to start");
        // declare a scanner to scan command line input
        Scanner cmdScanner = new Scanner(System.in);
        while (true){
            String stdInput = cmdScanner.nextLine(); // read one line of cmd input
            if (stdInput == null){
                System.out.println("echo client: no input, leave loop");
                break;
            }
            if (stdInput.equalsIgnoreCase("quit"))
            {
                break; // quit the while loop
            }

            // initialize the sendPacket
            byte[] sendBuffer = stdInput.getBytes();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                    server_addr, port_value);

            // try to send packet
            try {
                clientSocket.send(sendPacket);
            }catch (IOException i)
            {
                System.out.println("echo client: failed to send message");
                System.out.println(i.getMessage());
                break; // leave while loop
            }

            // now we try to receive packet from server
            recvPacket = new DatagramPacket(recvBuffer, recvBufferLength);
            try {
                clientSocket.receive(recvPacket);
                String receivedData = new String(recvPacket.getData(),
                        0, recvPacket.getLength());
                System.out.println("**** echo client: from server: " + receivedData);
            }catch (IOException i)
            {
                System.out.println("echo client: failed to receive data");
                System.out.println(i.getMessage());
                break; // error occurs, leave while loop
            }

        }

        // now we have left the while loop
        System.out.println("**** echo client: prepare to exit");
        clientSocket.close(); // close socket
        cmdScanner.close(); // close scanner
    }


    public static void main(String[] args) {

        Simple_Java_Udp udpPlay = new Simple_Java_Udp();

        System.out.println("Hello World!");

        // test package usage
        if (args.length == 1){
            if (args[0].equalsIgnoreCase("test")){
                TestAddClass t1 = new TestAddClass(1024, 2345);
                TestAddClass t2 = new TestAddClass(new Random());
                System.out.println("t1 id: " + t1.getClass_id());
                System.out.println("t1 quatity: " + t1.getClass_quatity());
                System.out.println("t1 random: "+ t1.getNumberfromTestAddClass2());
                System.out.println("t2 id: " + t2.getClass_id());
                System.out.println("t2 quatity: " + t2.getClass_quatity());
                System.out.println("t2 random: " + t2.getNumberfromTestAddClass2());
                System.exit(0);
            }
        }


        if (args.length != 3) {
            System.out.println("Wrong usage");
            System.out.println("Correct usage: mode ip port");
            System.exit(0);
        }

        if (args[0].equalsIgnoreCase("server")) {
            // run server mode
            udpPlay.udp_echo_server(args[1], args[2]);
        }
        else if (args[0].equalsIgnoreCase("client")) {
            // run client mode
            udpPlay.udp_echo_client(args[1], args[2]);
        }else {
            System.out.println("Wrong usage");
            System.out.println("Correct usage: mode ip port");
        }


        System.exit(0);
    }
}

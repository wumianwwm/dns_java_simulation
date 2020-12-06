/** Represents a DNS server for our project simulation.
 * The DNS server receives a DNS query from a Client,
 * Then it generates a response message, and send it back
 *  to the client.
 *
 *  The server will have its own socket for sending, receiving
 *      packets to/from client. */
import java.net.*;

public class Simple_DNS_Server
{
    // stub...
    private DatagramSocket socket; // used for sending/receiving UDP packets
    private InetAddress server_addr; // IP address specifies socket to bind to.
    private int server_port; // port the server waits for UDP packets.

    /** Constructor:
     * Take an IP address and a Port, both in string format,
     * to create a Simple_DNS_Server object.*/
    public Simple_DNS_Server(String server_IP, String portStr)
    {
        //
    }

    /** Helper method:
     * create socket.*/
    private void createSocket(int server_port, InetAddress server_addr)
    {
        //
    }

    /** Helper method:
     * create InetAddress */
    private void getInetAddress(String server_IP)
    {
        //
    }

    /** Helper method:
     * extract port number. */
    private void getPortNumber(String portStr)
    {
        //
    }

}

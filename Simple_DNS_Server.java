/** Represents a DNS server for our project simulation.
 * The DNS server receives a DNS query from a Client,
 * Then it generates a response message, and send it back
 *  to the client.
 *
 *  The server will have its own socket for sending, receiving
 *      packets to/from client. */
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Simple_DNS_Server
{
    // variables use to create socket, and send/receive DNS query.
    private DatagramSocket socket; // used for sending/receiving UDP packets
    private InetAddress server_addr; // IP address specifies socket to bind to.
    private int server_port; // port the server waits for UDP packets.

    // Variable used for this research project:
    // the pre-set IP address towards a query.
    //  e.g. 192.168.56.4
    private String answer_IP;
    // the flag to be set at response header
    //  e.g. 0x1234
    private short headerFlag;
    /** Constructor:
     * Take an IP address and a Port, both in string format,
     * to create a Simple_DNS_Server object.*/
    public Simple_DNS_Server(String server_IP, String portStr,
                             String answer_IP, short headerFlag)
    {
        // call helper methods to initialize the variables.
        getPortNumber(portStr);
        getInetAddress(server_IP);
        createSocket();
        // set the answer_IP, and headerFlag
        this.answer_IP = answer_IP;
        this.headerFlag = headerFlag;
    }

    /** Helper method:
     * create socket, and set the time out value.
     * Note: getPortNumber() and getInetAddress() should be called
     *      first before calling this method.
     * Note: the parameter can be adjusted if needed in the future. */
    private void createSocket()
    {
        // first check if server_addr is null, or server_port is 0.
        if ((this.server_port == 0) || (this.server_addr == null))
        {
            this.socket = null;
            return;
        }

        try {
            this.socket = new DatagramSocket(this.server_port, this.server_addr);
            // set time out to 15 seconds.
            // If within 15 seconds, no UDP packets are received, a
            //  SocketTimeoutException will be thrown.
            this.socket.setSoTimeout(15000);
        }catch (SocketException s) {
            System.out.println("Simple_DNS_Server: socket " + s.getMessage());
            this.socket = null;
        }
    }

    /** Helper method:
     * create InetAddress based on server_IP.
     * If error occur, set server_addr to null. */
    private void getInetAddress(String server_IP)
    {
        try {
            this.server_addr = InetAddress.getByName(server_IP);
        }catch (UnknownHostException u) {
            System.out.println("Simpple_DNS_Sever: "
            + "server IP address is unknown.");
            this.server_addr = null;
        }
    }

    /** Helper method:
     * extract port number.
     * If error occurs, set server_port to 0. */
    private void getPortNumber(String portStr)
    {
        try {
            this.server_port = Integer.parseInt(portStr);
        }catch (NumberFormatException n){
            System.out.println("Simple_DNS_Sever: "
            + "sever port format error.");
            this.server_port = 0;
        }
    }


    /** Helper method:
     * Extract DNS query message from received packet.
     * @param recvPacket - packet received from a client.
     * @return a new DNS Message stands for DNS query. */
    private DNSMessage extractQuery(DatagramPacket recvPacket)
    {
        byte[] receivedBuffer = recvPacket.getData();
        BigEndianDecoder decoder = new BigEndianDecoder(receivedBuffer);
        return new DNSMessage(decoder);
    }


    /** Helper method:
     * Create a DNS response message from a DNS query message.
     * @param queryMsg: DNS Message stands for a query.
     * @return a new DNS Message stands for DNS response. */
    private DNSMessage generateResponse(DNSMessage queryMsg)
    {
        // create one resource record
        String queryDomainName = queryMsg.getQueryName();
        int rcode = queryMsg.getQType() & 0xffff;
        RecordType rType = RecordType.getByCode(rcode);
        short qClass = queryMsg.getQClass();
        DNSResourceRecord oneAnswer = new DNSResourceRecord(queryDomainName,
                rType, qClass, 3600, this.answer_IP);

        // put it in answer section.
        DNSResourceRecords answers = new DNSResourceRecords();
        answers.addOneRecord(oneAnswer);
        // authorities and additions.
        DNSResourceRecords nameServers = new DNSResourceRecords();
        DNSResourceRecords additional = new DNSResourceRecords();

        return new DNSMessage(queryMsg, this.headerFlag,
                answers, nameServers, additional);
    }


    /** Helper method:
     * Create a Datagram packet that will be sent to client.
     * @param response: DNS response Message, that HAS NOT BEEN ENCODED.
     * @param recvPacket: datagram packet received from a client.
     * @return a datagram packet to be sent to that client. */
    private DatagramPacket createSendPacket(DNSMessage response,
                                            DatagramPacket recvPacket)
    {
        // first we encode the response, and get bytes buffer.
        response.encode(response.getEncoder());
        byte[] sendBuffer = response.tobytesBuffer();
        int bufferLength = sendBuffer.length;

        // now we get client IP and port info from recvPacket
        InetAddress clientAddr = recvPacket.getAddress();
        int clientPort = recvPacket.getPort();

        return new DatagramPacket(sendBuffer, bufferLength,
                clientAddr, clientPort);
    }


    /** Running the server program:
     * Waits for a query from a DNS client, then encodes
     *  a response packet, with one answer.
     * The answer's IP address is this.answer_IP*/
    public void running_server()
    {
        if (this.socket == null)
        {
            System.out.println("Simple_DNS_Server: socket is null.");
            return;
        }

        // bytes buffer for creating the packet.
        byte[] recvBuffer = new byte[1024];
        int bufferLength = recvBuffer.length;
        while (true)
        {
            // set all bytes to 0. TODO: check
            Arrays.fill(recvBuffer, (byte)0);
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, bufferLength);
            try
            {
                this.socket.receive(recvPacket);
                // extract query and create response.
                DNSMessage query = this.extractQuery(recvPacket);
                DNSMessage response = this.generateResponse(query);
                // encode the response and send it back to server.
                DatagramPacket sendPacket = this.createSendPacket(response,
                        recvPacket);
                // now we try to send the packet
                this.socket.send(sendPacket);
            }catch (SocketTimeoutException s)
            {
                // no error, client timed out, leave the loop.
                break;
            }catch (IOException i)
            {
                System.out.println("Simple_UDP_Server: receive error");
                System.out.println(i.getMessage());
                break;
            }
        }

    }

}

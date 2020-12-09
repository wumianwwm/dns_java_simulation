import java.net.*;

public class Simple_DNS_Client {

    // variables use to create socket, and send/receive DNS query.
    private DatagramSocket socket; // used for sending/receiving UDP packets
    private InetAddress server_addr; // IP address of server
    private int server_port; // port of server waits for connection.

    private InetAddress attacker_addr; // IP address of attacker
    private int attackerPort; // port of attacker

    /** Constructor:*/
    public Simple_DNS_Client(String sever_IP, String severPort,
                             String attacker_IP, String attackerPort)
    {
        this.getInetAddress(sever_IP, attacker_IP);
        this.getPortNumber(severPort, attackerPort);
        try {
            this.socket = new DatagramSocket();
        }catch (SocketException s){
            System.out.println("Simple_DNS_Client: socket error.");
            this.socket = null;
        }
    }


    /** Helper method:
     *set timeout value for socket.*/
    public void setSocketTimeOut(int timeOut)
    {
        if (this.socket == null)
        {
            System.out.println("Simple_DNS_Client: socket is null. "
            + "Can't set timeout.");
            return;
        }

        try {
            this.socket.setSoTimeout(timeOut);
        }catch (SocketException s) {
            System.out.println("Simple_DNS_Client: cannot set timeout.");
            System.out.println(s.getMessage());
        }
    }

    /** Helper method:
     * create InetAddress based on server_IP, attacker_IP.
     * @param server_IP IP address, in string format, of server.
     * @param attacker_IP IP address, in string format, of attacker
     * If error occurs, set server_addr to null. */
    private void getInetAddress(String server_IP, String attacker_IP)
    {
        try {
            this.server_addr = InetAddress.getByName(server_IP);
            this.attacker_addr = InetAddress.getByName(attacker_IP);
        }catch (UnknownHostException u) {
            System.out.println("Simple_DNS_Client: "
                    + "server/attacker IP address unknown.");
            this.server_addr = null;
        }
    }

    /** Helper method:
     * extract port number.
     * @param severPort port number, in string format, of server.
     * @param attackerPort port number, in string format, of client
     * If error occurs, set port number to 0. */
    private void getPortNumber(String severPort, String attackerPort)
    {
        try {
            this.server_port = Integer.parseInt(severPort);
        }catch (NumberFormatException n){
            System.out.println("Simple_DNS_Client: "
                    + "sever port format error.");
            this.server_port = 0;
        }

        try {
            this.attackerPort = Integer.parseInt(attackerPort);
        }catch (NumberFormatException n1) {
            System.out.println("Simple_DNS_Client: "
            + "attacker port format error.");
            this.attackerPort = 0;
        }
    }


    /** Running the client program
     * Creates a query based on variation of domain name,
     * Sends the query to server,
     * Receives response.
     * Repeat with a new domain name.
     *
     * @param baseName The base domain name, e.g. "www.uwo.ca"
     *         if first query's name is "www.uwo0.ca",
     *         the second query's name is "www.uwo1.ca", etc.
     * @param numQuery Number of query the client will send.
     *          if numQuery <= 1, client will send one query. */
    public void running_client(String baseName, int numQuery)
    {
        String[] splitBaseName = this.splitBaseName(baseName);

        for (int i = 0; i < numQuery; i++)
        {
            String queryName = this.createDomainName(splitBaseName,
                    i);
            // send and receive dns message, version 1
            this.sendAndRecv_v0(queryName);
        }
    }

    /** Helper method for processing base domain name.
     * Take a domain name like "www.uwo.ca", split it into
     * 2 Strings, one "www.uwo", the other is ".ca"
     * @param baseName The base domain name, e.g. "www.uwo.ca".
     * @return an String array with at most 2 elements, one is
     *       something like "www.uwo", the other is ".ca" */
    private String[] splitBaseName(String baseName)
    {
        String[] splitStr = baseName.split("\\.+");
        if (splitStr.length >= 3)
        {
            // e.g. base name is "www.cs.uwo.ca", we want the first element
            //  to be "www.cs.uwo"
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < splitStr.length-2; i++)
            {
                builder.append(splitStr[i]);
                builder.append(".");
            }
            // append the second last element in splitStr
            builder.append(splitStr[splitStr.length-2]);
            String firstElement = builder.toString();

            // get the second element
            StringBuilder builder1 = new StringBuilder();
            builder1.append(".");
            builder1.append(splitStr[splitStr.length-1]);
            String secondElement = builder1.toString();

            // return an array with two elements.
            String[] returnStr = new String[2];
            returnStr[0] = firstElement;
            returnStr[1] = secondElement;
            return returnStr;
        }

        if (splitStr.length == 2)
        {
            // process the second element.
            StringBuilder builder1 = new StringBuilder();
            builder1.append(".");
            builder1.append(splitStr[splitStr.length-1]);
            splitStr[1] = builder1.toString();
        }
        return splitStr;
    }

    /** Helper method for generating the final domain name
     *      which will be used in query.
     *  Try to append a number to the second last(or the first)
     *      element of a string array, then append all elements
     *      of the array together.
     *  e.g.: input {"www.uwo", ".ca"}, 15.
     *        output "www.uwo15.ca"
     * */
    private String createDomainName(String[] splitStr, int cursor)
    {
        if (splitStr == null)
        {
            System.out.println("Simple_DNS_Client: " +
                    "can't create Domain Name - data is null.");
            return null;
        }
        if (splitStr.length == 0)
        {
            System.out.println("Simple_DNS_Client: " +
                    "can't create Domain Name - empty array.");
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(splitStr[0]);
        builder.append(cursor);
        if (splitStr.length == 2)
        {
            builder.append(splitStr[1]);
        }
        return builder.toString();
    }


    /** Send and receive message -- version 1.
     * create one query, send to sever, receive response
     * from server.
     * Test: calculate RTT, send and receive.
     * ******** No DFP involves ********
     * @param queryName Domain name we want to query. */
    private void sendAndRecv_v0(String queryName)
    {
        //
    }

    /** Helper method: create a Datagram Packet, which
     *      will be sent to server/attacker.
     *  @param queryName domain name in query.
     *  @return a Datagram Packet ready to be sent. */
    private DatagramPacket createSendPacket(String queryName)
    {

        return null;
    }
}

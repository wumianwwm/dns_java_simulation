import java.io.IOException;
import java.net.*;
import java.util.Random;

public class Simple_DNS_Client {

    // variables use to create socket, and send/receive DNS query.
    private DatagramSocket socket; // used for sending/receiving UDP packets
    private Random random; // used for generating random query ID.
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
        this.random = new Random();
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
        System.out.println("DNS Client: start running.");
        if (this.socket == null)
        {
            System.out.println("DNS Client: socket is null. Exit");
            System.exit(0);
        }

        String[] splitBaseName = this.splitBaseName(baseName);
        AuthSeverStats severStats = this.createServerStats(20, RecordType.A);
        System.out.println("severStats estimatedRtt: " + severStats.getEstimatedRTT()
        + " devRTT: " + severStats.getDevRTT());

        for (int i = 0; i < numQuery; i++)
        {
            String queryName = this.createDomainName(splitBaseName,
                    i);
            // send and receive dns message, version 1
//            this.sendAndRecv_v0(queryName);
            System.out.println("******** Round: " + i + " ********");
            this.sendAndRecv_v1(queryName, severStats);
            System.out.println("********************");
            System.out.println(" ");
            System.out.println(" ");
        }
        // close the socket
        this.socket.close();
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


    /** Send and receive message -- version 0.
     * create one query, send only to sever, receive response
     * from server.
     * Test: calculate RTT, send and receive.
     * ******** No DFP involves ********
     * @param queryName Domain name we want to query. */
    private void sendAndRecv_v0(String queryName)
    {
        // first create the packet to be sent.
        DatagramPacket queryPacket = this.createSendPacket(queryName,
                this.server_addr, this.server_port);
        try
        {
            this.socket.send(queryPacket);
            // record send time
            long sendTime = System.currentTimeMillis();

            // prepare to receive packet
            byte[] recvBuffer = new byte[1024];
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
            this.socket.receive(recvPacket);
            // record receive time
            long recvTime = System.currentTimeMillis();
            int rtt = (int) (recvTime - sendTime);

            // decode response.
            DNSMessage responseMsg = DNSMessage.getMessageFromPacket(recvPacket);
            // retrieve information and print results
            this.printAnswersFromResponse(responseMsg, rtt);

        }catch (IOException i)
        {
            //
        }
    }


    /** Send and receive message - version 1.
     * first, send query to both attacker and server.
     * then, use our version of DFP to handle possible attacks.
     *
     * This version will focus on handling duplicate response to a
     *  single query.
     *
     * It will implement in a way that, for each packet we receive
     *  and plan to process, it will be a expected response.
     *
     * In other words, a response with expected query name and ID,
     *  not a query name from previous query sent to server/attacker.
     * @param queryName Domain name we want to query.
     * @param severStats Server statistics. */
    private void sendAndRecv_v1(String queryName, AuthSeverStats severStats)
    {
        DatagramPacket toAttacker = this.createSendPacket(queryName,
                this.attacker_addr, this.attackerPort);
        DatagramPacket toSever = this.createSendPacket(queryName,
                this.server_addr, this.server_port);
        DatagramPacket receivePacket = this.createRecvPacket(1024);
        DatagramPacket additionPacket = this.createRecvPacket(1024);
        long sendTime  = 0; // time when we send the first packet.
        long recvTime  = 0; // time when we receive the first packet.
        int rtt = 0; // round trip time for the first  received packet.
        long recvTime2 = 0; // time when we receive the second packet.
        int rtt2 = 0; // round trip time for the second received packet.

        try
        {
            this.socket.send(toAttacker);
            sendTime = System.currentTimeMillis();
            this.socket.send(toSever);
            this.socket.setSoTimeout(0); // reset wait value.
            this.socket.receive(receivePacket);

            recvTime = System.currentTimeMillis();
            rtt = (int) (recvTime - sendTime);

            // now we calculate the time we need to wait for any more response
            int waitTime = severStats.getFullWindowTime(rtt);
            // set time out value, and waits for received message
            this.socket.setSoTimeout(waitTime);
            this.socket.receive(additionPacket);

            // we received a second packet!
            recvTime2 = System.currentTimeMillis();
            rtt2 = (int) (recvTime2 - sendTime);

            System.out.println("first received packet: ");
            DNSMessage firstMsg = DNSMessage.getMessageFromPacket(receivePacket);
            this.printAnswersFromResponse(firstMsg, rtt);

            System.out.println("second received packet: ");
            DNSMessage secondMsg = DNSMessage.getMessageFromPacket(additionPacket);
            this.printAnswersFromResponse(secondMsg, rtt2);
            //TODO: implement our imrpovments here.

        }
        catch (SocketTimeoutException t)
        {
            // we waits for some amount of time, no additional packets arrive.
            // The first packet is a valid one.
            recvTime2 = System.currentTimeMillis();
            severStats.updateSeverStats(rtt);
            System.out.println("Socket time out! after " +
                    (recvTime2 - sendTime) + "ms we sent the first packet!");
            // now we print the first packet.
            System.out.println("first received packet: ");
            DNSMessage firstMsg = DNSMessage.getMessageFromPacket(receivePacket);
            this.printAnswersFromResponse(firstMsg, rtt);

            /** Notice from experiment:
             * The following results is observed from experiment.
             * Client: send query #5 to server and attacker.
             *
             * Both server and attacker sends response back.
             *
             * However, the attacker's response did not arrive even
             *  we have catch a socket timeout.
             *
             * In other words, if we do not handle that late arrive packet,
             *  it will be extracted from socket's receive buffer, and used
             *  in the next round of comparison.*/

            // call helper method to discard late arrive packets.
            this.discardLateArrivedPacket(severStats);
        }
        catch (IOException i)
        {
            System.out.println("DNS client: IO exception in sendAndRecv_v1");
            System.out.println(i.getMessage());
        }
    }

    /** Helper method: create a Datagram Packet, which
     *      will be sent to server/attacker.
     *  @param queryName domain name in query.
     *  @param dstAddress destination address of the packet.
     *  @param destPort destination port of the packet.
     *  @return a Datagram Packet ready to be sent. */
    private DatagramPacket createSendPacket(String queryName, InetAddress dstAddress,
                                            int destPort)
    {
        // if needed, we will adjust this method, so that all
        //  query have different query ID.
        int randomId = this.random.nextInt(65535);
        DNSMessage queryMsg = new DNSMessage(queryName, randomId, RecordType.A);
        queryMsg.encode(queryMsg.getEncoder());
        byte[] queryData = queryMsg.tobytesBuffer();

        return new DatagramPacket(queryData, queryData.length,
                dstAddress, destPort);
    }

    /** Helper method for creating a datagram packet,
     *      which used for socket.receive() method. */
    private DatagramPacket createRecvPacket(int bufferSize)
    {
        byte[] bytesBuffer = new byte[bufferSize];
        return new DatagramPacket(bytesBuffer, bufferSize);
    }

    /** Helper method: create an AuthServerStats object.
     *  Create an object, and update its estimatedRTT, devRTT
     *      by sending X number of packets to server, and
     *      record their RTT.
     *  @param sampleCount the X value.
     *  @param type Type of query client will perform. */
    private AuthSeverStats createServerStats(int sampleCount, RecordType type)
    {
        String server_IP = this.server_addr.getHostAddress();
        AuthSeverStats severStats = new AuthSeverStats(server_IP, type);

        int errorCount = 0;
        int successCount = 0;
        while (true)
        {
            if ((successCount >= sampleCount) ||
                    (errorCount >= sampleCount))
            {
                break;
            }

            DatagramPacket packet = this.createSendPacket("www.uwo.ca",
                    this.server_addr, this.server_port);
            try
            {
                // set time out; prepare packet for receiving data
                this.socket.setSoTimeout(5000);
                byte[] buffer = new byte[1024];
                DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
                // send and receive packets.
                this.socket.send(packet);
                long sendTime = System.currentTimeMillis();
                this.socket.receive(recvPacket);
                long recvTime = System.currentTimeMillis();
                // calculate round trip time, and update server statistics.
                int rtt = (int) (recvTime - sendTime);
                severStats.updateSeverStats(rtt);
                // now this round is successful.
                successCount += 1;
            } catch (IOException i)
            {
                errorCount += 1;
            }
        }

        if (successCount < sampleCount){
            // debug purpose.
            System.out.println("DNS_Client: warning, insufficient sample count.");
        }
        return severStats;
    }

    /** Helper method for sendAndRecv_v1:
     * Discard a late arrive packet. Because within our waiting
     *  window, the packet did not arrive.
     * We need to explicitly extract it out off socket's receive
     *  buffer, so that it won't affect further experiments.
     *
     * @param severStats used to get estimatedRTT. */
    private void discardLateArrivedPacket(AuthSeverStats severStats)
    {
        int trialTime = 0; // how many times we have tried the code below?
        DatagramPacket recvPacket = this.createRecvPacket(512);
        // try to handle delay packets, allow at most 10 failures.
        while (trialTime < 10)
        {
            try
            {
                // we try to set socket time out value to be estimated
                //  round trip time between client/server.
                this.socket.setSoTimeout(severStats.getEstimatedRTT());
                // now see if any late packets arrived.
                this.socket.receive(recvPacket);
            }catch (SocketTimeoutException t)
            {
                // Okay, no more delayed packet.
                break;
            }catch (IOException i)
            {
                System.out.println(i.getMessage());
                trialTime += 1;
            }
        }

        if (trialTime >= 10)
        {
            System.out.println("client: discard late packet: error.");
        }
    }

    /** Helper method for printing results.
     * @param responseMsg DNS Message decoded from a response packet.
     * @param rtt round trip time of the packet. */
    private void printAnswersFromResponse(DNSMessage responseMsg, int rtt)
    {
        // retrieve information.
        int qID = responseMsg.getQueryId();
        String qName = responseMsg.getQueryName();
        RecordType qType = RecordType.getByCode(
                responseMsg.getQType());
        String[] IPs = responseMsg.retrieveDNSAnswers(qName, qType);

        // print results.
        System.out.println(qID + " " + qName + " "
                + qType);
        for (int i = 0; i < IPs.length; i++)
        {
            System.out.print(IPs[i]);
            System.out.print(" ");
        }
        System.out.println(" RTT: " + rtt);
        System.out.println("----------------");
        System.out.println(" ");
    }
}

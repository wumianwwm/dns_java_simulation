import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Simple_DNS_Client {

    // variables use to create socket, and send/receive DNS query.
    private DatagramSocket socket; // used for sending/receiving UDP packets
    private Random random; // used for generating random query ID.
    private InetAddress server_addr; // IP address of server
    private int server_port; // port of server waits for connection.
    private InetAddress attacker_addr; // IP address of attacker
    private int attackerPort; // port of attacker
    // variables used for experiment purpose
    // String: IP address, Int: how many times that IP been used as an answer to a query.
    private HashMap<String, Integer> experimentResults;
    private long totalTime; // client total execution time for processing queries.

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
        this.experimentResults = new HashMap<>();
        this.totalTime = 0;
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

        PrintStream originalOut = System.out;
        try
        {
            PrintStream outputStream = new PrintStream(
                    new FileOutputStream("00_output.txt"));
            System.setOut(outputStream);
//            System.setOut(originalOut); // debug purpose...
        }catch (FileNotFoundException f)
        {
            System.out.println("client: failed to set output.");
            System.out.println(f.getMessage());
        }

        String[] splitBaseName = this.splitBaseName(baseName);
        System.out.println("Start packet sampling");
        AuthServerStats severStats = this.createServerStats(20, RecordType.A);
        System.out.println("Server statistics: estimated Round Trip Time(RTT): "
                + severStats.getEstimatedRTT() + " ms");
        System.out.println("Server statistics: RTT deviation: " +
                severStats.getDevRTT() + " ms");
        System.out.println(" ");
        System.out.println("DNS Client: start processing queries");
        System.out.println(" ");

        for (int i = 0; i < numQuery; i++)
        {
            String queryName = this.createDomainName(splitBaseName,
                    i);
            // send and receive dns message, version 1
//            this.sendAndRecv_v0(queryName);
            System.out.println("******** Round " + i +
                     ": query name: " + queryName +" ********");
            long before_v1 = System.currentTimeMillis();
            this.sendAndRecv_v1(queryName, severStats);
            long after_v1 = System.currentTimeMillis();
            long v1_execution_time = after_v1 - before_v1;
            this.totalTime += v1_execution_time;
            System.out.println("********************************************");
            System.out.println(" ");
            System.out.println(" ");
            System.out.println(" ");
        }
        System.out.println("**** Experiment Results Summary ****");
        double average_v1_time = ((double) this.totalTime) / 25 ;
        System.out.println(" ");
        System.out.println("Average time to lookup one domain name: "
                + average_v1_time + " ms");
        System.out.println(" ");
        System.out.println("IP addresses client thinks are valid:");
        for (String s: this.experimentResults.keySet())
        {
            System.out.println("IP: " + s + " counts: " +
                    this.experimentResults.get(s));
        }
        // close the socket
        System.setOut(originalOut);
        System.out.println("End of client running process");
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
        int queryId = this.random.nextInt(65535);
        DatagramPacket queryPacket = this.createSendPacket(queryName,
                this.server_addr, this.server_port, queryId);
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
    private void sendAndRecv_v1(String queryName, AuthServerStats severStats)
    {
        int queryId = this.random.nextInt(65535);
        DatagramPacket toAttacker = this.createSendPacket(queryName,
                this.attacker_addr, this.attackerPort, queryId);
        DatagramPacket toSever = this.createSendPacket(queryName,
                this.server_addr, this.server_port, queryId);
        DatagramPacket firstRecv = this.createRecvPacket(1024);
        DatagramPacket secondrecv = this.createRecvPacket(1024);
        long sendTime  = 0; // time when we send the first packet.
        long recvTime  = 0; // time when we receive the first packet.
        int rtt = 0; // round trip time for the first  received packet.
        long recvTime2 = 0; // time when we receive the second packet.
        int rtt2 = 0; // round trip time for the second received packet.

        // to consider packet loss, the client tries to send packet, waits for first response.
        // If within 10 rounds, we still cannot receive a response for query, end this method.
        int receiveFirstPktTrial = 1;
        while (true)
        {
            try
            {
                // socket timeout might be altered by previous method calls,
                //  we need to reset socket timeout here.
                this.socket.setSoTimeout(2 * severStats.getEstimatedRTT());
                this.socket.send(toAttacker);
                sendTime = System.currentTimeMillis();
                this.socket.send(toSever);
                this.socket.receive(firstRecv);

                // now we successfully received the first packet
                recvTime = System.currentTimeMillis();
                rtt = (int) (recvTime - sendTime);
                break;
            }
            catch (SocketTimeoutException t)
            {
                // We failed to receive the first packet.
                receiveFirstPktTrial += 1;
                if (receiveFirstPktTrial >= 10)
                {
                    System.out.println("DNS Client: Frequent packet lose.");
                    System.out.println("final answer: " + queryName
                            + " IP:" + " failed to get IP");
                    // 255.255.255.255 - error outside the scope of out DFP algorithm.
                    this.updateExperimentResults("255.255.255.255");
                    return;
                }
            }
            catch (IOException io)
            {
                System.out.println("DNS Client: IO exception in sendAndRecv_v1 1");
                System.out.println(io.getMessage());
                System.out.println("final answer: " + queryName
                        + " IP:" + " failed to get IP");
                this.updateExperimentResults("255.255.255.255");
                return;
            }
        }

        try
        {
            // We have received the first packet.
            // now we calculate the time we need to wait for any more response
            int waitTime = severStats.getFullWindowTime(rtt);
            // set time out value, and waits for received message
            this.socket.setSoTimeout(waitTime);
            this.socket.receive(secondrecv);

            // we received a second packet!
            recvTime2 = System.currentTimeMillis();
            rtt2 = (int) (recvTime2 - sendTime);

        }
        catch (SocketTimeoutException t)
        {
            // we waits for some amount of time, no additional packets arrive.
            // The packet is valid if it matches the query client sent.
            recvTime2 = System.currentTimeMillis();
            System.out.println("Socket time out! after " +
                    (recvTime2 - sendTime) + "ms we sent the first packet!");
            System.out.println("How many times we send query?: " + receiveFirstPktTrial);
            // now we print the first packet.
            System.out.println("the only received packet: ");
            DNSMessage firstMsg = DNSMessage.getMessageFromPacket(firstRecv);
            this.printAnswersFromResponse(firstMsg, rtt);
            if (firstMsg.getQueryId() != queryId)
            {
                // the query ID is not matched, this response is invalid.
                System.out.println("One packet: error: query ID unmatched.");
                this.updateExperimentResults("255.255.255.255");
                this.discardLateArrivedPacket(severStats);
                return;
            }

            // update experiment results.
            String[] ips = firstMsg.retrieveDNSAnswers(queryName,
                    RecordType.getByCode(firstMsg.getQType() & 0xFFFF));
            if (ips.length == 0)
            {
                // This is usually cause by received packet's is a response to
                //  another query packet sent by client.
                System.out.println("One packet: error: no matched IP address.");
                firstMsg.printDNSMessage();
                this.updateExperimentResults("255.255.255.255");
            }
            if (ips.length > 0)
            {
                // the response packet's query name and type matches client's sent query.
                System.out.println("One packet: update experiment result using: " + ips[0]);
                this.updateExperimentResults(ips[0]);
            }

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

            // discard any late arrive packets, then return.
            this.discardLateArrivedPacket(severStats);
            return;
        }
        catch (IOException i)
        {
            System.out.println("DNS client: IO exception in sendAndRecv_v1");
            System.out.println(i.getMessage());
            return;
        }

        // When code reaches here, we have received two responses for one query.
        System.out.println("How many times we send query: " + receiveFirstPktTrial);
        System.out.println("first received packet info: ");
        DNSMessage firstMsg = DNSMessage.getMessageFromPacket(firstRecv);
        this.printAnswersFromResponse(firstMsg, rtt);
        System.out.println("second received packet info: ");
        DNSMessage secondMsg = DNSMessage.getMessageFromPacket(secondrecv);
        this.printAnswersFromResponse(secondMsg, rtt2);

        // Create packet statistics.
        DNSMessage[] messages = new DNSMessage[2];
        messages[0] = firstMsg;
        messages[1] = secondMsg;
        InetAddress[] source_addrs = new InetAddress[2];
        source_addrs[0] = firstRecv.getAddress();
        source_addrs[1] = secondrecv.getAddress();
        int[] rtts = new int[2];
        rtts[0] = rtt;
        rtts[1] = rtt2;
        AuthServerPacketStats[] statsArr = this.createPacketStatsArray(queryId,
                queryName, severStats, messages, source_addrs, rtts);

        switch (statsArr.length)
        {
            case 0:
                System.out.println("error: no packet is valid");
                System.out.println("final answer: " + queryName
                        + " IP: failed to get IP");
                this.updateExperimentResults("255.255.255.255");
                return;
            case 1:
                System.out.println("Only one packet is valid.");
                System.out.println("final answer: "+statsArr[0].getQueryName()
                        + " IP:" + statsArr[0].getIp_addresses()[0]);
                this.updateExperimentResults(statsArr[0].getIp_addresses()[0]);
                // when create the stats, we update the countWithinWindowTime already,
                //  no need to update packet stats again.
                return;
        }

        // now we can for sure that, the packet statistics has two elements.
        System.out.println("Two potential valid packets received. Start" +
                " rescue method.");
        int rv = this.v1_dfp_rescue(queryName, severStats, statsArr);
//        System.out.println("statsArr0: " + statsArr[0].getIp_addresses()[0]);
//        System.out.println("statsArr1: " + statsArr[1].getIp_addresses()[0]);
//        System.out.println("rv value: " + rv);
        switch (rv)
        {
            case -1:
                System.out.println("Rescue method failed, cannot determine which " +
                        "one is the valid packet.");
                System.out.println(queryName + " IP: failed to get IP");
                // 0.0.0.0 means we can't distinguish between 2 potential valid IPs.
                this.updateExperimentResults("0.0.0.0");
                return;
            case 0:
                // we update server stats using the first rtt
                System.out.println("Rescue method succeed, " +
                        "update server stats using " + rtt);
                severStats.updateSeverStats(rtt);
                System.out.println("Update experiment result using: " +
                        statsArr[0].getIp_addresses()[0]);
                this.updateExperimentResults(statsArr[0].getIp_addresses()[0]);
                break;
            case 1:
                // we update server stats using the second rtt.
                System.out.println("Rescue method succeed, " +
                        "update server stats using " + rtt2);
                severStats.updateSeverStats(rtt2);
                System.out.println("Update experiment result using: " +
                        statsArr[1].getIp_addresses()[0]);
                this.updateExperimentResults(statsArr[1].getIp_addresses()[0]);
        }
        // print answer.
        System.out.println("final answer: " + queryName
                + " IP: " + statsArr[rv].getIp_addresses()[0]);
    }

    /** Helper method: create a Datagram Packet, which
     *      will be sent to server/attacker.
     *  @param queryName domain name in query.
     *  @param dstAddress destination address of the packet.
     *  @param destPort destination port of the packet.
     *  @param queryId  ID in dns header.
     *  @return a Datagram Packet ready to be sent. */
    private DatagramPacket createSendPacket(String queryName, InetAddress dstAddress,
                                            int destPort, int queryId)
    {
        // two packets will have same ID in header, if they use same queryId.
        DNSMessage queryMsg = new DNSMessage(queryName, queryId, RecordType.A);
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
    private AuthServerStats createServerStats(int sampleCount, RecordType type)
    {
        String server_IP = this.server_addr.getHostAddress();
        AuthServerStats severStats = new AuthServerStats(server_IP, type);
        int queryId = this.random.nextInt(65535);

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
                    this.server_addr, this.server_port, queryId);
            try
            {
                // set time out; prepare packet for receiving data
                this.socket.setSoTimeout(300);
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

    /** Helper method: create an array of AuthServerPacketStats objects.
     * Given the two datagram packets received, check if they are
     *  responses to the domain name in query sent from client.
     *
     * @param qId query ID
     * @param queryName domain name sent in query.
     * @param serverStats  authoritative server statistics
     * @param messages array contains 2 DNS messages, first message from first packet.
     *                  second message from the second packet.
     * @param source_addrs array contains 2 InetAddress, same rule as above.
     * @param rtts array contains 2 integers, same rule as above.
     * @return an array of AuthServerPacketStats.
     *  May be 0, 1, or 2 element.
     *  The first one will always created from the first packet.
     *  The second one will always created from the second packet. */
    private AuthServerPacketStats[] createPacketStatsArray(
            int qId,String queryName, AuthServerStats serverStats,
            DNSMessage[] messages, InetAddress[] source_addrs,
            int[] rtts)
    {
        AuthServerPacketStats firstPktStats = new AuthServerPacketStats(
                messages[0], source_addrs[0], serverStats, rtts[0]);
        AuthServerPacketStats secondPktStats = new AuthServerPacketStats(
                messages[1], source_addrs[1], serverStats, rtts[1]);
        List<AuthServerPacketStats> statsList = new ArrayList<>();

        if (firstPktStats.isStatsHasValidIdAndName(qId, queryName))
        {
            statsList.add(firstPktStats);
        }
        if (secondPktStats.isStatsHasValidIdAndName(qId, queryName))
        {
            statsList.add(secondPktStats);
        }

        int listSize = statsList.size();
        AuthServerPacketStats[] packetStatsArr = new AuthServerPacketStats[listSize];
        for (int i = 0; i < listSize; i++)
        {
            packetStatsArr[i] = statsList.get(i);
        }

        return packetStatsArr;
    }


    /** Handle case when we receive two response from different servers,
     *   and both of them looks like "valid" response.
     * Resend the query to both attacker and server, 5 times.
     * Receive response, based on same logic in sendAndRecv_v1().
     * Update packet statistics.
     * After 5 times, check which packet statistic has more
     *  counts within window time.
     *
     * Note: I know this method is reusing some code in sendAndRecv_v1()...
     * So many optimization thoughts, (due to procrastination) so little time T_T...
     * @param queryName domain name in the query.
     * @param severStats authoritative server statistics
     * @param pktStatsArr an array of packet statistics. only call
     *                    this method when array has exact 2 elements.
     *         Fist element stands for the first received packet;
     *         Second element stands for the second received packet.
     * @return -1 - we failed to distinguish which packet is valid one;
     *         0 - the first packet is the valid one.
     *         1 - the second packet is the valid one.*/
    private int v1_dfp_rescue(String queryName, AuthServerStats severStats,
                              AuthServerPacketStats[] pktStatsArr)
    {
        int queryId = 0; // id in DNS header
        long sendTime  = 0; // time when we send the first packet.
        long recvTime  = 0; // time when we receive the first packet.
        int rtt = 0; // round trip time for the first  received packet.
        long recvTime2 = 0; // time when we receive the second packet.
        int rtt2 = 0; // round trip time for the second received packet.
        int waitTime = 0; // time to wait for second packet.

        for (int i = 0; i < 5;)
        {
            queryId = this.random.nextInt(65535);
            DatagramPacket toAttacker = this.createSendPacket(queryName,
                    this.attacker_addr, this.attackerPort, queryId);
            DatagramPacket toSever = this.createSendPacket(queryName,
                    this.server_addr, this.server_port, queryId);
            DatagramPacket firstRecv = this.createRecvPacket(1024);
            DatagramPacket secondRecv = this.createRecvPacket(1024);
            // we try to receive the first packet.
            try
            {
                // set wait time to 2*estimatedRTT, if no packets come back, its lost.
                this.socket.setSoTimeout(2 * severStats.getEstimatedRTT());
                this.socket.send(toAttacker);
                sendTime = System.currentTimeMillis();
                this.socket.send(toSever);
                this.socket.receive(firstRecv);
                recvTime = System.currentTimeMillis();
                rtt = (int) (recvTime - sendTime);
                i += 1; // we get at least one response, count as 1 success re-send.
            }catch (SocketTimeoutException t)
            {
                // we lost the first received packet.
                this.discardLateArrivedPacket(severStats);
                continue;
            }catch (IOException io)
            {
                System.out.println("v1_dfp_rescue: IO error.");
                System.out.println(io.getMessage());
                continue;
            }
            // we try to receive the second packet
            try
            {
                waitTime = severStats.getFullWindowTime(rtt);
                this.socket.setSoTimeout(waitTime);
                this.socket.receive(secondRecv);
                // now, again, second packet arrives.
                recvTime2 = System.currentTimeMillis();
                rtt2 = (int) (recvTime2 - sendTime);
                // update the packet statistics
                this.update_pktStatsArr(pktStatsArr, severStats, firstRecv, rtt);
                this.update_pktStatsArr(pktStatsArr, severStats, secondRecv, rtt2);
            }catch (SocketTimeoutException t)
            {
                // no second packet, discard late packets, update packet statistics.
                this.discardLateArrivedPacket(severStats);
                this.update_pktStatsArr(pktStatsArr, severStats, firstRecv, rtt);
            }catch (IOException io)
            {
                System.out.println("v1_dfp_rescue: IO error.");
                System.out.println(io.getMessage());
            }

        }

        if (pktStatsArr[0].getCountWithinWindowTime() <
        pktStatsArr[1].getCountWithinWindowTime())
        {
            // the second packet has more counts fall in window time.
            return 1;
        }
        if (pktStatsArr[0].getCountWithinWindowTime() >
        pktStatsArr[1].getCountWithinWindowTime())
        {
            // the first packet has more counts fall in window time.
            return 0;
        }

        // we failed to distinguish which one has more counts in window time.
        return -1;
    }

    /** Helper method for v1_dfp_rescue:
     * Help updating the two packet statistics, based on new received
     *  packets and their round trip time.
     * @param pktStatsArr array of packet statistics.
     * @param severStats server statistics
     * @param recvPkt a new received packets.
     * @param rtt round trip time of the packet. */
    private void update_pktStatsArr(AuthServerPacketStats[] pktStatsArr,
                                    AuthServerStats severStats,
                                    DatagramPacket recvPkt,
                                    int rtt)
    {
        for (int i = 0; i < pktStatsArr.length; i++)
        {
            if (pktStatsArr[i].isPacketBelongsToStats(recvPkt))
            {
                pktStatsArr[i].updateInWindowTimeCount(rtt, severStats);
            }
        }
    }


    /** Helper method for sendAndRecv_v1:
     * Discard a late arrive packet. Because within our waiting
     *  window, the packet did not arrive.
     * We need to explicitly extract it out off socket's receive
     *  buffer, so that it won't affect further experiments.
     *
     * @param severStats used to get estimatedRTT. */
    private void discardLateArrivedPacket(AuthServerStats severStats)
    {
        int trialTime = 0; // how many times we have tried the code below?
        DatagramPacket recvPacket = this.createRecvPacket(512);
        // try to handle delay packets, allow at most 10 failures.
        while (trialTime < 10)
        {
            try
            {
                // we set socket time out value to be 4 * estimated
                //  round trip time between client/server.
                this.socket.setSoTimeout(4 * severStats.getEstimatedRTT());
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
        System.out.println(" RTT: " + rtt + " ms");
        System.out.println("----------------");
        System.out.println(" ");
    }

    /** Helper method for updating experimental results.
     * Takes the IP address we get for one query, update the experiment results.
     * @param IPfromServer ip address we obtained from DNS response.*/
    private void updateExperimentResults(String IPfromServer)
    {
        Integer count = this.experimentResults.get(IPfromServer);
        if (count != null)
        {
            this.experimentResults.put(IPfromServer,count+1);
            return;
        }
        this.experimentResults.put(IPfromServer, 1);
    }
}

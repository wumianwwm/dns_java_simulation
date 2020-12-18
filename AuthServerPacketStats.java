import java.net.DatagramPacket;
import java.net.InetAddress;

/** Represents the packet statistics.
 *
 * If we send two queries, with the same domain name to lookup,
 *  to the same authority server. We expect that the two responses
 *  from that server have same DNS answers, i.e. IP addresses.
 *
 * When our version of DFP encounters multiple(i.e. 2) packets corresponds
 *  to one query. It will try to resend that query multiple times, and
 *  record upcoming responses' round trip time.
 *
 * We assume that in theory, packets coming from attacker are less likely
 *  to fall within window time, than packets coming from actual server
 *
 * This class aims to represent the statistics of several packets
 *  received from an "authoritative server".
 *
 * In some cases, this class can represents a simplified DNS response
 *  message, extracted from a datagram packet. */
public class AuthServerPacketStats
{
    // the IP address where the packet comes from.
    private String server_address;
    // domain name in the query the client previously sent.
    private String queryName;
    // answers (e.g. IP addresses) of that domain name
    private String[] ip_addresses;
    // count how many times, the matched responses fall in window time.
    //  Matched response: DNS response come from same server, contain same
    //  query name in DNS question section.
    private int countWithinWindowTime = 0;
    // the ID in DNS response header field. Used to verify if the packet that
    //  used to create this object is expected - i.e. query ID matches the client
    //  previously sent DNS query's header ID.
    private int id_from_header;

    /** Alternative constructor: use a DNS Message decoded from a packet,
     *      to construct a packet statistics
     * @param responseMsg the datagram packet we received.
     * @param source_addr source of the packet.
     * @param serverStats Authoritative server statistics.
     * @param rtt round trip time of that DNS message.*/
    public AuthServerPacketStats(DNSMessage responseMsg,
                                 InetAddress source_addr,
                                 AuthServerStats serverStats,
                                 int rtt)
    {
        this.server_address = source_addr.getHostAddress();
        this.queryName = responseMsg.getQueryName();
        RecordType qType = RecordType.getByCode(responseMsg.getQType() & 0xFFFF);
        this.ip_addresses = responseMsg.retrieveDNSAnswers(this.queryName, qType);
        this.id_from_header = responseMsg.getQueryId();

        if (!serverStats.isEarlyPacket(rtt))
        {
            this.countWithinWindowTime = 1;
        }
    }

    /** Constructor:
     * Create an authoritative packet statistics object,
     *  by using the the first DNS response message we
     *  received from an "authoritative server".
     * @param packet the datagram packet we received..
     * @param severStats Authoritative server statistics.
     * @param rtt round trip time of that DNS message. */
    public AuthServerPacketStats(DatagramPacket packet,
                                 AuthServerStats severStats,
                                 int rtt)
    {
        DNSMessage responseMsg = DNSMessage.getMessageFromPacket(packet);
        RecordType queryType = RecordType.getByCode(
                responseMsg.getQType() & 0xFFFF);

        this.server_address = packet.getAddress().getHostAddress();
        this.queryName = responseMsg.getQueryName();
        this.ip_addresses = responseMsg.retrieveDNSAnswers(this.queryName,
                queryType);
        if (!severStats.isEarlyPacket(rtt))
        {
            // The packet does not arrive before window start time.
            // The first packet is within window time.
            this.countWithinWindowTime = 1;
        }
    }

    /** Helper method to get server's IP address,
     *      in String format. */
    public String getServer_address()
    {
        return this.server_address;
    }

    /** Helper method to get query name. */
    public String getQueryName()
    {
        return this.queryName;
    }

    /** Helper method to get all ip_addresses of the query name. */
    public String[] getIp_addresses()
    {
        return this.ip_addresses;
    }

    /** Helper method to get how many times packets are fall
     *      in window time. */
    public int getCountWithinWindowTime()
    {
        return this.countWithinWindowTime;
    }

    /** Helper method to get query ID */
    public int getId_from_header()
    {
        return this.id_from_header;
    }


    /** Update the statistics:
     * Giving another response's round trip time, check
     *  if it falls within window time. If yes, update
     *  this.countWithinWindowTime.
     *
     * When calling this method, another check method should be called,
     *  so that we know the rtt of that response is what we want.
     *
     * In other words, the rtt corresponds packet, is coming from
     *  this.server_address;
     * The packet contains answer for this.queryName.
     *
     * @param rtt  round trip time of a packet.
     * @param severStats authoritative server statistics. */
    public void updateInWindowTimeCount(int rtt, AuthServerStats severStats)
    {
        if (!severStats.isEarlyPacket(rtt))
        {
            // the packet which rtt belongs to, is not an early packet.
            this.countWithinWindowTime += 1;
        }
    }


    /** Check if one packet comes from the same server,
     *   has the same query name, as this packet statistics.
     * @param packet a received datagram packet.
     * @return true if the packet has the same info stats above.
     *         false otherwise. */
    public boolean isPacketBelongsToStats(DatagramPacket packet)
    {
        DNSMessage message = DNSMessage.getMessageFromPacket(packet);
        String source_IP = packet.getAddress().getHostAddress();
        String qName = message.getQueryName();
        if (!qName.equals(this.queryName))
        {
            return false;
        }
        if (!source_IP.equals(this.server_address))
        {
            return false;
        }
        return true;
    }
}



/** Class represents authority server statistics:
 * This class keep records of an authority sever's
 * IP address, type associate with the statistics.
 *
 * In DFP paper, authors states that different record type
 *  (e.g. Type A or Type CNAME) of the query, will affect the
 *  RTT from that authority server.
 *
 * Two statistics are recorded: RTT - Round Trip Time,
 *  and DevRTT - deviation of RTT. */

public class AuthSeverStats {

    // IP address of server, in string format
    private String serverIP;
    // type of query send from client
    private RecordType type;

    // estimated round trip time, in millisecond.
    private int estimatedRTT;
    // deviation of rtt, in millisecond.
    private double devRTT;

    // alpha and beta value -- from DFP paper
    private double alpha = 0.125;
    private double beta = 0.25;
    // factor window value -- from DFP paper
    private int factorWindow = 2;

    /** Default constructor
     * @param ip Sever ip address
     * @param r type of query associate with this instance */
    public AuthSeverStats(String ip, RecordType r)
    {
        this.serverIP = ip;
        this.type = r;
        this.estimatedRTT = 0;
        this.devRTT = 0; // no deviation yet.
    }

    /** Helper method for getting IP. */
    public String getServerIP()
    {

        return this.serverIP;
    }

    /** Helper method for getting type. */
    public RecordType getType()
    {

        return this.type;
    }

    /** Helper method for getting estimatedRTT. */
    public int getEstimatedRTT()
    {
        return this.estimatedRTT;
    }

    /** Helper method for getting devRTT. */
    public double getDevRTT()
    {
        return this.devRTT;
    }

    /** Update estimatedRTT and devRTT
     * For simplicity of our project, right now, let's
     *  not worry about when should a response's RTT be
     *  updated to this server statistics.
     *
     * For simulation, we might explicitly measure estimatedRTT.
     * @assume the RTT is from a valid server response.
     *
     * @param rtt - round trip time of a response. */
    public void updateSeverStats(int rtt)
    {
        if (this.estimatedRTT == 0)
        {
            this.estimatedRTT = rtt;
        }
        this.estimatedRTT = (int) ((1 - this.alpha) *
                this.estimatedRTT + this.alpha * rtt);

        // absolute value of difference between rtt
        //  and estimatedRTT
        double absValue = Math.abs(rtt - this.estimatedRTT);

        this.devRTT = (1 - this.beta) * this.devRTT
                + this.beta * absValue;
    }

    /** Get the time client should waits for another response..
     * For too early packet, wait time calculation is same as mentioned
     *  in DFP paper.
     * Even if packet is not too early, we will still wait full window time.
     * @param rtt Round Trip Time of the first response packet */
    public int getFullWindowTime(int rtt)
    {
        if (this.estimatedRTT == 0)
        {
            // wait another RTT.
            return rtt;
        }

        // update devRTT to use.
        double absValue = Math.abs(rtt - this.estimatedRTT);
        double finalDevRTT = 0; // the final DevRTT we gonna use.
        if (this.devRTT == 0)
        {
            // We calculate a temporary devRTT value.
            finalDevRTT = this.beta * absValue;
        }
        if (this.devRTT != 0)
        {
            finalDevRTT = this.devRTT;
        }

        // calculate wait time.
        int windowStartTime = (int) (this.estimatedRTT -
                finalDevRTT * this.factorWindow);
        if (rtt < windowStartTime)
        {
            // for too early packet.
            return (int)(absValue + finalDevRTT * this.factorWindow);
        }
        // for not too early packet
        return (int) (finalDevRTT * this.factorWindow);
    }

    /** Helper method: check if one packet is an early packet.
     * @param rtt Round trip time of one query/response.
     * @return true is packet arrives before window start time.
     *      false otherwise.
     * @assume estimatedRTT and devRTT are not zero. */
    public boolean isEarlyPacket(int rtt)
    {
        int windowStartTime = (int) (this.estimatedRTT -
                this.devRTT * this.factorWindow);
        if (rtt < windowStartTime)
        {
            return true;
        }

        return false;
    }



}

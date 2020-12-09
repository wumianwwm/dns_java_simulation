

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
     * @param r type of query associate with this instance
     * @param rtt round trip time for the query's first response.*/
    public AuthSeverStats(String ip, RecordType r, int rtt)
    {
        this.serverIP = ip;
        this.type = r;
        this.estimatedRTT = rtt;
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
        this.estimatedRTT = (int) ((1 - this.alpha) *
                this.estimatedRTT + this.alpha * rtt);

        // absolute value of difference between rtt
        //  and estimatedRTT
        double absValue = Math.abs(rtt - this.estimatedRTT);

        this.devRTT = (1 - this.beta) * this.devRTT
                + this.beta * absValue;
    }



}
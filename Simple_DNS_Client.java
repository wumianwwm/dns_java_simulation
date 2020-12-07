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
}

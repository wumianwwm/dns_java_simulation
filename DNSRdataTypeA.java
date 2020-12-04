/**
 * the type A's DNSResourceRecord's rdata.
 * type value: 1.
 * one field: a 32-bit data which represents a IPv4 address.
 */
public class DNSRdataTypeA extends DNSRdata
{

    /** Constructor for encoding purpose:
     * Use to create a DNSRdataTypeA instance, and then encode it
     *  into a DNS message.
     *  @param
     *  dataStr - a string with IPV4 address format. like "129.100.0.79"*/
    public DNSRdataTypeA(String dataStr)
    {
        // the parsedIp orginally used for holding parsed IP address from
        // a DNS Resource Record Rdata field.
        // Now, we can use to to store the IP address that we will encode
        // using BigEndianEncoder later on.
        this.parsedIp = dataStr;
    }


    // constructor:
    // decode the Ip address which in 4 bytes
    public DNSRdataTypeA(BigEndianDecoder decoder)
    {
        // First initialized an empty string
        this.parsedIp = decoder.decodeIpv4();

    }

    @Override
    public String getInfo()
    {
        // return the parsed Ipv4 address
        return this.parsedIp;
    }

    @Override
    /** implement the abstract method. */
    public short getDataLength()
    {
        // TODO: implement this method.
        return 0;
    }

    @Override
    /** Implementing the interface:
     * @param encoderV: the BigEndianEncoder inside DNSMessage object. */
    public void encode(BigEndianEncoder encoderV)
    {
        // TODO: think about how to encode it?
    }
}

/**
 * the type A's DNSResourceRecord's rdata.
 * type value: 1.
 * one field: a 32-bit data which represents a IPv4 address.
 */
public class DNSRdataTypeA extends DNSRdata
{

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
    /** Implementing the interface:
     * @param encoderV: the BigEndianEncoder inside DNSMessage object. */
    public void encode(BigEndianEncoder encoderV)
    {
        // TODO: think about how to encode it?
    }
}

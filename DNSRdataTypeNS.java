/**
 * This class represents a rdata for resource record with type NS.
 * Type value: 2.
 * One field: a domain name.
 */
public class DNSRdataTypeNS extends DNSRdata
{

    // constructor:
    public DNSRdataTypeNS(BigEndianDecoder decoder)
    {
        this.dnsName = DNSName.decode(decoder);
    }

    @Override
    public String getInfo()
    {
        // return the NSDNAME parsed which stored in an DNSName object
        return this.dnsName.getName();
    }
}

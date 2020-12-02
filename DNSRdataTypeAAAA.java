/**
 * the type AAAA's DNSResourceRecord's rdata.
 * type value: 28.
 * one field: a 128-bit data which represents a IPv6 address.
 */
public class DNSRdataTypeAAAA extends DNSRdata
{
    public DNSRdataTypeAAAA(BigEndianDecoder decoder)
    {
        this.parsedIp = decoder.decodeIpv6();
    }

    @Override
    public String getInfo() {
        return this.parsedIp;
    }
}

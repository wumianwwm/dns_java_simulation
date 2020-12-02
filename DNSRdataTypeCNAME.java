/**
 * This class represents the rdata for resource record with type cname.
 * Type value: 5
 * One field: a domain name.
 */
public class DNSRdataTypeCNAME extends DNSRdata
{
    // constrcutor:
    public DNSRdataTypeCNAME(BigEndianDecoder decoder)
    {
        this.dnsName = DNSName.decode(decoder);
    }



    @Override
    public String getInfo()
    {
        return this.dnsName.getName();
    }
}

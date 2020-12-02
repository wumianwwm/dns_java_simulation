/**
 * the type MD, MF, MB, MG, MR, and PTR's DNSResourceRecord's rdata
 * type value: could be 3, 4, 7, 8, 9.
 * All these types have the same rdata format, which is
 * one field: a MADNAME: which is a domain name
 */
public class DNSRdataTypeMdfbgrPtr extends DNSRdata
{

    /**
     * Construct a DNSRdataTypeMdfbgrPtr object
     * @param decoder: the BigEndianDecoder originally
     *               from DNSMessage's constructor argument
     */
    public DNSRdataTypeMdfbgrPtr(BigEndianDecoder decoder)
    {

        this.dnsName = DNSName.decode(decoder);
    }


    @Override
    public String getInfo()
    {

        return "----";
    }
}

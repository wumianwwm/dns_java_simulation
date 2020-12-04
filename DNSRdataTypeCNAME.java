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

    @Override
    /** implement the abstract method. */
    public int getDataLength()
    {
        //for research project, no need for implementation here.
        return 0;
    }

    @Override
    /** Implementing the interface:
     * @param encoderV: the BigEndianEncoder inside DNSMessage object. */
    public void encode(BigEndianEncoder encoderV)
    {
        // Note: No need for implementation for this class.
        // Our simulation will use Type A resource record only.
    }
}

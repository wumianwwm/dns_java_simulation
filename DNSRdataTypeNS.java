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


    @Override
    /** implement the abstract method. */
    public short getDataLength()
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

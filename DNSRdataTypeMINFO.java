/**
 * Represents a DNS Resource Record with MINFO type's rdata.
 * type value: 14
 * It has two domain names inside, decode them to save in decoder
 * for the purpose of dynamic programming.
 */
public class DNSRdataTypeMINFO extends DNSRdata
{
    // the two domain name inside
    private DNSName n1;
    private DNSName n2;


    public DNSRdataTypeMINFO(BigEndianDecoder decoder)
    {
        this.n1 = DNSName.decode(decoder);
        this.n2 = DNSName.decode(decoder);
    }


    @Override
    public String getInfo()
    {

        return "----";
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

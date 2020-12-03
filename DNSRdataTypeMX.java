/**
 * This class represents a rdata for resource record with type MX.
 * Type value: 15.
 * One field: a domain name.
 */
public class DNSRdataTypeMX extends DNSRdata
{
    // the 16-bit integer which specifies the preference.
    private short reference;

    public DNSRdataTypeMX(BigEndianDecoder decoder)
    {
        // stub
        this.reference = decoder.decodeShort();
        this.dnsName = DNSName.decode(decoder);
    }


    @Override
    public String getInfo()
    {

        return this.dnsName.getName();
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

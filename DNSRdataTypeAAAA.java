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

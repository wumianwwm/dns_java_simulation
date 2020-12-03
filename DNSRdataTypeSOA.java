/**
 * Represent a DNS ResourceRecord type SOA's rdata.
 * Type value: 6.
 * It has two domain name inside, plus 20 bytes of data.
 *
 * We need to decode the two domain name and store them in decoder,
 * And skipp the 20 bytes of data.
 */
public class DNSRdataTypeSOA extends DNSRdata {


    private DNSName nmNAME;
    private DNSName rnNAME;

    public DNSRdataTypeSOA(BigEndianDecoder decoder)
    {
        // stub
        // TODO: check
        // This should not be skipped, caused the domain names here might
        // be pointed by other name pointers
        this.nmNAME = DNSName.decode(decoder);
        this.rnNAME = DNSName.decode(decoder);

        // recheck if the skip process is correct
        decoder.skip(20);
    }


    @Override
    public String getInfo()
    {
        // stub
        return "----";
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

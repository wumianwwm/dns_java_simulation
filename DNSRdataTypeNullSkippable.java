/**
 * Represent either Null, WKS or HINFO data type.
 * type value: 10, 11, 13
 * The null data type is experimental, so just skip those bytes
 * if encounter a DNS Resource Record with this type.
 *
 * The WKS and HINFO has data that is not domain name, just skip.
 */
public class DNSRdataTypeNullSkippable extends DNSRdata
{

    /**
     * Construct a new DNSRdataTypeNullSkippable object
     * @param decoder: the BigEndianDecoder originally from DNSMessage's constructor
     * @param rdataLength: number of bytes to skip
     */
    public DNSRdataTypeNullSkippable(BigEndianDecoder decoder, short rdataLength)
    {
        int numOfBytes = rdataLength & 0xFFFF;
        decoder.skip(numOfBytes);
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

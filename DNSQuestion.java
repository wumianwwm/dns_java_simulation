/**
 * This class represents the question section of a DNS message
 * for a query or a response.
 *
 * It implements the encodable interface so that the query can be encoded;
 * and it has a constructor supports the decoding of a DNS message from
 * a server's response.
 */
public class DNSQuestion implements Encodable
{

    // the Domain name we wish to query
    private DNSName domainName;
    private short qType;
    private short qClass;


    /**
     * Constructor for encoding purpose:
     * encode a domain name to a DNSQuestion
     * @param domainNameV: a domain name format like "xxx.xxxx.xxx"
     * @param r: the type of query we wish to ask, e.g. A, NS, AAAA, etc.
     */
    public DNSQuestion (String domainNameV, RecordType r)
    {
        this.domainName = new DNSLabels(domainNameV);
        this.qType = (short) r.getCode();
        this.qClass = (short) 0x1;
    }


    /** Constructor for encoding a DNS response's DNS question.
     * @param domainName - domain name in the question.
     * @param  queryType - type of the query
     * @param queryClass - class of the query */
    public DNSQuestion(String domainName, short queryType, short queryClass)
    {
        this.domainName = new DNSLabels(domainName);
        this.qType = queryType;
        this.qClass = queryClass;
    }


    /**
     * Constructor for decoding purpose:
     * docode a DNSQuestion object from the received dns response message.
     * @param decoder: a decoder that contains the bytes buffer of the whole dns response data.
     */
    public DNSQuestion (BigEndianDecoder decoder)
    {
        this.domainName = DNSName.decode(decoder);
        this.qType = decoder.decodeShort();
        this.qClass = decoder.decodeShort();
    }


    /** Helper method: get the domain name within the question field
     * @return : a string that represents the domain name */
    public String getDomainNameInQuestion()
    {

        return this.domainName.getName();
    }


    /** Helper method: get the query type.
     * @return: the qType */
    public short getqType()
    {

        return this.qType;
    }

    /** Helper method: get the query class
     * @return: the qClass */
    public short getqClass()
    {

        return this.qClass;
    }

    @Override
    public void encode(BigEndianEncoder encoderV)
    {
        // First get the Domain Name in string format
        String qName = this.domainName.getName();

        // encode the Domain Name's string
        encoderV.encodeName(qName);

        // now encode qType and qClass
        encoderV.encodeShort(this.qType);
        encoderV.encodeShort(this.qClass);
    }


}

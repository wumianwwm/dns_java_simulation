/**
 * DNSHeader represents the header of a DNS communication message (either a query or a response).
 * This class implements the Encodable interface to support encoding a query.
 * It provides two constructors: one for encoding and the other for decoding purpose.
 */
public class DNSHeader implements Encodable
{

    // the Query Id
    private short id;

    // total 16-bit flags, default setting is when encoding, all bits are set to 0.
    private short flag;

    // QDCOUNT: originally a 16-bit unsigned int.
    // number of questions
    private short questionCount;
    // ANCOUNT: originally a 16-bit unsigned int
    // Number of answers
    private short answerCount;
    // NSCOUNT: originally a 16-bit unsigned int
    // Number of name servers
    private short nameServerCount;
    // ARCOUNT: originally a 16-bit unsigned int
    // Number of additional Resource Records
    private short additionalFullRRCount;

    // default constrcutor
    // first used in Encode: 1 Question, no answer/ns/additional RR
    public DNSHeader (short queryId)
    {
        this.id = queryId;
        // set flag and four counts field.
        this.flag = 0x0000;
        this.questionCount = 1;
        this.answerCount = 0;
        this.nameServerCount = 0;
        this.additionalFullRRCount = 0;
    }


    /** Constructor for DNS server to encode a DNSHeader.
     *  Since the server needs to add resource records to the message,
     *  answer, nameServer, additionalFullRR, might not be zero.
     *  @param queryId - id of this dns response, should be same as query.
     *  @param headerFlag - flag in the DNS header, just leave as 0 for this project.
     *  @param questions - number of questions in Question section.
     *  @param answers - number of RRs in Answer section.
     *  @param nameServers - number of RRs in Name Server section.
     *  @param additions - number of RRs in Additional Info section.
     *
     *  Note: for this project, the server should send back a DNS response,
     *  with headerFlage be 0x0000; question 1; answer 1;
     *  no name server, or additional info needed. */
    public DNSHeader(short queryId, short headerFlag, short questions,
                     short answers, short nameServers, short additions)
    {
        this.id = queryId;
        this.flag = headerFlag;
        this.questionCount = questions;
        this.answerCount = answers;
        this.nameServerCount = nameServers;
        this.additionalFullRRCount = additions;
    }


    /**
     * Constructor for decoding purpose:
     * decode the response's data and thus construct a DNSHeader object for a DNSMessage object.
     * @param decoder: the BigEndianDecoder from DNSMessage's constructor's argument.
     */
    public DNSHeader (BigEndianDecoder decoder)
    {

        this.id = decoder.decodeShort();
        this.flag = decoder.decodeShort();
        this.questionCount = decoder.decodeShort();
        this.answerCount = decoder.decodeShort();
        this.nameServerCount = decoder.decodeShort();
        this.additionalFullRRCount = decoder.decodeShort();

    }


    /**
     * Helper method:
     * @return: number of answers in the DNS response message.
     */
    public int getAnswerCount()
    {
        return this.answerCount;
    }


    /**
     * Helper method:
     * @return: number of name servers in the DNS response message.
     */
    public int getNameServerCount()
    {
        return this.nameServerCount;
    }


    /**
     * Helper method:
     * @return: number of additional resource records in the DNS response message.
     */
    public int getAdditionalRRCount()
    {
        return this.additionalFullRRCount;
    }


    /**
     * Helper method:
     * Check if the response is from an authoritative server
     * @return: true if the server is authoritative, false otherwise.
     */
    public boolean isFromAuthoritative()
    {
        if ((this.flag & 0x0400) == 0)
        {
            // not from a authoritative server
            return false;
        }

        else return true;
    }


    /**
     * Helper method:
     * check if the response's DNSHeader's RCODE is correct.
     * @return true if all RCODE bits are zero, false otherwise.
     */
    public boolean isRCODEInFlagAllZero()
    {
        if ((this.flag & 0x000F) == 0)
        {
            return true;
        }
        else return false;
    }


    /**
     * Helper method:
     * get the query Id for printing purpose.
     * @return this.queryId that converted to an integer.
     */
    public int getQueryId()
    {

        return (this.id & 0xFFFF);
    }


    /** Helper method:
     * get the query Id, in short data type.
     * This function will be used for encoding a DNS response packet.
     * @return thid.id, in short data type */
    public short getQueryIdInShort()
    {

        return this.id;
    }

    @Override
    public void encode(BigEndianEncoder encoderV)
    {

        // First encode the id
        encoderV.encodeShort(this.id);
        // then encode the 16-bit flags
        encoderV.encodeShort(this.flag);
        // then encode QCOUNT
        encoderV.encodeShort(this.questionCount);
        // then encode ANCOUNT
        encoderV.encodeShort(this.answerCount);
        // then encode NSCOUNT
        encoderV.encodeShort(this.nameServerCount);
        // then encode ARCOUNT
        encoderV.encodeShort(this.additionalFullRRCount);


    }


}

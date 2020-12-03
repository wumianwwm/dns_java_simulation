import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * DNSMessage represents a communication message (either a query or a response)
 * which the format is specified by RFC1035, section 4.1.
 *
 * This class consists a DNSHeader, a DNSQuestion and three DNSResourceRecords objects
 * which stands for Answer, Authority and Additional section in a
 * communication message respectively.
 * One DNSMessage also has one BigEndianEncoder object to manage the encoding
 * of this specific DNS query.
 *
 * DNSMessage implements the Encodable interface,
 * this is achieved by using the implementation of Encodable interface
 * by DNSMessage components (DNSHeader and DNSQuestion)
 */
public class DNSMessage implements Encodable
{
    // one DNS message object will have
    // one encoder inside the class for encoding this message.
    private BigEndianEncoder encoder;

    // The real components of a DNS message
    private DNSHeader dnsHeader;
    private DNSQuestion dnsQuestion;
    private DNSResourceRecords dnsAnswers;
    private DNSResourceRecords dnsNameServers;
    private DNSResourceRecords dnsAdditionalRecords;


    /**
     * Constructor version 1:
     * construct a DNSMessage object for sending query.
     * @param domainNameV: the domain name we wish to query for.
     * @param randomV: the Random class' object to provide a random query Id.
     * @param r:       the type of query we wish to ask (e.g. A, AAAA, etc...)
     */
    public DNSMessage (String domainNameV, Random randomV, RecordType r)
    {
        this.encoder = new BigEndianEncoder();

        int queryId = randomV.nextInt(65536);
        this.dnsHeader = new DNSHeader((short) queryId);
        this.dnsQuestion = new DNSQuestion(domainNameV, r);
    }


    /**
     * Constructor version 2:
     * construct a DNSMessage which represents a response
     * from a DNS server.
     * @param decoder: the BigEndianDecoder which have been initialized
     *               to contain the bytes buffer corresponds to this DNSMessage.
     */
    public DNSMessage (BigEndianDecoder decoder)
    {
        // first construct the dnsHeader and dnsQuestion.
        this.dnsHeader = new DNSHeader(decoder);
        this.dnsQuestion = new DNSQuestion(decoder);
        // get counts of resource records in each section.
        int answerCount = this.getAnswerCount();
        int nsCount = this.getNameServerCount();
        int additionInfoCount = this.getAddtionalInfoCount();

        this.dnsAnswers = new DNSResourceRecords(decoder, answerCount);
        this.dnsNameServers = new DNSResourceRecords(decoder, nsCount);
        this.dnsAdditionalRecords = new DNSResourceRecords(decoder, additionInfoCount);

    }


    /**
     * Helper method:
     * get the answer count from dnsHeader
     * @return: an integer for number of answers.
     */
    public int getAnswerCount()
    {

        return this.dnsHeader.getAnswerCount();
    }


    /**
     * Helper method:
     * get the name serber count from dnsHeader
     * @return: an integer for number of name servers.
     */
    public int getNameServerCount()
    {

        return this.dnsHeader.getNameServerCount();
    }


    /**
     * Helper method:
     * get the additional count from dnsHeader
     * @return: an integer for number of additional resource records.
     */
    public int getAddtionalInfoCount()
    {

        return this.dnsHeader.getAdditionalRRCount();
    }


    /**
     * Helper method:
     * get queryId from DNSHeader
     * @return the queryId inside DNSHeader
     */
    public int getQueryId()
    {

        return this.dnsHeader.getQueryId();
    }


    /** Helper method:
     * get domain name in dns question
     * @return the domain name's string */
    public String getQueryName()
    {

        return this.dnsQuestion.getDomainNameInQuestion();
    }


    /**
     * Helper method:
     * check if the response if from the authoritative server
     * of the domain name.
     * @return: true if it is from the authoritative server,
     *          false otherwise.
     */
    public boolean isFromAuthoritative()
    {

        return this.dnsHeader.isFromAuthoritative();
    }

    /**
     * Helper method:
     * check if the message Response code is all zero
     * @return true if Response code is all zero, false otherwise.
     */
    public boolean isResponseCodeCorrect()
    {

        return this.dnsHeader.isRCODEInFlagAllZero();
    }


    /**
     * Adapter Method:
     * get A list of Resource Records in Answer/Authority/Additional Section
     * @param section: a all lower-case string:
     *               could be "answer", "authority" or "additional".
     * @return a list of resource records
     *
     * **** for simplicity of the research project, this method might not be used.
     */
//    public List<ResourceRecord> getRRinSection(String section)
//    {
//        switch (section)
//        {
//            case "answer":
//                return this.dnsAnswers.getResourceRecords();
//
//            case "authority":
//                return this.dnsNameServers.getResourceRecords();
//
//            case "additional":
//                return this.dnsAdditionalRecords.getResourceRecords();
//
//                default:
//                    // println for debugging purpose
//                    System.out.println("Error! Input is not supported" +
//                            " at DNSMessage, getRRinSection() method!");
//                    return Collections.emptyList();
//        }
//    }


    /**
     * Helper method:
     * return all the resource records decoded inside the DNS Message as a List.
     * So that all records can be later on used for saving to cache.
     * @return: a list of all resource records
     *      (answer + name server + additional section).
     *
     * **** for simplicity of the research project, this method might not be used.
     */
//    public List<ResourceRecord> getAllRecords()
//    {
//        List<ResourceRecord> r1 = this.getRRinSection("answer");
//        r1.addAll(this.getRRinSection("authority"));
//        r1.addAll(this.getRRinSection("additional"));
//        return r1;
//    }


    /**
     * Helper method:
     * get the encoder for passing as an arugment
     * @return this.encoder
     */
    public BigEndianEncoder getEncoder()
    {

        return this.encoder;
    }

    @Override
    /**
     * Implementing the interface:
     * @param encoderV: the BigEndianEncoder inside DNSMessage object.
     *                this parameter is get by calling
     *                this.getEncoder().
     */
    public void encode(BigEndianEncoder encoderV)
    {

        this.dnsHeader.encode(encoderV);
        this.dnsQuestion.encode(encoderV);
    }


    /**
     * Helper method:
     * get the bytes buffer from bigEndianEncoder
     * @return the bytes buffer converted from encoder
     */
    public byte[] tobytesBuffer()
    {

        return this.encoder.toBtyesBuffer();
    }





}

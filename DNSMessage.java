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

        // initialize the lists
        this.dnsAnswers = new DNSResourceRecords();
        this.dnsNameServers = new DNSResourceRecords();
        this.dnsAdditionalRecords = new DNSResourceRecords();
    }

    /** Constructor version 1 - altered:
     * @param domainName the domain name we wish to query for.
     * @param id transaction id for the query(0-65535).
     * @param r  should be A for research project. */
    public DNSMessage(String domainName, int id, RecordType r)
    {
        this.encoder = new BigEndianEncoder();
        this.dnsHeader = new DNSHeader((short)id);
        this.dnsQuestion = new DNSQuestion(domainName, r);
        // initialize lists
        this.dnsAnswers = new DNSResourceRecords();
        this.dnsNameServers = new DNSResourceRecords();
        this.dnsAdditionalRecords = new DNSResourceRecords();
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


    /** Constructor version 3:
     * Constructs a DNS response packets, which will be used by server and attacker.
     *
     * The basic logic is: the server/attacker has received the client's query packet.
     * The server/client do some work, to handle the query.
     * During the handling process, server/attacker creates resource records for
     *  each one of the Answer, Authority(nameSevers), Additional Info, section.
     *
     * a new flag will also be generated. Indicate any errors, and the packet is a
     * response packet. (See RFC1034, page 26, section 4.1.1)
     *
     * @param dnsQuery - the query sent from a DNS client
     * @param newFlag - the new flag used for DNSHeader.
     * @param answers - list of resource records in Answer section.
     * @param nameSevers - list of resource records in Name Severs(Authority) section.
     * @param additionalRecords - list of resource records in additional information.
     * */
    public DNSMessage (DNSMessage dnsQuery,
                       short newFlag,
                       DNSResourceRecords answers,
                       DNSResourceRecords nameSevers,
                       DNSResourceRecords additionalRecords)
    {
        // TODO: check, first we should create an encoder.
        this.encoder = new BigEndianEncoder();
        // get question from query
        this.dnsQuestion = dnsQuery.copyToNewQuestion();
        // now we update the three RRs
        this.dnsAnswers = answers;
        this.dnsNameServers = nameSevers;
        this.dnsAdditionalRecords = additionalRecords;
        // now we extract counts.
        short numOfAnswer = this.dnsAnswers.getRRCount();
        short numOfNS = this.dnsNameServers.getRRCount();
        short numOfAddtions = this.dnsAdditionalRecords.getRRCount();

        // now we can create a new DNS Header
        this.dnsHeader = newHeaderforResponse(dnsQuery, newFlag,
                numOfAnswer, numOfNS, numOfAddtions);
    }



    /** Helper method:
     * Use the dnsQuestion object to create a new DNSQuestion.
     * This method will be used when we want to construct a DNS response
     * message, corresponding to a DNS query.
     * @return: a new DNSQuestion object  or NULL */
    public DNSQuestion copyToNewQuestion()
    {
        if (this.dnsQuestion == null)
        {
            System.out.println("DNSMessage: question is null!");
            return null;
        }

        String domainName = this.dnsQuestion.getDomainNameInQuestion();
        short queryType = this.dnsQuestion.getqType();
        short queryClass = this.dnsQuestion.getqClass();
        return new DNSQuestion(domainName, queryType, queryClass);
    }


    /** Helper method:
     * Create a new DNSHeader as a DNS response to a DNS query message.
     * @param query: the DNSMessage received from a client.
     * @param newFlag: new flags to be set in new Header.
     * @param answerCount: number of RR(Resource Record)s in answer section.
     * @param nsCount: number of RRs in authority section.
     * @param additionCount: number of RRs in additional section.
     * @return a new DNSHeader object*/
    public DNSHeader newHeaderforResponse(DNSMessage query,
                                          short newFlag,
                                          short answerCount,
                                          short nsCount,
                                          short additionCount)
    {
        short queryId = (short) query.getQueryId();
        // question count
        short qCount = query.getQuestionCountFromHeader();
        return new DNSHeader(queryId, newFlag, qCount,
                answerCount, nsCount, additionCount);
    }


    /** Helper method:
     * get questionCount field in DNSHeader of this message.
     * @return questionCount in DNSHeader. */
    public short getQuestionCountFromHeader()
    {

        return this.dnsHeader.getQuestionCount();
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

    /** Helper method:
     * get question type in dns question.*/
    public short getQType()
    {

        return this.dnsQuestion.getqType();
    }

    /** Helper method:
     * get question class in dns question. */
    public short getQClass()
    {

        return this.dnsQuestion.getqClass();
    }


    /** Retrieve all answers from dnsAnswers,
     * get IP address of the query domain name.
     * @param qName domain name of query.
     * @param qType record type query looks for.
     * @return an array of IPv4 address of the domain
     *  name, in String format. */
    public String[] retrieveDNSAnswers(String qName, RecordType qType)
    {

        // retrieve rdata (e.g. IPv4 address) in DNS answer section.
        return this.dnsAnswers.getIPsOfName(qName, qType);
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


    /** Helper method:
     * print the whole DNS message */
    public void printDNSMessage()
    {
        System.out.println("Header Section:");
        this.dnsHeader.printDNSHeader();

        System.out.println("Question Section:");
        this.dnsQuestion.printDNSQuestion();

        System.out.println("Answer Section:");
        this.dnsAnswers.printResourceRecords();

        System.out.println("Authority Section:");
        this.dnsNameServers.printResourceRecords();

        System.out.println("Additional Section:");
        this.dnsAdditionalRecords.printResourceRecords();
    }

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
        // update: now we need to encode the three sections as well.
        this.dnsHeader.encode(encoderV);
        this.dnsQuestion.encode(encoderV);
        // the implementation in DNSResourceRecords will
        // ensure that, if list is empty, it will not
        // encode anything, just return.
        this.dnsAnswers.encode(encoderV);
        this.dnsNameServers.encode(encoderV);
        this.dnsAdditionalRecords.encode(encoderV);
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

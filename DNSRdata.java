/**
 * This abstract class represents the data session in a DNSResourceRecord.
 * Because different types of DNSResourceRecords have deferent Rdata format,
 * all its subclasses are corresponds to a specific type's Rdata.
 */
public abstract class DNSRdata implements Encodable
{

    // a field represent a domain name
    protected DNSName dnsName;
    // a field represent a parsed ip address,
    // could be ipv4 or ipv6
    protected String parsedIp;

    /** Factory method for encoding purpose:
     * Based on TYPE filed in ResourceRecord,
     * create appropriate DNSRdata
     * @param
     *  r - represents the type of resource record this Rdata belongs to.
     *  dataStr - data for DNSRdata, in String format
     *      Different type of Resource Record has different rdata format.
     *      So for now we use a String to serve as raw data buffer.
     *
     *      The subclass can use information in dataStr, to encode proper
     *      rdata based on format defined in RFC 1034.
     *
     *      For typeA, rdata should be a Ipv4 address.
     *      like "129.100.0.79" */
    public static DNSRdata createInstance(short r, String dataStr)
    {
        switch (r)
        {
            // supported type
            // For this project, we only support encode a TypeA Rdata.
            case 1:
                return new DNSRdataTypeA(dataStr);

            default:
                return null;
        }

    }


    // Factory method for decoding purpose:
    // Based on the TYPE field in ResourceRecord,
    // generate appropriate DNSRdata.
    public static DNSRdata decode(BigEndianDecoder decoder, short r, short rdLengthV)
    {

        switch (r)
        {
            // supported type
            case 1:
                return new DNSRdataTypeA(decoder);

            // supported type
            case 2:
                return new DNSRdataTypeNS(decoder);

            case 3:
                return new DNSRdataTypeMdfbgrPtr(decoder);

            case 4:
                return new DNSRdataTypeMdfbgrPtr(decoder);

            // supported type
            case 5:
                return new DNSRdataTypeCNAME(decoder);

            case 6:
                return new DNSRdataTypeSOA(decoder);

            case 7:
                return new DNSRdataTypeMdfbgrPtr(decoder);

            case 8:
                return new DNSRdataTypeMdfbgrPtr(decoder);

            case 9:
                return new DNSRdataTypeMdfbgrPtr(decoder);

            case 10:
                return new DNSRdataTypeNullSkippable(decoder, rdLengthV);

            case 11:
                return new DNSRdataTypeNullSkippable(decoder, rdLengthV);

            case 12:
                return new DNSRdataTypeMdfbgrPtr(decoder);

            case 13:
                return new DNSRdataTypeNullSkippable(decoder, rdLengthV);

            case 14:
                return new DNSRdataTypeMINFO(decoder);

            // supported type
            case 15:
                return new DNSRdataTypeMX(decoder);

            // supported type
            case 28:
                return new DNSRdataTypeAAAA(decoder);

                default:
                    // return an object that just skip the whole rdata
                    return new DNSRdataTypeNullSkippable(decoder, rdLengthV);
        }
    }


    // Abstract method:
    // get the information in DNSRdata:
    // For type A, type AAAA: this should return the parsedIp;
    //
    // For type NS, CNAME, MX: this should return name;
    public abstract String getInfo();

    /** Abstract method: get the length of DNSrdata, in number of bytes.
     * Since different types of RR have differnet rdata format,
     * each subclasses of DNSRdata need to implement this method,
     * based on their rdata format and size. */
    public abstract short getDataLength();

}

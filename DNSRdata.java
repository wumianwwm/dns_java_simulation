/**
 * This abstract class represents the data session in a DNSResourceRecord.
 * Because different types of DNSResourceRecords have deferent Rdata format,
 * all its subclasses are corresponds to a specific type's Rdata.
 */
public abstract class DNSRdata
{

    // a field represent a domain name
    protected DNSName dnsName;
    // a field represent a parsed ip address,
    // could be ipv4 or ipv6
    protected String parsedIp;

    // Factory method:
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

}

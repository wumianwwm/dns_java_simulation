/**
 *  This class represents a full resource record specified by RFC1035.
 *  In a response from a DNS server, the section of answer, authority and additional
 *  will all have a list of resource records.
 *
 *  All resource records have the same format:
 *  1. a Domain name, here represented by a DNSName object;
 *  2. a type value, which is a 16-bit integer;
 *  3. a class value, which is a 16-bit integer;
 *  4. a time-to-live value, which is a 32-bit integer;
 *  5. a rdata_length, which is a 16-bit integer for bytes counts in rdata filed;
 *  6. a rdata.
 *
 *  Different types of DNS Resource Records have different structure of rdata.
 *  Therefore the DNSRdata is an abstract data type.
 */
public class DNSResourceRecord
{
    // NAME field
    private DNSName dnsName;
    // TYPE field. represents the type of record (A, AAAA, NS, etc..)
    private short type;
    // the class code. This value is not important for this assignment.
    private short RRclass;
    // TTL field 32-bit;
    private int TTL;
    // RDlegnth
    private short rdLength;
    // Rdata
    private DNSRdata rdata;

    // converted Field:
    // This is not directly from decoding,
    // but from the short value type.
    private RecordType recordType;

    public DNSResourceRecord (BigEndianDecoder decoder)
    {
        // The factory mode build a DNSName
        this.dnsName = DNSName.decode(decoder);
        this.type = decoder.decodeShort();
        this.RRclass = decoder.decodeShort();
        this.TTL = decoder.decodeInt();
        this.rdLength = decoder.decodeShort();

        // set the recordType
        this.recordType = RecordType.getByCode(this.type & 0xFFFF);

        // The factory mode build a DNSRdate
        this.rdata = DNSRdata.decode(decoder,this.type, this.rdLength);

    }


    /**
     * Adapter method:
     * generate proper ResourceRecord object from DNSResourceRecord.
     * @return an ResourceRecord object
     *
     * For simplicity of the project, this method might not be used.
     */
//    public ResourceRecord getResourceRecordFromDNSRR()
//    {
//        // for the convention of printing results,
//        // all types of DNSResourceRecord will use this constructor.
//        // i.e. give string result even for ip address.
//        return new ResourceRecord(this.dnsName.getName(), this.recordType,
//                (long) this.TTL, this.rdata.getInfo());
//    }



}

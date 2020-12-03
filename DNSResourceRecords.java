import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DNSResourceRecords can be used to specify a list of DNSResourceRecords
 * in Answer, Authority or Additional section of the response DNSMessage
 * from a server.
 */
public class DNSResourceRecords implements Iterable<DNSResourceRecord>, Encodable
{
    private List<DNSResourceRecord> records;
    // this field is used for counting the number of
    // DNSResourceRecord that should be decoded
    private int recordCount;

    /**
     * Constructor:
     * @param decoder: The BigEndianDecoder from DNSMessage's constructor's argument.
     * @param count: number of resource records to decode.
     */
    public DNSResourceRecords(BigEndianDecoder decoder, int count)
    {

        this.records = new ArrayList<>();
        this.recordCount = count;

        // in the for loop generate new ResourceRecord,
        // then put into the list
        for (int i = 0; i < this.recordCount; i++)
        {
            DNSResourceRecord resourceRecord = new DNSResourceRecord(decoder);
            this.records.add(resourceRecord);
        }

    }


    /**
     * Adapter method:
     * when requires, convert all DNSResoureRecords it stores to ResourceRecord.
     * @return a List of ResourceRecords that each is got from DNSResourceRecord
     *
     * **** For simplicity of the research project, this method might not be used.
     */
//    public List<ResourceRecord> getResourceRecords()
//    {
//        List<ResourceRecord> recordList = new ArrayList<>();
//
//        for (DNSResourceRecord r : this.records)
//        {
//            ResourceRecord oneRecord = r.getResourceRecordFromDNSRR();
//            recordList.add(oneRecord);
//        }
//
//        return recordList;
//    }


    @Override
    public Iterator<DNSResourceRecord> iterator()
    {
        return this.records.iterator();
    }


    @Override
    /** Implementing the interface:
     * @param encoderV: the BigEndianEncoder inside DNSMessage object. */
    public void encode(BigEndianEncoder encoderV)
    {
        // TODO: think about how to encode it?
        // we need to call encode every resource record.
        // possibly, we need some consturctor for encoding purpose.
        // Or we need a addElement() method to add DNSRresourceRecord to list.
    }
}

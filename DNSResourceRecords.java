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

    /** Constructor for encoding purpose.
     * this constructor will only initialize the list.
     * We will use other method to add DNSResourceRecord,
     *  one-by-one, to the list. */
    public DNSResourceRecords()
    {
        this.records = new ArrayList<>();
        // this variable isn't useful for encoding, since we know
        // how many records will be inside each list.
        // Just update it so that it has the correct value.
        this.recordCount = 0;
    }

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

    /** Helper method to add one DNSResourceRecord into the list.
     * The function will also update the recordCount.
     * @param
     * resourceRecord: the object to be added into the list. */
    public void addOneRecord(DNSResourceRecord resourceRecord)
    {
        this.records.add(resourceRecord);
        this.recordCount = this.records.size();
    }

    /** Helper method:
     * This function is helpful when we try to encode a DNS response message.
     * For DNSHeader, we need to include the RR count in each section.
     * @return number of RR in the list records. */
    public short getRRCount()
    {
        // recordCount should be updated every time we add element to list.
        return (short) this.recordCount;
    }


    /** Helper method:
     * print every resource record in the list. */
    public void printResourceRecords()
    {
        if (this.recordCount == 0)
        {
            System.out.println("    No resource records are found.");
            return; // list is empty, just return.
        }

        for (DNSResourceRecord r : this.records)
        {
            r.printResourceRecord();
        }
        // when all RRs are printed, leave a blank line at the end.
        System.out.println(" ");
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
        // first check if there is any Resource Record in the lis.
        if (this.recordCount == 0)
        {
            // list is empty, just returns.
            return;
        }

        for (DNSResourceRecord record : this.records)
        {
            // let each resource record encode itself.
            record.encode(encoderV);
        }
    }
}

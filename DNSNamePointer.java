/**
 * The subclass of DNSName.
 * It represents the case when a name pointer is or is part of a domain name.
 */
public class DNSNamePointer extends DNSName
{

    public DNSNamePointer(BigEndianDecoder decoder)
    {
        int offset = decoder.getOffset();

        int pointerPosition = decoder.decodeShort() & 0x3FFF;

        this.name = decoder.getEntry(pointerPosition).getName();

        decoder.addEntry(offset, this);

    }


    @Override
    public void encode(BigEndianEncoder encoderV)
    {
        // stub
        // No need to implement this one
        // Because when encoding DNSQuestion, DNSLabels rather than
        // DNSNamePointer will be used for encoding the Domain name.
    }
}

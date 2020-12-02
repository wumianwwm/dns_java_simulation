/**
 * This represents a basic type extended from DNSName.
 *
 * By RFC1035, a DNSLabel in bytes buffer is defined as 2 components:
 * 1. a length byte,
 * 2. a sequence of bytes together represents a string in ASCII format.
 *
 * DNSLabel is a component of DNSLabels. But DNSLabels can also include a DNSNamePointer.
 *
 */
public class DNSLabel extends DNSName
{


    /**
     * Constructor for decoding purpose:
     * construct a DNSLabel from received message
     * @param decoder: the BigEndianDecoder object from DNSMessage's constructor's argument.
     */
    public DNSLabel(BigEndianDecoder decoder)
    {
        byte b = decoder.decodeByte();

        if (b == 0)
        {
            this.name = "";
        }
        else
        {
            this.name = decoder.decodeString( b & 0xFF);
        }

    }

    @Override
    public void encode(BigEndianEncoder encoderV)
    {
        // stub

    }
}

/**
 * This abstract class represents a domain name object.
 * A domain name can be either:
 * 1: a sequence of DNS Labels
 * 2: a name pointer pointing to a pre-existed domain name
 * 3: a sqeuence of DNS Labels plus a name pointer
 *
 * There are three classes that extends this abstract class:
 * 1: DNSLabel,
 * 2: DNSLabels,
 * 3: DNSNamePointer.
 *
 * Because in DNSQuestion, we also have a DNSName to encode,
 * this abstract class implements the Encodable interface.
 * However, because our DNS query will not compress a domain name,
 * only DNSLabels will actually provide implementation for encoding purpose.
 *
 * See the subclasses for more information of why there are three subclasses
 * and their data structure.
 */
public abstract class DNSName implements Encodable
{
    protected String name;

    // helper method
    // Return the decoded string of this DNSName
    public String getName()
    {
        return this.name;
    }


    /**
     * static factory method:
     * Base on the first byte to determine the actual class of the DNSName.
     * If the first byte is a name pointer, the generated DNSName will
     * be a DNSNamePointer object.
     *
     * If the first byte is not a pointer, but a byte indicates length of bytes
     * of the label e.g. 0x03 (followed by 0x77, 0x77, 0x77...),
     * the actual type of DNSName will be DNSLabels.
     * @param decoder: the decoder contains all bytes inside a buffer
     *               that represents a response from a server.
     * @return a DNSName object.
     */
    public static DNSName decode(BigEndianDecoder decoder)
    {
        byte firstByte = decoder.peekByte();

        if ((firstByte & 0xC0) == 0xC0)
        {
            return new DNSNamePointer(decoder);
        }
        else
        {
            return new DNSLabels(decoder);
        }
    }



}

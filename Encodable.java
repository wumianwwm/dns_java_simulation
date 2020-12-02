/**
 * An object that can be encoded to a component of a dns query.
 *
 * The user of this interface can have abstract way to encode the
 * data they wish into a DNS query message.
 *
 * The interface has one method: encode(BigEndianEncoder encoderV).
 * This method provides an argument which type is BigEndainEncoder.
 *
 * By using the methods provided by encoderV object, the classes that
 * implement this Interface does not need to worry about details of
 * converting data into bytes, and store in proper order and format.
 */
public interface Encodable
{
    /**
     * To be implemented by DNSMessage, DNSHeader, DNSQuestion, DNSName
     * @param encoderV: The BigEndianEncoder that comes from the
     *                private variable in DNSMessage.
     */
    void encode(BigEndianEncoder encoderV);
}

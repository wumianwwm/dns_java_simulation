import java.util.HashMap;

/**
 * This class represent a decoder which will be used during
 * the process of decoding a DNS response from a server.
 * The object will pass as argument to each DNSMessage components'
 * constructors. In order to help us to decode and then construct
 * a DNSMessage object.
 */
public class BigEndianDecoder
{
    // the received bytes buffer of a DNS message
    private byte[] receivedBuffer;

    // the cursor of where the current byte should be decoded
    // the Instance of the class in charge of updating this cursor
    //
    // i.e. the first byte that we haven't decoded
    // (although we may have a look the value of this byte
    //      via     this.peekByte()     method.)
    private int offset;


    // a HashMap to maintain parsed Domain Name
    // key: int (offset)
    // value: the dnsName object contains the domain name starting at this position
    //
    // e.g.
    // at offset 0x30 is www.cs.ubc.ca
    // the offset at 0x33 should be cs.ubc.ca
    // the offset at 0x35 should be ubc.ca
    // the offset at 0x38 should be ca
    // all four names' correspond dnsName objects will be stored in the hashMap
    //
    // One DNSMessage should have one BigEndianDecoder
    // and thus one parsedName HashMap.
    private HashMap<Integer, DNSName> nameTable;

    /**
     * constructor:
     * use the received buffer to construct a new dns Decoder
     * @param receivedBufferV: the received bytes buffer from a datagramPacket.
     */
    public BigEndianDecoder(byte[] receivedBufferV)
    {
        this.receivedBuffer = receivedBufferV;
        this.offset = 0;
        this.nameTable = new HashMap<>();
    }


    /**
     *  return the current offset value.
     *     Notice that all decoding method except peekByte(),
     *     will eventually increment the offset,
     *     So sometimes the first thing to do is calling this method,
     *     So that you know where the start of your domain name label's.
     * @return: the current offset value
     */
    public int getOffset()
    {
        return this.offset;
    }


    /**
     * Helper method:
     * when some data are not necessary, e.g. SOA's rdata, there are 20 bytes
     * that we do not care.
     * So we update the offset to skip decoding these bytes
     * @param numOfBytes: the number of bytes we wish to skip
     */
    public void skip(int numOfBytes)
    {

        this.offset += numOfBytes;
    }


    /**
     * Helper method:
     * pick one byte value, but do not update the offset
     * @return: one byte at receivedBuffer[offset]
     */
    public byte peekByte()
    {
        // do not increment offset
        return this.receivedBuffer[this.offset];
    }


    /**
     * Basic decoding method:
     * return the byte and update the offset.
     * @return: the current byte at receivedBuffer[offset]
     */
    public byte decodeByte()
    {
        // First get the current byte
        byte b = this.receivedBuffer[this.offset];
        // then increment the offset
        // to point to the next byte that should be decoded.
        this.offset += 1;
        return b;
    }

    /**
     * Basic decoding method:
     * decode a 16-bit short value that is represented at
     * receivedBuffer[offset], receivedBuffer[offset + 1].
     *
     * This method does not need to manage the offset updates,
     * since it calls decodeByte() to get those two bytes' values.
     * @return the decoded short value
     */
    public short decodeShort()
    {
        // e.g. the short we want is 0x0123
        // In receviedBuffer, this should be [..., 0x01, 0x23, ...]
        // first get the higher byte
        // so its value would be 0x01
        byte higherByte = this.decodeByte();
        // get the lower byte, which should be 0x23
        byte lowerByte = this.decodeByte();

        int higherBToInt = (higherByte & 0xFF) << 8;
        int lowerBToInt = lowerByte & 0xFF;
        return (short) (higherBToInt | lowerBToInt);
    }


    /**
     * Basic decoind method:
     * decode a 32-bit integer value that is represented at
     * receivedBuffer[offset], receivedBuffer[offset + 1],
     * receivedBuffer[offset + 2], receivedBuffer[offset + 3].
     *
     * This method does not need to manage the offset updates,
     * since it calls decodeShort() to get two short values.
     * @return a decoded 32-bit integer
     */
    public int decodeInt()
    {

        // e.g. the 32-bit int we want is 0x12345678
        // In receivedBuffer, this should be [..., 0x12, 0x34, 0x56, 0x78, ...]

        // First get the higher two-bytes short,
        // so higherShort should be 0x1234.
        short higherShort = this.decodeShort();
        // Now get the lower two-bytes short,
        // so lowerShort should be 0x5678.
        short lowerShort = this.decodeShort();

        // higherSToInt should be 0x12340000
        int higherSToInt = (higherShort & 0xFFFF) << 16;
        // lowerSToInt should be 0x00005678
        int lowerSToInt = lowerShort & 0xFFFF;

        return (higherSToInt | lowerSToInt);
    }


    /**
     * Convenient method:
     * Decode a sequence of bytes and put into a byte array.
     *
     * This method itself does not need to manage offset update,
     * since it calls decodeByte() to get bytes it need.
     * @param amountV: amount of bytes we wish to decode
     * @return a byte array containing these bytes.
     */
    public byte[] decodeBytes(int amountV)
    {
        byte[] bytes = new byte[amountV];

        for (int i = 0; i < amountV; i++)
        {
            bytes[i] = this.decodeByte();
        }
        return bytes;
    }

    /**
     * Basic decoding method:
     * decode a string represented in buffer as:
     * receivedBuffer[offset], ..., receivedBuffer[offset + length]:
     * which each byte's value represent a ASCII char.
     *
     * This method will update (increase) the offset by length.
     * @param length: the number of bytes that the target string consists of
     * @return: a decoded string
     */
    public String decodeString(int length)
    {
        // This will return the string in ASCII format.
        // e.g. receivedBuffer[0x32] to receivedBuffer[0x34] all have value 0x77,
        // the parsed string will be "www" at offset 0x32
        String str = new String(this.receivedBuffer, this.offset, length);
        // update the offset
        this.offset += length;
        return str;
    }



    /**
     * Basic decoding method:
     * decode an IPv4 address that represented by 4 bytes in receivedBuffer.
     *
     * This method itself does not manage the offset,
     * since it calls decodeBytes() to get the 4 bytes.
     * @return a parsed string e.g. "145.123.2.32"
     */
    public String decodeIpv4()
    {
        StringBuilder builder = new StringBuilder();
        // The 4 bytes represents Ipv4 address
        byte[] theFourBytes = this.decodeBytes(4);

        for (int i = 0; i < 3; i++)
        {
            // Use & operation with integer to remove sign of byte.
            builder.append(theFourBytes[i] & 0xFF);
            builder.append('.');
        }
        // append the last byte
        builder.append(theFourBytes[3] & 0xFF);


        return builder.toString();
    }

    /**
     * Basic decoding method:
     * decode an IPv6 address that represented by 16 bytes in the buffer.
     *
     *  This method itself does not need to manage offset,
     *  since it calls decodeIpv6Helper() to get each 4 hex value in a string.
     * @return a string like "1abc:0:3214:6789:8907:b212:bcd1:2eac"
     */
    public String decodeIpv6()
    {
        StringBuilder builder = new StringBuilder();
        // get the 8 segments and append to the StringBuilder
        for (int i = 0; i < 8; i++)
        {
            String oneSegment = this.decodeIpv6Helper();
            builder.append(oneSegment);
            // now append the ':' character
            builder.append(':');
        }

        // delete the last ':' character
        builder.deleteCharAt(builder.toString().length() - 1);

        return builder.toString();
    }

    /**
     * Helper method:
     * decode one of the eight Ipv6 sub-address which takes 2 bytes
     * e.g. {..., 3b, 24, ...}
     * @return a parsed string like "3b24"
     */
    private String decodeIpv6Helper()
    {
        StringBuilder builder = new StringBuilder();

        // decode 2 bytes for this segement
        byte[] twoBytes = this.decodeBytes(2);

        // append
        for (int i = 0; i < 2; i++)
        {
            builder.append(String.format("%02x", twoBytes[i]));
        }

        // This string could be something like "0023"
        // need to take the higher 00 off.
        String rawString = builder.toString();
        String returnedString;
        int cursor = 0;
        while (rawString.charAt(cursor) == '0')
        {
            cursor += 1;
            if (cursor == rawString.length())
            {
                // the raw string is "0000"
                return "0";
            }
        }

        returnedString = rawString.substring(cursor);


        return returnedString;
    }


    /**
     * Dynamic programing method:
     * After decoded a DNSName object, store it at the nameTable.
     *
     * @param offset: the starting position of the domain name in receivedBuffer
     * @param dnsName: the DNSName object corresponds to the domain name
     */
    public void addEntry(int offset, DNSName dnsName)
    {

        this.nameTable.put(offset, dnsName);
    }


    /**
     * Dynamic programing method:
     * When we try to find a domain name at a offset indicated by a pointer,
     * goes to the nameTable to get the DNSName object.
     * @param offset: the offset which decoded from a NamePointer
     * @return a DNSName object which contains the name that supposed to be represent
     *          at this offset, if no compression is involved.
     */
    public DNSName getEntry(int offset)
    {

        return this.nameTable.get(offset);
    }



}

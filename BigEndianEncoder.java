import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * This class handles the encoding of a DNS query. It contains a list of bytes
 * that stores the encoded bytes. And provides methods to get the encoded bytes buffer
 * from the list.
 *
 * The encoding is in Big Endian. That is the higher bytes get encoded first to list;
 * and in the bytes buffer, the higher bytes should appear first than lower bytes.
 *
 * Although when encoding a query, we do not need to worry about encoding Resource Records,
 * but the methods provided by this class can also be used to encode Resource Records
 * if needed.
 */
public class BigEndianEncoder
{
    // the list is going to store
    // all bytes that encoded
    private ArrayList<Byte> bytesList;

    /**
     * Construct a new BigEndianEncoder object
     */
    public BigEndianEncoder()
    {
        this.bytesList = new ArrayList<>();
    }


    /**
     * Basic encoding method:
     * Encode one byte value that adds to the byteList.
     * @param b: the byte that is going to be encoded
     */
    public void encodeByte(byte b)
    {
        // just add one byte to the list
        this.bytesList.add(b);
    }


    /**
     * Convenient method:
     * Encode an array of bytes that will be added to the byteList;
     * The bytes[0] will be the higher byte so encoding should be ascending order.
     * @param bytes: an array of bytes to be encoded
     */
    public void encodeBytes(byte[] bytes)
    {
        for (int i = 0; i < bytes.length; i++)
        {
            this.encodeByte(bytes[i]);
        }
    }


    /**
     * Basic encoding method:
     * Encodes a 16-bit (2 bytes) short number, and add the
     * higer byte as well as the lower byte to the byteList.
     * @param s: the 16-bit short number to be encoded
     */
    public void encodeShort(short s)
    {

        byte higerByte = (byte) (s >> 8);
        byte lowerByte = (byte) s;

        // First encode higher byte
        // Then encode lower byte
        this.encodeByte(higerByte);
        this.encodeByte(lowerByte);

    }


    /**
     * Basic encoding method:
     * Encodes a string which represents a domain name.
     * The length of each substring and the last 0x00 byte will be added
     * as required by RFC1035.
     * @param name: a string like "xxx.xxxxx.xxx"
     */
    public void encodeName(String name)
    {
        // first split the name by "." dot
        String[] splittedName = name.split("\\.+");

        // use a for loop, encode each split string
        for (int i = 0; i < splittedName.length; i++)
        {
             try
             {
                 // first convert string to a byte array
                 // e.g. "www" now should be {0x77, 0x77, 0x77}
                 byte[] toBytes = splittedName[i].getBytes("ASCII");
                 // get the length byte to be encoded
                 byte labelCount = (byte) toBytes.length;
                 // encode to the list
                 this.encodeByte(labelCount);
                 // encode the byte array
                 this.encodeBytes(toBytes);

             }
             catch (UnsupportedEncodingException e)
             {
                 // stub
             }
        }

        // at the end, encode 00 to indicate the end of domain Name
        this.encodeByte((byte) 0x00);

    }


    /**
     * Helper method:
     * when the encoding of a DNS query is completed,
     * return the byte array which gonna to be sent to a DNS server.
     * @return: a bytes buffer from byteList
     */
    public byte[] toBtyesBuffer()
    {

       int length = this.bytesList.size();
       byte[] bytesBuffer = new byte[length];

       for (int i = 0; i < length; i++)
       {
           Byte b = this.bytesList.get(i);
           bytesBuffer[i] = b.byteValue();
       }

        return bytesBuffer;
    }

}

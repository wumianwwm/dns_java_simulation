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


    /** Basic encoding method:
     * Encode a 32-bit integer, in big-endian format.
     * Lets's say the int has value 0x10203040
     * in buffer it will be [... 0x10, 0x20, 0x30, 0x40, ...]
     * @param i: the 32-bit intger to be encoded. */
    public void encodeInt(int i)
    {
        // higherShort is 0x1020
        short higherShort = (short) (i >> 16);
        // lowerShort is 0x3040
        short lowerShort = (short) i;
        // Now we first encode the higherShort's two bytes
        //  then we encode the lower two bytes.
        this.encodeShort(higherShort);
        this.encodeShort(lowerShort);
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


    /** Basic encoding method: Encodes an IPv4 address
     * IPv4 address in a DNS rdata, is a 32-bit(4 bytes) integer.
     * For research project, this method is used for encoding
     * Type A Resource Record's rdata.
     * @param ipv4Addr - a string that looks like "129.100.0.79" */
    public void encodeIPv4(String ipv4Addr)
    {
        // if any error occurs, encode 0.0.0.0 as IPv4 address

        // a 4 byte short variable that help us encode 0.0.0.0
        int wrongData = 0;
        // firsr we split the string by "."
        String[] splittedStr = ipv4Addr.split("\\.+");
        // debug purpose. The length of splittedStr should be 4.
        if (splittedStr.length != 4)
        {
            System.out.println("encodeIPv4: incorrect split string length. ");
            // Encode two short variable.
            this.encodeInt(wrongData);
            return;
        }

        int byteBalue; // use it for get integer value from a sub string
        // Use a for loop, to convert each string to a
        byte[] bytesArray = new byte[4]; // a 4-bytes array for our data
        for (int i = 0; i < splittedStr.length; i++)
        {
            try
            {
                byteBalue = Integer.parseInt(splittedStr[i]);
                // if no error, add the byte value to bytesBuffer.
                bytesArray[i] = (byte) byteBalue;
            }catch (NumberFormatException n)
            {
                System.out.println("encodeIPv4: can't convert string to number");
                // encode 2 short variables with value 0, and returns.
                this.encodeInt(wrongData);
                return;
            }
        }

        // Now we left the loop, no exception was caught.
        // Encode the array of bytes.
        this.encodeBytes(bytesArray);
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

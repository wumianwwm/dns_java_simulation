// a DNSLabels try to represent a Domain name decoded from received buffer.
// one DNSLabel in byte buffer is
// byte (length), byte (ASCII character), byte (ASCII character)....
// e.g  [0x03][0x77][0x77][0x77] which represent www.
//
// In bytes buffer
// One DNS labels can be a sequence of labels + a zero byte indicate the end.
// e.g [0x03][0x77][0x77][0x77] [0x03][0x75][0x62][0x63] [0x02][0x63][0x61] [0x00]
// which represents     www.ubc.ca.
//
// Or a DNSLabels could be DNSLables + a Pointer
// e.g [0x05][0x75][0x67][0x72][0x61][0x64] [0xc0][0x41]
// which represents     ugrad.ubc.ca (Assume offset at 0x41's address is ubc.ca).
//
// So each DNSLabels will consists of two parts
// first one is DNSName called head, which could be a DNSLabel or a NamePointer.
//
// The rest is a DNSLabels called tail.
//
// When construct a DNSLabels, recursively construct head and tail.
public class DNSLabels extends DNSName
{

    private DNSName head;
    private DNSLabels tail;


    /**
     * Constructor for encoding purpose:
     * Encodes a DNSLabels when encode the DNSName for DNSQuestion.
     * @param name: the domain name we wish to query.
     */
    public DNSLabels(String name)
    {
        //
        this.name = name;
    }


    /**
     * Constructor for decoding purpose:
     * construct a DNSLabels object when this constructor is called
     * in DNSName's factory method.
     * @param decoder: the BigEndianDecoder contains the whole bytes buffer
     *               which represents data of the DNS response from a server.
     */
    public DNSLabels(BigEndianDecoder decoder)
    {
        // first record the offset at this position
        // later on store (offset, DNSLabels) to the hashMap inside decoder
        int offset = decoder.getOffset();

        byte firstByte = decoder.peekByte();

        // check the firstByte on whether it indicates a pointer or not
        if ((firstByte & 0xC0) == 0)
        {
            // not pointer, start to decode label
            this.head = new DNSLabel(decoder);

            byte afterHead = decoder.peekByte();
            if (afterHead == 0)
            {
                // afterHead is 0x00, indicate the end of a Domain Name,
                // as well as the end of DNS labels.
                // after head is 0x00, so you should skip one byte!!!!
                decoder.skip(1);
                // the head is the last label since afterHead is 0x00,
                // so there has no tail.
                this.tail = null;
                this.name = this.head.getName();
            }

            if (afterHead != 0)
            {
                // there is something after the head label.
                // either another label ([lengthByte, 0x.., 0x.., 0x.., ....]);
                // or a Name pointer ([c0, offset]).
                //
                // Here we call the DNSLabels constructor recursively
                // for the tail portion of THIS dnsLabels object.
                this.tail = new DNSLabels(decoder);
                this.name = this.head.getName() + "." + this.tail.getName();
            }
        }
        else
        {
            // the head is a name Pointer.
            // So we reached a Name pointer,
            // there should be no tails after the Name pointer.
            this.head = new DNSNamePointer(decoder);
            this.tail = null;
            this.name = this.head.getName();
        }
        // we finished decoding the DNSLebels;
        // Now we need to add the result to the table inside decoder.
        //
        // This is required by the dynamic programming method used in decoding.
        decoder.addEntry(offset, this);
    }



    @Override
    public void encode(BigEndianEncoder encoderV)
    {

        encoderV.encodeName(this.name);

    }
}

package org.fluxoid.utils.crc;

public class Crc16Utils {

    public static int[] ANSI_CRC16_TABLE = new int[] { 0, 0xcc01, 0xd801, 0x1400, 0xf001, 0x3c00, 0x2800, 0xe401, 0xa001,
            0x6c00, 0x7800, 0xb401, 0x5000, 0x9c01, 0x8801, 0x4400};


    //https://wiki.wxwidgets.org/Development:_Small_Table_CRC
    public static int[] makeCrcTable(int crc_poly)
    {
        int i, val, result;
        int [] crc_table = new int[16];

        for (val = 0; val < 16; val++)
        {
            result = val;

            for (i = 0; i < 4; i++)
                if ((result & 1) != 0)
                    result = (result >>> 1) ^ crc_poly;
                else
                    result >>>= 1;

            crc_table[val] = result;


        }
        return crc_table;
    }

    public static int[] makeCrcTable256(int crc_poly)
    {
        int i, val, result;
        int [] crc_table = new int[256];

        for (val = 0; val < 256; val++)
        {
            result = val;

            for (i = 0; i < 8; i++)
                if ((result & 1) != 0)
                    result = (result >>> 1) ^ crc_poly;
                else
                    result >>>= 1;

            crc_table[val] = result;


        }
        return crc_table;
    }

    public static int computeCrc(int[] crc_table, int old_crc, byte [] data)
    {
        int i;

        for (i = 0; i < data.length; i++)
        {
		/* XOR in the data. */
            old_crc ^= 0xff & data[i];

		/* Perform the XORing for each nibble */
            old_crc = (old_crc >>> 4) ^ crc_table[old_crc & 0x0f];
            old_crc = (old_crc >>> 4) ^ crc_table[old_crc & 0x0f];
        }

        return (old_crc);
    }

    public static int computeCrc256(int[] crc_table, int old_crc, byte [] data)
    {
        int i;

        for (i = 0; i < data.length; i++)
        {
		/* XOR in the data. */
            old_crc ^= 0xff & data[i];

		/* Perform the XORing for each byte */
            old_crc = (old_crc >>> 8) ^ crc_table[old_crc & 0xff];
        }

        return (old_crc);
    }

    public static void printTable(int [] table) {
        for (int i : table) {
            System.out.printf("0x%04x,", i);
        }
        System.out.println();
    }


    public static void main(String [] args) {
        //CRC-16-IBM
        printTable(makeCrcTable(0xA001)); // little endian variant
        printTable(makeCrcTable256(0xA001)); // little endian variant
        byte [] data = new byte [] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef,1,2,3,44,6,-23,-12,-128,-64};
        System.out.println(computeCrc(ANSI_CRC16_TABLE,0,data));
        System.out.println(computeCrc256(makeCrcTable256(0xA001),0,data));
    }
}

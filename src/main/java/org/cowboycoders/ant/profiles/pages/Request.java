package org.cowboycoders.ant.profiles.pages;

import static org.cowboycoders.ant.profiles.BitManipulation.PutUnsignedNumIn1LeBytes;
import static org.cowboycoders.ant.profiles.BitManipulation.UnsignedNumFrom1LeByte;

/**
 * p 70
 * Created by fluxoid on 09/02/17.
 */
public class Request implements AntPage {

    public static final int REQEUSTED_PAGE_OFFSET = 6;
    public static final int SUBPAGE_OFFSET = 3;
    public static int PAGE_NUMBER = 70;
    private final int pageNumber;
    private final int subPage;

    public int getRequestedPageNumber() {
        return pageNumber;
    }

    public int getSubPage() {
        return subPage;
    }

    public Request(final byte[] data) {
        pageNumber = UnsignedNumFrom1LeByte(data[REQEUSTED_PAGE_OFFSET]);
        subPage =  UnsignedNumFrom1LeByte(data[SUBPAGE_OFFSET]);
    }

    @Override
    public int getPageNumber() {
        return PAGE_NUMBER;
    }

    public static class RequestPayload implements AntPacketEncodable {
        private int pageNumber;
        private int subPage = 0xff;

        public int getPageNumber() {
            return pageNumber;
        }

        public RequestPayload setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public int getSubPage() {
            return subPage;
        }

        public RequestPayload setSubPage(int subPage) {
            this.subPage = subPage;
            return this;
        }

        @Override
        public void encode(byte[] packet) {
            PutUnsignedNumIn1LeBytes(packet, AntPage.PAGE_OFFSET, PAGE_NUMBER);
            PutUnsignedNumIn1LeBytes(packet, 1,  0xff);
            PutUnsignedNumIn1LeBytes(packet, 2,  0xff);
            PutUnsignedNumIn1LeBytes(packet, REQEUSTED_PAGE_OFFSET, pageNumber);
            PutUnsignedNumIn1LeBytes(packet, 4,  0xff);
            PutUnsignedNumIn1LeBytes(packet, 5,  1);
            PutUnsignedNumIn1LeBytes(packet, SUBPAGE_OFFSET, subPage);
            PutUnsignedNumIn1LeBytes(packet, 7,  1);
        }
    }

}

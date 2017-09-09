package org.cowboycoders.ant.profiles.pages;

import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.profiles.common.utils.PayloadUtils;
import org.fluxoid.utils.bytes.LittleEndianArray;

/**
 * p 70
 * Created by fluxoid on 09/02/17.
 */
public class Request implements AntPage {

    public static final int REQEUSTED_PAGE_OFFSET = 6;
    public static final int SUBPAGE_OFFSET = 3;
    public static final int MANUFACTURER_PAGE_NUM = 80;
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
        LittleEndianArray viewer = new LittleEndianArray(data);
        pageNumber = viewer.unsignedToInt(REQEUSTED_PAGE_OFFSET, 1);
        subPage = viewer.unsignedToInt(SUBPAGE_OFFSET, 1);
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
            LittleEndianArray viewer = new LittleEndianArray(packet);
            viewer.putUnsigned(AntPage.PAGE_OFFSET, 1, PAGE_NUMBER);
            viewer.putUnsigned(1, 1, 0xff);
            viewer.putUnsigned(2, 1, 0xff);
            viewer.putUnsigned(REQEUSTED_PAGE_OFFSET, 1, pageNumber);
            viewer.putUnsigned(4, 1, 0xff);
            viewer.putUnsigned(5, 1, 1);
            viewer.putUnsigned(SUBPAGE_OFFSET, 1, subPage);
            viewer.putUnsigned(7, 1, 1);
        }
    }

    private static BroadcastDataMessage genSimpleRequest(int pageNum) {
        return PayloadUtils.getBroadcastDataMessage(
                new RequestPayload().setPageNumber(pageNum)
        );
    }

    public static BroadcastDataMessage getManufacturer() {
     return genSimpleRequest(MANUFACTURER_PAGE_NUM);
    }

}

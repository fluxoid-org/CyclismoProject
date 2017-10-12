package org.cowboycoders.ant.profiles.common;

import static org.cowboycoders.ant.profiles.common.ManufacturerIds.GARMIN;
import static org.cowboycoders.ant.profiles.common.ManufacturerIds.HEALTHANDLIFE;

public class DeviceIds {

    private DeviceIds() {}

    // manufacurer id : 257
    public enum HealthLife  implements DeviceDescriptor {
        AIT_SPORT_WATCH(1314);
        private final int id;

        HealthLife(int id) {
            this.id = id;
        }

        public int getDeviceId() {
            return id;
        }

        public int getManufacturerId() {
            return HEALTHANDLIFE;
        }

    }

    public enum Garmin implements DeviceDescriptor {
        ALF04 (1341),
        AMX (1461),
        ANDROID_ANTPLUS_PLUGIN (65532),
        APPROACH_S6 (1936),
        AXB01 (3),
        AXB02 (4),
        AXH01 (2),
        BCM (10),
        BSM (9),
        CHIRP (1253),
        CONNECT (65534),
        DSI_ALF01 (1011),
        DSI_ALF02 (6),
        EDGE1000 (1836),
        EDGE200 (1325),
        EDGE200_TAIWAN (1555),
        EDGE500 (1036),
        EDGE500_CHINA (1387),
        EDGE500_JAPAN (1213),
        EDGE500_KOREA (1422),
        EDGE500_TAIWAN (1199),
        EDGE510 (1561),
        EDGE510_ASIA (1821),
        EDGE510_JAPAN (1742),
        EDGE510_KOREA (1918),
        EDGE800 (1169),
        EDGE800_CHINA (1386),
        EDGE800_JAPAN (1334),
        EDGE800_KOREA (1497),
        EDGE800_TAIWAN (1333),
        EDGE810 (1567),
        EDGE810_CHINA (1822),
        EDGE810_JAPAN (1721),
        EDGE810_TAIWAN (1823),
        EDGE_REMOTE (10014),
        EDGE_TOURING (1736),
        EPIX (1988),
        FENIX (1551),
        FENIX2 (1967),
        FR10 (1482),
        FR10_JAPAN (1688),
        FR110 (1124),
        FR110_JAPAN (1274),
        FR15 (1903),
        FR210_JAPAN (1360),
        FR220 (1632),
        FR220_CHINA (1931),
        FR220_JAPAN (1930),
        FR301_CHINA (473),
        FR301_JAPAN (474),
        FR301_KOREA (475),
        FR301_TAIWAN (494),
        FR310XT (1018),
        FR310XT_4T (1446),
        FR405 (717),
        FR405_JAPAN (987),
        FR50 (782),
        FR60 (988),
        FR610 (1345),
        FR610_JAPAN (1410),
        FR620 (1623),
        FR620_CHINA (1929),
        FR620_JAPAN (1928),
        FR70 (1436),
        FR910XT (1328),
        FR910XT_CHINA (1537),
        FR910XT_JAPAN (1600),
        FR910XT_KOREA (1664),
        FR920XT (1765),
        HRM1 (1),
        HRM2SS (5),
        HRM3SS (7),
        HRM_RUN (1752),
        HRM_RUN_SINGLE_BYTE_PRODUCT_ID (8),
        INVALID (0xffff),
        SDM4 (10007),
        SWIM (1499),
        TEMPE (1570),
        TRAINING_CENTER (20119),
        VECTOR_CP (1381),
        VECTOR_SS (1380),
        VIRB_ELITE (1735),
        VIRB_REMOTE (1853),
        VIVO_ACTIVE (1907),
        VIVO_FIT (1837),
        VIVO_FIT2 (2150),
        VIVO_KI (1885),
        VIVO_SMART (1956);

        private final int id;

        Garmin(int id) {
            this.id = id;
        }

        public int getDeviceId() {
            return id;
        }

        public int getManufacturerId() {
            return 1;
        }

        public static int manufacturerId() {
            return GARMIN;
        }

    }
}

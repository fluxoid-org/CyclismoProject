package org.cowboycoders.ant.profiles.common;

public class Defines {

    public enum GenericCommandStatus {
        FAIL(1),
        NOT_SUPPORTED(2),
        PASS(0),
        PENDING(4),
        REJECTED(3),
        UNINITIALIZED(5),
        UNRECOGNIZED(-1);

        private int intValue;

        private GenericCommandStatus(final int intValue) {
            this.intValue = intValue;
        }

        public static GenericCommandStatus getValueFromInt(final int intValue) {
            for (final GenericCommandStatus commandStatus : values()) {
                if (commandStatus.getIntValue() == intValue) {
                    return commandStatus;
                }
            }
            final GenericCommandStatus unrecognized = GenericCommandStatus.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }
}
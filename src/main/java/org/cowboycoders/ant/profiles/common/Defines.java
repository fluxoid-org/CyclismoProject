package org.cowboycoders.ant.profiles.common;

public class Defines {

    public enum CommandStatus {
        FAIL(1),
        NOT_SUPPORTED(2),
        PASS(0),
        PENDING(4),
        REJECTED(3),
        UNINITIALIZED(5),
        UNRECOGNIZED(-1);

        private int intValue;

        private CommandStatus(final int intValue) {
            this.intValue = intValue;
        }

        public static CommandStatus getValueFromInt(final int intValue) {
            for (final CommandStatus commandStatus : values()) {
                if (commandStatus.getIntValue() == intValue) {
                    return commandStatus;
                }
            }
            final CommandStatus unrecognized = CommandStatus.UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }

        public int getIntValue() {
            return this.intValue;
        }
    }
}
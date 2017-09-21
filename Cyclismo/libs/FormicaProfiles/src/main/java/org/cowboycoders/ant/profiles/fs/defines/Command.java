package org.cowboycoders.ant.profiles.fs.defines;

public enum Command {
    CHANGE_FREQUENCY(2),
    REQUEST_AUTH(4),
    REQUEST_PING(5),
    REQUEST_DOWNLOAD(9),
    UNKNOWN(Integer.MAX_VALUE)

    ;
    private final int cmdId;

    Command(int cmdId) {
        this.cmdId = cmdId;
    }

    public byte encode(byte [] data, int offset) {
        return (byte) (data[offset] | cmdId);
    }

    public static Command decode (byte [] data, int offset) {
        int target = (data[offset] & 0xff);
        for (Command cmd: Command.values()) {
            if (cmd.cmdId == target) {
                return cmd;
            }
        }
        return UNKNOWN;
    }

}

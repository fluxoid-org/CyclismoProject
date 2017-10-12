package org.cowboycoders.ant.profiles.fs.pages;

    public enum AuthMode {
        PASSTHROUGH,
        SERIAL,
        PAIRING,
        PASSKEY,
        UNKNOWN;

        public static AuthMode[] vals = AuthMode.values();

        public static AuthMode decode(byte data) {
            int i = 0xff & data;
            if (i >= vals.length) {
                return UNKNOWN;
            }
            return vals[i];
        }

    }


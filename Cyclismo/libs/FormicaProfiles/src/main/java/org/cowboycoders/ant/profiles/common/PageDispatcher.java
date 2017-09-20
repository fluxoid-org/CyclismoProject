package org.cowboycoders.ant.profiles.common;

import org.cowboycoders.ant.profiles.fitnessequipment.pages.*;
import org.cowboycoders.ant.profiles.fs.pages.BeaconAdvert;
import org.cowboycoders.ant.profiles.fs.pages.CommandFactory;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.cowboycoders.ant.profiles.pages.ManufacturerInfo;
import org.cowboycoders.ant.profiles.pages.Request;
import org.fluxoid.utils.bytes.LittleEndianArray;


import java.util.logging.Logger;


import static org.cowboycoders.ant.profiles.pages.AntPage.PAGE_OFFSET;

/**
 * Created by fluxoid on 30/12/16.
 */
public class PageDispatcher extends FilteredBroadcastMessenger<AntPage> {

    public AntPage decode(byte[] data) {
        final int page = getPageNum(data);
        switch (page) {
            // FEC
            case 1:
                return new CalibrationResponse(data);
            case 2:
                return new CalibrationProgress(data);
            case 16:
                return new GeneralData(data);
            case 17:
                return new GeneralSettings(data);
            case 18:
                return new MetabolicData(data);
            case 21:
                return new BikeData(data);
            case 25:
                return new TrainerData(data);
            case 26:
                return new TorqueData(data);
            case 48:
                return new PercentageResistance(data);
            case 49:
                return new TargetPower(data);
            case 50:
                return new WindResistance(data);
            case 51:
                return new TrackResistance(data);
            case 54:
                return new CapabilitiesPage(data);
            case 55:
                return new ConfigPage(data);
            case 70:
                return new Request(data);
            case 71:
                return new Command(data);
            case ManufacturerInfo.PAGE_NUMBER:
                return new ManufacturerInfo(data);
            // ANT-FS
            case BeaconAdvert.PAGE_NUM:
                return new BeaconAdvert(data);
            case CommandFactory.PAGE_NUM:
                // sub page directory
                return CommandFactory.decode(data);

        }
        return null;
    }

    public static int getPageNum(byte[] data) {
        LittleEndianArray array = new LittleEndianArray(data);
        return array.unsignedToInt(PAGE_OFFSET,1);
    }

    public boolean dispatch(final byte[] data) {
        AntPage page = decode(data);
        if (page != null) {
            send(page);
            return true;
        } else {
            Logger.getGlobal().warning("no handler for page: " + data[PAGE_OFFSET]);
        }
        return false;
    }
}

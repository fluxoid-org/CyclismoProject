package org.cowboycoders.ant.profiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.ChannelEventHandler;
import org.cowboycoders.ant.ChannelId;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.SlaveChannelType;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.messages.data.DataMessage;
import org.cowboycoders.ant.profiles.common.PageDispatcher;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.*;
import org.cowboycoders.ant.profiles.pages.AntPage;
import org.cowboycoders.ant.profiles.pages.Request;
import org.cowboycoders.ant.profiles.simulators.NetworkKeys;

import static org.cowboycoders.ant.profiles.common.PageDispatcher.getPageNum;
import static org.fluxoid.utils.Format.bytesToString;

/**
 * Created by fluxoid on 02/07/17.
 */
public class FecProfile {

    private Channel channel;

    public static void main(String [] args) {
        AntTransceiver antchip = new AntTransceiver(0);
        Node node = new Node(antchip);
        node.start();
        node.reset();
        new FecProfile().start(node);
    }


    private Logger logger = LogManager.getLogger();

    public void requestCapabilities() {
        requestPage(CapabilitiesPage.PAGE_NUMBER);
    }

    public void requestConfig() {
        requestPage(ConfigPage.PAGE_NUMBER);
    }

    public void requestCmdStatus() {
        requestPage(Command.PAGE_NUMBER);
    }

    public void requestCalibration() {
        requestPage(CalibrationResponse.PAGE_NUMBER);
    }

    public void requestStatusBasic() {
        requestPage(PercentageResistance.PAGE_NUMBER);
    }

    public void requestStatusTrack() {
        requestPage(TrackResistance.PAGE_NUMBER);
    }

    public void requestStatusWind() {
        requestPage(WindResistance.PAGE_NUMBER);
    }


    private void requestPage(int pageNumber) {
        byte [] data = new byte[8];
        new Request.RequestPayload()
                .setPageNumber(pageNumber)
                .encode(data);
        BroadcastDataMessage payload = new BroadcastDataMessage();
        payload.setData(data);
        channel.send(payload);
    }


    public void start(Node transceiver) {

        final PageDispatcher pageDispatcher = new PageDispatcher();

        pageDispatcher.addListener(Request.class, new BroadcastListener<Request>() {

            @Override
            public void receiveMessage(Request request) {
                final int page = request.getRequestedPageNumber();
                System.out.print("request for page: " + page);

            }
        });

        pageDispatcher.addListener(AntPage.class, new BroadcastListener<AntPage>() {

            @Override
            public void receiveMessage(AntPage page) {
                final int pageNum = page.getPageNumber();
                if (page instanceof Request) {
                    // separate handler above
                    return;
                }
                System.out.print("got page: " + page);

            }
        });


        channel = transceiver.getFreeChannel();
        ChannelType type = new SlaveChannelType(false, false);
        channel.assign(NetworkKeys.ANT_SPORT, type);
        channel.setId(ChannelId.WILDCARD,17,ChannelId.WILDCARD,false);
        channel.setPeriod(8192);
        channel.setFrequency(57);
        channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);
        channel.registerRxListener(new BroadcastListener<DataMessage>() {
            @Override
            public void receiveMessage(DataMessage msg) {
                byte [] data = msg.getPrimitiveData();

                pageDispatcher.dispatch(data);
                Defines.CommandId cmd = Defines.CommandId.getValueFromInt(getPageNum(data));
                if (cmd != Defines.CommandId.UNRECOGNIZED && cmd != Defines.CommandId.NO_CONTROL_PAGE_RECEIVED) {
                    logger.trace("receieved cmd: " + cmd);
                }
                logger.trace(bytesToString(msg.getData()));
            }
        }, DataMessage.class);

        channel.registerEventHandler(new ChannelEventHandler() {
            @Override
            public void onTransferNextDataBlock() {

            }

            @Override
            public void onTransferTxStart() {

            }

            @Override
            public void onTransferTxCompleted() {

            }

            @Override
            public void onTransferTxFailed() {

            }

            @Override
            public void onChannelClosed() {

            }

            @Override
            public void onRxFailGoToSearch() {

            }

            @Override
            public void onChannelCollision() {

            }

            @Override
            public void onTransferRxFailed() {

            }

            @Override
            public void onRxSearchTimeout() {

            }

            @Override
            public void onRxFail() {

            }

            @Override
            public void onTxSuccess() {

            }
        });

        channel.open();

    }
}

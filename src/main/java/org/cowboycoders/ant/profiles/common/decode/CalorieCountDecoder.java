package org.cowboycoders.ant.profiles.common.decode;

import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.decode.interfaces.CalorieCountDecodable;
import org.cowboycoders.ant.profiles.common.events.CalorieBurntUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;

;


/**
 * Created by fluxoid on 10/01/17.
 */
public class CalorieCountDecoder<T extends CalorieCountDecodable> implements Decoder<T> {


    private final FilteredBroadcastMessenger<TaggedTelemetryEvent> bus;
    private long caloriesBurnt;
    private CalorieCountDecodable prev;


    public CalorieCountDecoder(FilteredBroadcastMessenger<TaggedTelemetryEvent> updateHub) {
        this.bus = updateHub;
        reset();
    }

    public void reset() {
        caloriesBurnt = 0;
    }


    @Override
    public void update(T newPage) {
        if (prev == null) {
            prev = newPage;
            return;
        }
        caloriesBurnt += newPage.getCalorieDelta(prev);
        prev = newPage;
        bus.send(new CalorieBurntUpdate(newPage.getClass(), caloriesBurnt));
    }

    @Override
    public void invalidate() {
        prev = null;
    }
}

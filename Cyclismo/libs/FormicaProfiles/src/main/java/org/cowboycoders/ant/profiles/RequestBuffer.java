package org.cowboycoders.ant.profiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.pages.AntPage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestBuffer implements BroadcastListener<AntPage> {

    private static final long TIMEOUT = TimeUnit.SECONDS.toNanos(1);
    private static final int MAX_RETRIES = 10;
    private Logger logger = LogManager.getLogger();

    private List<RequestFor<? extends AntPage>> buffer = new LinkedList<>();
    private Long sentTimeStamp = null;
    private int retries;

    private void reset() {
        sentTimeStamp = null;
        retries = 0;
    }

    @Override
    public void receiveMessage(AntPage o) {
        if (buffer.size() < 1) {
            return;
        }
        RequestFor<? extends AntPage> current = buffer.get(0);
        long timeStamp = System.nanoTime();
        if (sentTimeStamp == null || timeStamp - sentTimeStamp > current.getTimeOut()) {
            if (retries >= current.getMaxRetries()) {
                doAccept(); // stop the requests
                logger.warn("max retries exceeded for: " + current.clazz);
                return;
            }
            current.performRequest();
            retries += 1;
            sentTimeStamp = timeStamp;
            return;
        }
        if (current.accept(o)) {
            doAccept();
        }

    }

    private void doAccept() {
        buffer.remove(0);
        reset();
    }

    public void request(RequestFor<? extends AntPage> request) {
        buffer.add(request);
    }

    public abstract static class RequestFor<T extends AntPage> {

        private final Class<T> clazz;

        public RequestFor(Class<T> clazz) {
            this.clazz = clazz;
        }

        public boolean accept(AntPage page) {
            return clazz.isInstance(page);

        }
        public abstract void performRequest();
        public long getTimeOut() {
            return TIMEOUT;
        }
        public int getMaxRetries() {
            return MAX_RETRIES;
        }
    }
}

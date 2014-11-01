package org.nanomsg;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RequestReplyTest extends AbstractTest {
    private static final String DATE_REQUEST = "DATE";

    private int node0(String address){
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_REP);
        assert sock >= 0;
        assert Nanomsg.nn_bind(sock, address) >= 0;
        final byte msgbuf[] = new byte[255];

        while (true){
            int bytes = Nanomsg.nn_recv (sock, msgbuf, 0);
            assert bytes >= 0;

            final String msg = new String(msgbuf,0, bytes);

            if(DATE_REQUEST.equals(msg)){
                logger.info("NODE0: received request '{}'", msg);
                final String dateResponseStr = date();
                logger.info("NODE0: sending response '{}'", dateResponseStr);

                byte raw[] = dateResponseStr.getBytes();
                bytes = Nanomsg.nn_send(sock, raw, 0);
                assert raw.length == bytes;
            }

            if("Goodbye.".equals(msg)){
                logger.debug("NODE0: Received termination message, processing will be terminated");
                break;
            }
        }

        return Nanomsg.nn_shutdown(sock, 0);
    }

    private int node1 (String address){
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_REQ);
        assert sock >= 0;
        assert Nanomsg.nn_connect(sock, address) >= 0;
        logger.info("NODE1: sending request '{}'", DATE_REQUEST);

        byte raw[] = DATE_REQUEST.getBytes();
        int bytes = Nanomsg.nn_send(sock, raw, 0);
        assert raw.length == bytes;

        raw = new byte[500];
        bytes = Nanomsg.nn_recv(sock, raw, 0);
        assert bytes>=0;
        String resp = new String(raw, 0, bytes);

        logger.info("NODE1: received response '{}'", resp);
        return Nanomsg.nn_shutdown(sock, 0);
    }

    @Test
    public void test() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(1);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                node0("ipc:///tmp/reqrep.ipc");
            }
        });

        node1("ipc:///tmp/reqrep.ipc");
        logger.info("Waiting 1sec while node0 preforms graceful shutdown");
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }
}

package org.nanomsg;

import org.junit.Test;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PipelineTest extends AbstractTest {

    private int node0(String address){
        int sock = Nanomsg.nn_socket (Nanomsg.AF_SP, Nanomsg.NN_PULL);
        assert sock >= 0;
        assert Nanomsg.nn_bind(sock, address) >= 0;

        final ByteBuffer bb =ByteBuffer.allocateDirect(255);
        while (true) {

            int bytes = Nanomsg.nn_recv(sock, bb, 0);
            assert bytes >= 0;
            final byte[] raw = new byte[bytes];
            bb.get(raw);

            final String msg = new String(raw);
            logger.info("NODE0: received '{}'", msg);

            if("Goodbye.".equals(msg)){
                logger.debug("NODE0: Received termination message, processing will be terminated");
                break;
            }

            bb.rewind();
        }
        return Nanomsg.nn_shutdown(sock, 0);
    }

    private int node1 (String address, String msg)
    {
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_PUSH);
        assert sock >= 0;
        assert Nanomsg.nn_connect(sock, address) >= 0;

        logger.info("NODE1: Sending '{}'", msg);

        final byte[] raw = msg.getBytes();
        final ByteBuffer bb = ByteBuffer.allocateDirect(raw.length);
        bb.put(raw);
        bb.flip();

        int bytes = Nanomsg.nn_send(sock, bb, 0);

        assert bytes == raw.length;
        return Nanomsg.nn_shutdown(sock, 0);
    }

    @Test
    public void test() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(1);

        executorService.submit(new Runnable() {
           @Override
           public void run() {
               node0("ipc:///tmp/pipeline.ipc");
           }
        });

        node1("ipc:///tmp/pipeline.ipc", "Hello, world!");
        node1("ipc:///tmp/pipeline.ipc", "Goodbye.");

        logger.info("Waiting 1sec while node0 preforms graceful shutdown");
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }
}

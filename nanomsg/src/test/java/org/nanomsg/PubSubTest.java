package org.nanomsg;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PubSubTest extends AbstractTest {

    private int server(String address) throws InterruptedException {
        int sock = Nanomsg.nn_socket (Nanomsg.AF_SP, Nanomsg.NN_PUB);
        assert sock >= 0;
        assert Nanomsg.nn_bind(sock, address) >= 0;

        while (true){
            String dateStr = date();
            logger.info("SERVER: publishing date '{}'", dateStr);
            final byte raw[] = dateStr.getBytes();
            int bytes = Nanomsg.nn_send(sock, raw, 0);
            assert raw.length == bytes;
            Thread.sleep(1000);
        }
        //return Nanomsg.nn_shutdown(sock, 0);
    }

    private int client(String address, String name){
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_SUB);
        assert sock >= 0;

        // TODO learn more about publishing/subscribe keys
        assert Nanomsg.nn_setsockopt_str(sock, Nanomsg.NN_SUB, Nanomsg.NN_SUB_SUBSCRIBE, "") >= 0;
        assert Nanomsg.nn_connect(sock, address) >= 0;

        final byte raw[] = new byte[255];
        while (true){
            int bytes = Nanomsg.nn_recv(sock, raw, 0);
            assert bytes >= 0;

            final String msg = new String(raw, 0, bytes);
            logger.info("CLIENT ({}): received '{}'", name, msg);
        }
        //return Nanomsg.nn_shutdown(sock, 0);
    }

    private Runnable createClient(final String address, final String name){
        return new Runnable() {
            @Override
            public void run() {
                client(address, name);
            }
        };
    }

    @Test(timeout = 7000)
    public void test() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    server("ipc:///tmp/pubsub.ipc");
                } catch (InterruptedException e) {
                    logger.error("SERVER: ", e);
                }
            }
        });

        executorService.submit(
            createClient("ipc:///tmp/pubsub.ipc", "client0")
        );

        executorService.submit(
                createClient("ipc:///tmp/pubsub.ipc", "client1")
        );

        executorService.submit(
                createClient("ipc:///tmp/pubsub.ipc", "client2")
        );

        Thread.sleep(5000);
        executorService.shutdown();
    }
}


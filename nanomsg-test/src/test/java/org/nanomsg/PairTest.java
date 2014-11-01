package org.nanomsg;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PairTest extends AbstractTest {
    private int send_name(int sock, String name){
        logger.info("{}: sending '{}'", name.toUpperCase(), name);
        return Nanomsg.nn_send(sock, name.getBytes(), 0);
    }

    private int recv_name(int sock, String name){
        byte raw[] = new byte[255];

        int result = Nanomsg.nn_recv (sock, raw, 0);
        if (result > 0){
            final String msg = new String(raw,0,result);
            logger.info("{}: received '{}'", name, msg);
        }
        return result;
    }

    private int send_recv(int sock, String name) throws InterruptedException {
        int to = 100;
        assert Nanomsg.nn_setsockopt_int(sock, Nanomsg.NN_SOL_SOCKET, Nanomsg.NN_RCVTIMEO, to) >= 0;

        while(true){
            recv_name(sock, name);
            Thread.sleep(1000);
            send_name(sock, name);
        }
    }

    private int node0(String address) throws InterruptedException {
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_PAIR);
        assert sock >= 0;
        assert Nanomsg.nn_bind(sock, address) >= 0;
        send_recv(sock, "NODE0");
        return Nanomsg.nn_shutdown(sock, 0);
    }

    private int node1(String address) throws InterruptedException {
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_PAIR);
        assert sock >= 0;
        assert Nanomsg.nn_connect(sock, address) >= 0;
        send_recv(sock, "NODE1");
        return Nanomsg.nn_shutdown(sock, 0);
    }

    @Test(timeout = 5000)
    public void test() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    node0("ipc:///tmp/pair.ipc");
                } catch (InterruptedException e) {
                    logger.error("NODE0: ", e);
                }
            }
        });

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    node1("ipc:///tmp/pair.ipc");
                } catch (InterruptedException e) {
                    logger.error("NODE1: ", e);
                }
            }
        });

        Thread.sleep(3000);
        executorService.shutdown();
    }
}

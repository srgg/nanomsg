package org.nanomsg;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusTest extends AbstractTest {

    private int node(String address, String name, String... connections) throws InterruptedException {
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_BUS);

        assert sock >= 0;
        assert Nanomsg.nn_bind(sock, address) >= 0;

        Thread.sleep(1000); // wait for connections

        if(connections.length >0 ) {
            for(String a: connections){
                assert Nanomsg.nn_connect(sock, a) >0;
            }
        }

        Thread.sleep(1000); // wait for connections

        int to = 100;
        assert Nanomsg.nn_setsockopt_int(sock, Nanomsg.NN_SOL_SOCKET, Nanomsg.NN_RCVTIMEO, to) >= 0;

        // SEND
        logger.info("{}: sending '{}' onto bus", name.toUpperCase(), name);
        byte raw[] = name.getBytes();
        int send = Nanomsg.nn_send(sock, raw, 0);
        assert send == raw.length;

        raw = new byte[255];
        while (true){
            // RECV
            int recv = Nanomsg.nn_recv(sock, raw, 0);
            if (recv >= 0){
                final String msg = new String(raw, 0, recv);
                logger.info("{}: received '{}' from bus", name.toUpperCase(), msg);
            }
        }
        //return Nanomsg.nn_shutdown(sock, 0);
    }

    private Runnable createNode(final String address, final String name, final String...connections){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    node(address, name, connections);
                } catch (InterruptedException e) {
                    logger.error(name.toUpperCase() + ":", e);
                }
            }
        };
    }

    @Test(timeout = 5000)
    public void test() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        executorService.submit(
                createNode("ipc:///tmp/node0.ipc", "node0", "ipc:///tmp/node1.ipc", "ipc:///tmp/node2.ipc")
        );

        executorService.submit(
                createNode("ipc:///tmp/node1.ipc", "node1", "ipc:///tmp/node2.ipc", "ipc:///tmp/node3.ipc")
        );

        executorService.submit(
                createNode("ipc:///tmp/node2.ipc", "node2", "ipc:///tmp/node3.ipc")
        );

        executorService.submit(
                createNode("ipc:///tmp/node3.ipc", "node3", "ipc:///tmp/node0.ipc")
        );

        Thread.sleep(3000);
        executorService.shutdown();
    }
}

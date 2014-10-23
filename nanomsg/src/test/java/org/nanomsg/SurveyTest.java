package org.nanomsg;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SurveyTest extends AbstractTest {

    private int server(String address) throws InterruptedException {
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_SURVEYOR);
        assert sock >= 0;
        assert Nanomsg.nn_bind(sock, address) >= 0;
        Thread.sleep(1000); // wait for connections

        logger.info("SERVER: sending date survey request");
        byte raw[] = date().getBytes();
        int bytes = Nanomsg.nn_send(sock, raw, 0);
        assert bytes == raw.length;

        assert Nanomsg.ETIMEDOUT != -1;
        raw = new byte[255];
        while(true){
            bytes = Nanomsg.nn_recv (sock, raw, 0);
            if (bytes == Nanomsg.ETIMEDOUT) break;
            if (bytes >= 0){
                final String msg = new String(raw, 0, bytes);
                logger.info("SERVER: received '{}' survey response", msg);
            }
        }
        return Nanomsg.nn_shutdown(sock, 0);
    }

    private int client(String address, String name){
        int sock = Nanomsg.nn_socket(Nanomsg.AF_SP, Nanomsg.NN_RESPONDENT);
        assert sock >= 0;
        assert Nanomsg.nn_connect(sock, address) >= 0;

        byte raw[] = new byte [255];
        while (true) {
            int bytes = Nanomsg.nn_recv(sock, raw, 0);
            if(bytes >= 0){
                final String req = new String(raw, 0, bytes);
                logger.info("CLIENT ({}): received '{}' survey request", name, req);

                final String dateStr = date();
                logger.info("CLIENT ({}): sending date survey response '{}'", name, dateStr);

                raw = dateStr.getBytes();
                bytes = Nanomsg.nn_send(sock, raw, 0);
                assert bytes == raw.length;
            }
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

    @Test(timeout = 5000)
    public void test() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    server("ipc:///tmp/survey.ipc");
                } catch (InterruptedException e) {
                    logger.error("SERVER: ", e);
                }
            }
        });

        executorService.submit(
            createClient("ipc:///tmp/survey.ipc", "client0")
        );

        executorService.submit(
                createClient("ipc:///tmp/survey.ipc", "client1")
        );

        executorService.submit(
                createClient("ipc:///tmp/survey.ipc", "client2")
        );

        Thread.sleep(3000);
        executorService.shutdown();
    }
}

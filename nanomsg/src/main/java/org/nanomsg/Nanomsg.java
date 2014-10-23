package org.nanomsg;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Nanomsg {

    public static native void nn_term();
    public static native int nn_errno();
    public static native String nn_strerror(int errnum);

    public static native int nn_socket(int domain, int protocol);
    public static native int nn_close(int socket);
    public static native int nn_bind(int socket, String address);
    public static native int nn_connect(int socket, String address);
    public static native int nn_shutdown(int socket, int how);

    public static int nn_send(int socket, ByteBuffer buffer, int flags){
        final int pos = buffer.position();
        return nn_send(socket, buffer, pos, buffer.limit()-pos, flags);
    }

    public static int nn_send(int socket, byte array[], int flags){
        return nn_send(socket, array, 0, array.length, flags);
    }

    public static int nn_send(int socket, byte[] array, int offset, int length, int flags){
        return nn_sendArray(socket, array, offset, length, flags);
    }

    public static native int nn_send(int socket, ByteBuffer buffer, int offset, int length, int flags);
    private static native int nn_sendArray(int socket, byte[] array, int offset, int length, int flags);

    public static int nn_recv(int socket, ByteBuffer buffer,int flags){
        return nn_recv(socket, buffer, buffer.position(), buffer.remaining(), flags);
    }

    public static int nn_recv(int socket, byte array[],int flags){
        return nn_recvArray(socket, array, 0, array.length, flags);
    }

    private static native int nn_recvArray(int socket, byte[] array, int offset, int length, int flags);
    public static native int nn_recv(int socket, ByteBuffer buffer, int offset, int length, int flags);

    public static native int nn_getsockopt_int(int socket, int level, int optidx, Integer optval);
    public static native int nn_setsockopt_int(int socket, int level, int optidx, int optval);
    public static native int nn_setsockopt_str(int socket, int level, int optidx, String optval);


    public static int AF_SP = -1;
    public static int NN_PULL = -1;
    public static int NN_PUSH = -1;
    public static int NN_REP = -1;
    public static int NN_REQ = -1;
    public static int NN_PAIR = -1;
    public static int NN_PUB = -1;
    public static int NN_SUB = -1;
    public static int NN_SURVEYOR = -1;
    public static int NN_RESPONDENT = -1;
    public static int NN_SUB_SUBSCRIBE = -1;
    public static int NN_BUS = -1;

    public static int NN_SOL_SOCKET = -1;
    public static int NN_RCVTIMEO = -1;


    public static int ETIMEDOUT = -1;

    static {
        NarSystem.loadLibrary();
        final Map<String, Integer> symbols = new HashMap<String, Integer>();
        final int qnt = loadAllSymbols(symbols);

        AF_SP = symbols.get("AF_SP");
        NN_PULL = symbols.get("NN_PULL");
        NN_PUSH = symbols.get("NN_PUSH");
        NN_REP = symbols.get("NN_REP");
        NN_REQ = symbols.get("NN_REQ");
        NN_PAIR = symbols.get("NN_PAIR");
        NN_PUB = symbols.get("NN_PUB");
        NN_SUB = symbols.get("NN_SUB");
        NN_SURVEYOR = symbols.get("NN_SURVEYOR");
        NN_RESPONDENT = symbols.get("NN_RESPONDENT");
        NN_BUS = symbols.get("NN_BUS");

        NN_SUB_SUBSCRIBE = symbols.get("NN_SUB_SUBSCRIBE");

        NN_SOL_SOCKET = symbols.get("NN_SOL_SOCKET");
        NN_RCVTIMEO = symbols.get("NN_RCVTIMEO");

        ETIMEDOUT = symbols.get("ETIMEDOUT");
    }

    private static native int loadAllSymbols(Map symbols);
}

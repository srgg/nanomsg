#include <assert.h>
#include <nanomsg/nn.h>
#include <nanomsg/tcp.h>
#include <nanomsg/pubsub.h>

#include "org_nanomsg_Nanomsg.h"

#define ASSERT(x) assert(x)

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_loadAllSymbols(JNIEnv* env, jclass cls, jobject map)
{
    jclass cmap = 0;
    jclass cint = 0;
    jmethodID mput = 0;
    jmethodID mnew = 0;
    jint count = 0;

    cmap = (*env)->GetObjectClass(env, map);
    ASSERT(cmap);

    mput = (*env)->GetMethodID(env, cmap,
                               "put",
                               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    ASSERT(mput);
    ASSERT(mput);

    cint = (*env)->FindClass(env, "java/lang/Integer");
    ASSERT(cint);

    mnew = (*env)->GetMethodID(env, cint,
                               "<init>",
                               "(I)V");
    ASSERT(mnew);

    for(count = 0; ; ++count) {
        const char* ckey;
        int cval;
        jstring jkey =  0;
        jobject jval = 0;

        ckey = nn_symbol(count, &cval);
        if (ckey == 0)
            break;
        // fprintf(stderr, "Got symbol #%d: [%s] -> %d\n", count, ckey, cval);

        jkey = (*env)->NewStringUTF(env, ckey);
        ASSERT(jkey);
        // fprintf(stderr, "Created Java String for [%s]\n", ckey);

        jval = (*env)->NewObject(env, cint, mnew, cval);
        ASSERT(jval);
        // fprintf(stderr, "Created Java Integer for [%d]\n", cval);

        (*env)->CallObjectMethod(env, map, mput, jkey, jval);
        // fprintf(stderr, "Inserted symbol in map: [%s] -> %d\n", ckey, cval);
    }

    return count;
}



JNIEXPORT void JNICALL Java_org_nanomsg_Nanomsg_nn_1term(JNIEnv* env, jclass cls){
    nn_term();
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1errno(JNIEnv* env, jclass cls){
    return nn_errno();
}

JNIEXPORT jstring JNICALL Java_org_nanomsg_Nanomsg_nn_1strerror(JNIEnv* env, jclass cls, jint errn){
    const char* cerr = 0;
    jstring jerr = 0;

    cerr = nn_strerror(errn);
    if (cerr == 0)
        cerr = "";

    jerr = (*env)->NewStringUTF(env, cerr);
    ASSERT(jerr);
    return jerr;
 }

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1socket(JNIEnv* env, jclass cls, jint domain, jint protocol){
    return nn_socket(domain, protocol);
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1close(JNIEnv* env, jclass cls, jint socket){
    return nn_close(socket);
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1bind(JNIEnv* env, jclass cls, jint socket, jstring address){
    const char* cadd = 0;
    cadd = (*env)->GetStringUTFChars(env, address, NULL);
    ASSERT(cadd);
    return nn_bind(socket, cadd);
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1connect(JNIEnv* env, jclass cls, jint socket, jstring address){
    const char* cadd = 0;
    cadd = (*env)->GetStringUTFChars(env, address, NULL);
    ASSERT(cadd);
    return nn_connect(socket, cadd);
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1shutdown(JNIEnv* env, jclass cls, jint socket, jint how){
    return nn_shutdown(socket, how);
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1sendArray(JNIEnv* env, jclass cls, jint socket, jbyteArray array, jint offset, jint length, jint flags){
    jbyte* b = nn_allocmsg((size_t)length, 0);
    (*env)->GetByteArrayRegion(env, array, offset, length, b );
    jint ret = nn_send(socket, &b, NN_MSG, flags);
    return ret;
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1send(JNIEnv* env, jclass cls, jint socket, jobject buffer, jint offset, jint length, jint flags){
    jbyte* cbuf = 0;
    jint ret = 0;

    cbuf = (jbyte*) (*env)->GetDirectBufferAddress(env, buffer);
    ASSERT(cbuf);

    ret = nn_send(socket, cbuf + offset, (size_t)length, flags);
    return ret;
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1recvArray(JNIEnv* env, jclass cls, jint socket, jbyteArray array, jint offset, jint length, jint flags){
    jbyte* b = nn_allocmsg((size_t)length, 0);
    int ret = nn_recv(socket, b, (size_t)length, flags);
    (*env)->SetByteArrayRegion(env, array, offset, length, b);
    nn_freemsg(b);
    return ret;
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1recv(JNIEnv* env, jclass cls, jint socket, jobject buffer, jint offset, jint length, jint flags){
    jbyte* cbuf = 0;
    jint ret = 0;

    cbuf = (jbyte*) (*env)->GetDirectBufferAddress(env, buffer);
    ASSERT(cbuf);
    ret = nn_recv(socket, cbuf + offset, (size_t)length, flags);

    return ret;
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1getsockopt_1int(JNIEnv* env, jclass cls, jint socket, jint level, jint optidx, jobject optval){
    jint ret = -1;
    int go = 0;

    switch (level) {
        case NN_SOL_SOCKET:
            switch (optidx){
                case NN_DOMAIN:
                case NN_PROTOCOL:
                case NN_LINGER:
                case NN_SNDBUF:
                case NN_RCVBUF:
                case NN_SNDTIMEO:
                case NN_RCVTIMEO:
                case NN_RECONNECT_IVL:
                case NN_RECONNECT_IVL_MAX:
                case NN_SNDPRIO:
                case NN_SNDFD:
                case NN_RCVFD:
                    go = 1;
                    break;
            }
            break;

        case NN_TCP:
            switch (optidx) {
            case NN_TCP_NODELAY:
                go = 1;
                break;
            }
            break;
    }

    if (go) {
        int val = 0;
        size_t len = sizeof(val);

        ret = nn_getsockopt(socket, level, optidx, &val, &len);
        if (ret >= 0) {
            jclass cval = 0;
            jfieldID ival = 0;

            ret = (jint)len;

            cval = (*env)->GetObjectClass(env, optval);
            ASSERT(cval);

            ival = (*env)->GetFieldID(env, cval, "value", "I");
            ASSERT(ival);

            (*env)->SetIntField(env, optval, ival, val);
        }
    }

    return ret;
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1setsockopt_1int(JNIEnv* env, jclass cls, jint socket, jint level, jint optidx, jint optval){
    jint ret = -1;
    int go = 0;

    switch (level) {
        case NN_SOL_SOCKET:
            switch (optidx) {
            case NN_LINGER:
            case NN_SNDBUF:
            case NN_RCVBUF:
            case NN_SNDTIMEO:
            case NN_RCVTIMEO:
            case NN_RECONNECT_IVL:
            case NN_RECONNECT_IVL_MAX:
            case NN_SNDPRIO:
                go = 1;
                break;
            }
            break;

        case NN_TCP:
            switch (optidx) {
            case NN_TCP_NODELAY:
                go = 1;
                fprintf(stderr, "TCP_NODELAY\n");
                break;
            }
            break;
    }

    if (go) {
        int oval = optval;
        size_t olen = sizeof(oval);

        ret = nn_setsockopt(socket, level, optidx, &oval, olen);
        if (ret >= 0) {
            ret = (jint)olen;
        }
    }

    return ret;
}

JNIEXPORT jint JNICALL Java_org_nanomsg_Nanomsg_nn_1setsockopt_1str(JNIEnv* env, jclass cls, jint socket, jint level, jint optidx, jstring optval){

    const jsize len = (*env)->GetStringUTFLength(env, optval);
    const char *str = (*env)->GetStringUTFChars(env, optval, 0);

    switch(level){
        case NN_SUB:
            break;

        default:
            return -1;
    }

    int ret = nn_setsockopt(socket, level, optidx, str, (size_t)len);
    (*env)->ReleaseStringUTFChars(env, optval, str);
    return ret;
}

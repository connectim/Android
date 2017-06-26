// IMessage.aidl
package connect.im;

// Declare any non-default types here with import statements

interface IMessage {

    void connectMessage(int type,inout byte[] ack,inout byte[] message);
}

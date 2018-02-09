// IMessage.aidl
package connect.im;

// Declare any non-default types here with import statements

interface IMessage {

    void serviceBind();

    void connectStart();

    void connectMessage(inout byte[] ack,inout byte[] message);

    void heartBeat();

    void connectStop();

    void connectExit();
}

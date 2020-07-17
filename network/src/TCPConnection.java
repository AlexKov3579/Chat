import java.io.*;
import java.net.Socket;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
//    private final User user;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(new Socket(ipAddr, port), eventListener);
    }



    public TCPConnection(Socket socket, TCPConnectionListener eventListener) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;

        // TODO: 15.07.2020 fix issue with in and out
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        Object obj = in.readObject();
                        eventListener.onReceiveObject(TCPConnection.this, obj);
                    }

                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                    disconnect();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        rxThread.start();
    }


    //    public synchronized void sendObject(String value) {
//        try {
//            out.write(value + "\r\n");
//            out.flush();
//        } catch (IOException e) {
//            eventListener.onException(TCPConnection.this, e);
//            disconnect();
//        }
//    }
    public synchronized void sendObject(Object obj) {
        try {
            out.writeObject(obj);
//            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }

    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }

    public Socket getSocket() {
        return socket;
    }

}

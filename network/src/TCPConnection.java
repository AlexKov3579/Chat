import java.io.*;
import java.net.Socket;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private User user;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(new Socket(ipAddr, port), eventListener, null);
    }

    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port, String username) throws IOException {
        this(new Socket(ipAddr, port), eventListener, username);
    }


    public TCPConnection(Socket socket, TCPConnectionListener eventListener, String username) throws IOException {
        this.eventListener = eventListener;
        this.user = new User(username, socket.getInetAddress(), socket.getPort());
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
                    eventListener.onDisconnect(TCPConnection.this);
                    disconnect();
                    eventListener.onException(TCPConnection.this, e);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        rxThread.start();
    }


    public synchronized void sendObject(Object obj) {
        try {
            out.writeObject(obj);

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


    public User getUser() {
        return user;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

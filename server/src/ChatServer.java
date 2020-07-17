import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {
    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private final ArrayList<User> users = new ArrayList<>();

    private ChatServer() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                try {
                    new TCPConnection(serverSocket.accept(), this);
                } catch (IOException ex) {
                    System.out.println("TCPConnection exception : " + ex);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections(new Message("Client connected: " + tcpConnection));
        User user = new User(tcpConnection.getSocket().getInetAddress().toString(), tcpConnection.getSocket().getPort());
        users.add(user);
        sendToAllConnections(users.toArray());
    }

    @Override
    public synchronized void onReceiveObject(TCPConnection tcpConnection, Object obj) {
        if (obj.getClass() == Message.class) {
            Message msg = (Message) obj;
            if (msg.getDestination() == null) sendToAllConnections(msg);
            else sendToOneConnection(msg);
        } else if (obj.getClass() == SystemMessage.class) {
            SystemMessage msg = (SystemMessage) obj;
            if (msg.getFlag() == SystemMessage.USER_INFO) {
                User user = new User(tcpConnection.getSocket().getInetAddress().toString(), tcpConnection.getSocket().getPort());
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).equals(user)) {
                        users.get(i).setUsername(msg.getText());
                        tcpConnection.sendObject(users.get(i));
                    }
                }
            }

        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections(new Message("Client died: " + tcpConnection));
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    public void sendToAllConnections(Object obj) {
//        System.out.println(((Message)obj).getText());
        final int cnt = connections.size();
        for (int i = 0; i < cnt; i++) connections.get(i).sendObject(obj);
    }

    public void sendToOneConnection(Message msg) {
        for (int i = 0; i < connections.size(); i++) {
            if (users.get(i).equals(msg.getDestination())) {
                connections.get(i).sendObject(msg);
            }
        }
    }
}

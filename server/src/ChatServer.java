import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {
    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
//    private final ArrayList<User> users = new ArrayList<>();

    private ChatServer() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                try {
                    new TCPConnection(serverSocket.accept(), this, null);
                } catch (IOException ex) {
                    System.out.println("TCPConnection exception : " + ex);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
//        users.add(user);
        sendToAllConnections(new Message("Client connected: " + tcpConnection));

    }


    @Override
    public synchronized void onReceiveObject(TCPConnection tcpConnection, Object obj) {
        if (obj.getClass() == Message.class) {
            Message msg = (Message) obj;
            if (msg.getDestination() == null) sendToAllConnections(msg);
            else sendToOneConnection(msg);
        } else if (obj.getClass() == User.class) {
            User user = (User) obj;
            for (int i = 0; i < connections.size(); i++) {
                Socket socket = connections.get(i).getSocket();
                if (socket.getInetAddress().equals(user.getIP_ADDR()) && socket.getPort() == user.getPort()) {
                    connections.get(i).setUser(user);
                    break;
                }
            }
            sendToAllConnections(makeDefaultModelList());
        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections(makeDefaultModelList());
        sendToAllConnections(new Message("Client died: " + tcpConnection.getUser().getUsername()));

    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    public void sendToAllConnections(Object obj) {
        final int cnt = connections.size();
        for (int i = 0; i < cnt; i++) connections.get(i).sendObject(obj);
    }

    public void sendToOneConnection(Message msg) {
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).getUser().equals(msg.getDestination())) {
                connections.get(i).sendObject(msg);
            }
        }
    }

    private DefaultListModel makeDefaultModelList() {
        DefaultListModel<User> userModelList = new DefaultListModel<>();
        for (int i = 0; i < connections.size(); i++) {
            userModelList.addElement(connections.get(i).getUser());
        }
        return userModelList;
    }
}


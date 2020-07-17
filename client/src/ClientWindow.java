import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;


public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "127.0.0.1";
    private static final int PORT = 8189;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static User user;
    private static String username;
    private TCPConnection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldInput = new JTextField();
    private final JList<User> userList = new JList<>();


    // TODO: 14.07.2020 implement list and user choice 
    private ClientWindow() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        log.setEditable(false);
        log.setLineWrap(true);
        fieldInput.addActionListener(this);

        add(log, BorderLayout.CENTER);
        add(fieldInput, BorderLayout.SOUTH);
        add(userList, BorderLayout.EAST);
        userList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        userList.setSize(WIDTH / 3, HEIGHT);

        new Authorisation();


        setVisible(true);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
        connection.sendObject(new SystemMessage(username, SystemMessage.USER_INFO));
    }

    @Override
    public void onReceiveObject(TCPConnection tcpConnection, Object obj) {
        if (obj.getClass() == Message.class) {
            Message msg = (Message) obj;
            printMsg(msg.getText());
        } else if (obj.getClass() == User.class) {
            user = (User) obj;
        } else {
            User[] users = (User[]) obj;
            userList.setListData(users);
        }

    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection closed");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.equals("")) return;
        fieldInput.setText(null);
        if (userList.isSelectionEmpty()) {
            connection.sendObject(new Message(user, msg));
        } else {
            connection.sendObject(new Message(user, userList.getSelectedValue(), msg));
        }

    }

    private synchronized void printMsg(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    class Authorisation extends JFrame implements ActionListener {
        private static final int WIDTH = 320;
        private static final int HEIGHT = 480;
        private final JTextField login = new JTextField("Your name");

        Authorisation() {
            setSize(WIDTH, HEIGHT);
            setLocationRelativeTo(null);
            setAlwaysOnTop(true);

            login.addActionListener(this);

            add(login, BorderLayout.SOUTH);

            setVisible(true);


        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            user = new User(login.getText(),);
            username = login.getText();
            try {
                connection = new TCPConnection(ClientWindow.this, IP_ADDR, PORT);
//            connection = new TCPConnection(ClientWindow.this, IP_ADDR, PORT);
            } catch (IOException ex) {
                printMsg("Connection exception: " + e);
            }
            this.setVisible(false);
            this.dispose();

        }
    }

}


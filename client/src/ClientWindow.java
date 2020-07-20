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
    DefaultListModel<User> users = new DefaultListModel<>();
    private JList<User> userList = new JList<>();
    private final JPanel pane = new JPanel();


    // TODO: 14.07.2020 implement list and user choice
    // TODO: 20.07.2020 fix username in list 
    private ClientWindow() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        log.setEditable(false);
        log.setLineWrap(true);

        fieldInput.addActionListener(this);

        add(pane, BorderLayout.CENTER);
        pane.setLayout(new GridLayout(1, 2));
        pane.add(log);

        pane.add(userList);

        add(fieldInput, BorderLayout.SOUTH);

        userList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        log.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        userList.setCellRenderer(new UserCellRenderer());
        userList.setVisible(true);

        username = JOptionPane.showInputDialog(null, "Your name");
        try {
            connection = new TCPConnection(ClientWindow.this, IP_ADDR, PORT, username);
            user = connection.getUser();
        } catch (IOException ex) {
            printMsg("Connection exception: " + ex);
        }


        setVisible(true);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
//        connection.sendObject(new SystemMessage(username, SystemMessage.USER_INFO));
        connection.sendObject(user);
    }

    @Override
    public void onReceiveObject(TCPConnection tcpConnection, Object obj) {
        if (obj.getClass() == Message.class) {
            Message msg = (Message) obj;
            if (msg.getSender() != null) printMsg(msg.getSender().getUsername() + " : " + msg.getText());
            else printMsg(msg.getText());
        } else if (obj.getClass() == User.class) {
            user = (User) obj;
            user.setUsername(username);
        } else {
            System.out.println("Why are we still here");
//            ArrayList<TCPConnection> userArrayList = (ArrayList)obj;
            users = (DefaultListModel) obj;

            userList.setModel(users);
            userList.updateUI();
            userList.ensureIndexIsVisible(users.getSize());
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
            printMsg(user.getUsername() + " : " + msg);
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


}

class UserCellRenderer extends JLabel implements ListCellRenderer {
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        User user = (User) value;
        setText(user.getUsername() + " : " + user.getIP_ADDR() + " : " + user.getPort());
        if (isSelected) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}
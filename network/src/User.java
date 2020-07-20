import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class User implements Serializable {
    private String username;
    private InetAddress IP_ADDR;
    private int port;

    User(String IP_ADDR, int port) throws UnknownHostException {
        this.IP_ADDR = InetAddress.getByName(IP_ADDR);
        this.port = port;
    }

    User(String username, String IP_ADDR, int port) throws UnknownHostException {
        this.username = username;
        this.IP_ADDR = InetAddress.getByName(IP_ADDR);
        this.port = port;
    }

    User(String username, InetAddress IP_ADDR, int port) throws UnknownHostException {
        this.username = username;
        this.IP_ADDR = IP_ADDR;
        this.port = port;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getIP_ADDR() {
        return IP_ADDR;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return port == user.port &&
                Objects.equals(IP_ADDR, user.IP_ADDR);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IP_ADDR, port);
    }

//    @Override
//    public String toString() {
//        return username+" : "+IP_ADDR+" : "+ port;
//    }
}

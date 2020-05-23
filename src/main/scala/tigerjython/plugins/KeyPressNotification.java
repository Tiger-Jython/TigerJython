package tigerjython.plugins;

/**
 * We define an interface for key-pressed events for better interaction with Jython.
 *
 * @author Tobias Kohn
 */
public interface KeyPressNotification {

    void apply(long time, int pos, String key);
}

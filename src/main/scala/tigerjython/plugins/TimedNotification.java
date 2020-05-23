package tigerjython.plugins;

/**
 * We define an interface for better interaction with Jython.
 *
 * @author Tobias Kohn
 */
public interface TimedNotification {

    void apply(long time);
}

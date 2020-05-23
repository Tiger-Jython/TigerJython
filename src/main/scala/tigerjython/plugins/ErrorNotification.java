package tigerjython.plugins;

/**
 * We define an interface for a `(Int, Int, String)=>Unit` function for easy interaction with Jython.
 *
 * @author Tobias Kohn
 */
public interface ErrorNotification {

    void apply(long time, int line, int column, String message);
}

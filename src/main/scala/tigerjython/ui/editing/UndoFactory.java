package tigerjython.ui.editing;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.TextChange;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.impl.ChangeQueue;
import org.fxmisc.undo.impl.UndoManagerImpl;

import java.time.Duration;
import java.util.function.Consumer;

/*
 * This is a simplified version of RichText's `UndoFactory`, with code taken from `org.fxmisc.undo.UndoManagerFactory`
 * and `org.fxmisc.richtext.util.UndoUtils`.
 *
 * We use this factory to inject our own change-queue into an otherwise standard undo manager.
 *
 * @author: Tobias Kohn
 */
public class UndoFactory {

    // This is slightly higher than the default of 500ms in the original file to compensate for the probably slower
    // typing speed of students when entering program code
    public static final Duration DEFAULT_PREVENT_MERGE_DELAY = Duration.ofMillis(1000);

    public static <PS, SEG, S> UndoManager<PlainTextChange> createUndoManager(GenericStyledArea<PS, SEG, S> area) {
        ChangeQueue<PlainTextChange> queue = new TigerJythonChangeQueue<>();
        return createUndoManager(area, queue);
    }

    public static <PS, SEG, S> UndoManager<PlainTextChange> createUndoManager(
            GenericStyledArea<PS, SEG, S> area,
            ChangeQueue<PlainTextChange> queue
    ) {
        return new UndoManagerImpl<>(
                queue,
                TextChange::invert,
                applyPlainTextChange(area),
                TextChange::mergeWith,
                TextChange::isIdentity,
                area.plainTextChanges(),
                DEFAULT_PREVENT_MERGE_DELAY
        );
    }

    public static <PS, SEG, S> Consumer<PlainTextChange> applyPlainTextChange(GenericStyledArea<PS, SEG, S> area) {
        return change -> {
            area.replaceText(change.getPosition(), change.getRemovalEnd(), change.getInserted());
            moveToChange( area, change );
        };
    }

    private static <PS, SEG, S> void moveToChange( GenericStyledArea<PS, SEG, S> area, PlainTextChange chg )
    {
        int pos = chg.getPosition();
        int len = chg.getNetLength();
        if ( len > 0 ) pos += len;

        area.moveTo( Math.min( pos, area.getLength() ) );
    }
}

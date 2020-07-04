package tigerjython.ui.editing;

import java.util.ArrayList;
import org.fxmisc.undo.impl.ChangeQueue;

/*
 * Almost the entire class is an exact copy of `org.fxmisc.undo.impl.UnlimitedChangeQueue`.  However, we add the
 * possibility to access items in the history and thus enable us to save the entire undo history, and restore it
 * later on.
 *
 * Changes by Tobias Kohn, June 2020.
 */
public class TigerJythonChangeQueue<C> implements ChangeQueue<C> {

    private class QueuePositionImpl implements QueuePosition {
        private final int allTimePos;
        private final long rev;

        QueuePositionImpl(int allTimePos, long rev) {
            this.allTimePos = allTimePos;
            this.rev = rev;
        }

        @Override
        public boolean isValid() {
            int pos = allTimePos - forgottenCount;
            if(0 <= pos && pos <= changes.size()) {
                return rev == revisionForPosition(pos);
            } else {
                return false;
            }
        }

        @Override
        public boolean equals(Object other) {
            if(other instanceof TigerJythonChangeQueue.QueuePositionImpl) {
                @SuppressWarnings("unchecked")
                QueuePositionImpl otherPos = (QueuePositionImpl) other;
                return getQueue() == otherPos.getQueue() && rev == otherPos.rev;
            } else {
                return false;
            }
        }

        private TigerJythonChangeQueue<C> getQueue() {
            return TigerJythonChangeQueue.this;
        }
    }

    private final ArrayList<RevisionedChange<C>> changes = new ArrayList<>();
    private int currentPosition = 0;

    private long revision = 0;
    private long zeroPositionRevision = revision;
    private int forgottenCount = 0;

    private int basePosition = 0;

    @Override
    public final boolean hasNext() {
        return currentPosition < changes.size();
    }

    @Override
    public final boolean hasPrev() {
        return currentPosition > 0;
    }

    @Override
    public final C peekNext() {
        return changes.get(currentPosition).getChange();
    }

    @Override
    public final C next() {
        return changes.get(currentPosition++).getChange();
    }

    @Override
    public final C peekPrev() {
        return changes.get(currentPosition - 1).getChange();
    }

    @Override
    public final C prev() {
        --currentPosition;
        if (currentPosition < basePosition)
            basePosition = currentPosition;
        return changes.get(currentPosition).getChange();
    }

    @Override
    public void forgetHistory() {
        if(currentPosition > 0) {
            zeroPositionRevision = revisionForPosition(currentPosition);
            int newSize = changes.size() - currentPosition;
            for(int i = 0; i < newSize; ++i) {
                changes.set(i, changes.get(currentPosition + i));
            }
            changes.subList(newSize, changes.size()).clear();
            forgottenCount += currentPosition;
            currentPosition = 0;
            basePosition = 0;
        }
    }

    public C getHistoryItem(int index) {
        int i = currentPosition - 1 - index;
        return (i >= 0) ? this.changes.get(i).getChange() : null;
    }

    public int getHistoryLength() {
        return currentPosition;
    }

    public void markPositionAsBase() {
        basePosition = currentPosition;
    }

    public int getRecentHistoryCount() {
        return currentPosition - basePosition;
    }

    @Override
    @SafeVarargs
    public final void push(C... changes) {
        this.changes.subList(currentPosition, this.changes.size()).clear();
        for(C c: changes) {
            RevisionedChange<C> revC = new RevisionedChange<>(c, ++revision);
            this.changes.add(revC);
        }
        currentPosition += changes.length;
    }

    @Override
    public QueuePosition getCurrentPosition() {
        return new QueuePositionImpl(forgottenCount + currentPosition, revisionForPosition(currentPosition));
    }

    private long revisionForPosition(int position) {
        return position == 0
                ? zeroPositionRevision
                : changes.get(position - 1).getRevision();
    }
}

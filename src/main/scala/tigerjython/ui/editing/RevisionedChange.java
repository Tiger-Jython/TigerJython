package tigerjython.ui.editing;

/*
 * This is an exact copy of `org.fxmisc.undo.impl.RevisionedChange`, needed here because the original is not public and
 * thus inaccessible from here.
 */
class RevisionedChange<C> {
    private final C change;
    private final long revision;

    public RevisionedChange(C change, long revision) {
        this.change = change;
        this.revision = revision;
    }

    public C getChange() {
        return change;
    }

    public long getRevision() {
        return revision;
    }
}

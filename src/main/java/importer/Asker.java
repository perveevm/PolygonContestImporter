package importer;

public abstract class Asker {
    protected boolean askForAll;
    protected boolean updateAll;

    public Asker(boolean askForAll, boolean updateAll) {
        this.askForAll = askForAll;
        this.updateAll = updateAll;
    }

    abstract int ask(String message);
    abstract Asker copyAsker();

    public boolean askForUpdate(String message) {
        if (updateAll) {
            return true;
        }
        int got = ask(message);
        if (askForAll && got == 2) {
            updateAll = true;
            return true;
        }
        return got >= 1;
    }

    public void setAskForAll(boolean askForAll) {
        this.askForAll = askForAll;
    }

    public void setUpdateAll(boolean updateAll) {
        this.updateAll = updateAll;
    }
}

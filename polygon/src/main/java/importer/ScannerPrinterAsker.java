package importer;

import java.io.PrintStream;
import java.util.Scanner;

public class ScannerPrinterAsker extends Asker {
    private final Scanner in;
    private final PrintStream out;

    public ScannerPrinterAsker(Scanner in, PrintStream out, boolean askForAll, boolean updateAll) {
        super(askForAll, updateAll);
        this.in = in;
        this.out = out;
    }

    public ScannerPrinterAsker(Scanner in, PrintStream out) {
        super(false, false);
        this.in = in;
        this.out = out;
    }

    @Override
    int ask(String message) {
        out.println(message);
        while (true) {
            if (askForAll) {
                out.println("(y - yes, yy - yes to all, n - no)");
            } else {
                out.println("(y - yes, n - no)");
            }
            String line = in.nextLine();
            if (in.ioException() != null) {
                System.err.println("Exception happened, while reading from console " + in.ioException().getMessage());
                throw new AssertionError(in.ioException());
            }
            switch (line) {
                case "yy":
                    return 2;
                case "y":
                    return 1;
                case "n":
                    return 0;
            }
        }
    }

    @Override
    Asker copyAsker() {
        return new ScannerPrinterAsker(in, out, askForAll, updateAll);
    }
}

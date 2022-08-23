package importer;

import java.io.PrintWriter;
import java.util.Scanner;

public class ScannerPrinterAsker extends Asker {
    private final Scanner in;
    private final PrintWriter out;

    public ScannerPrinterAsker(Scanner in, PrintWriter out, boolean askForAll, boolean updateAll) {
        super(askForAll, updateAll);
        this.in = in;
        this.out = out;
    }

    public ScannerPrinterAsker(Scanner in, PrintWriter out) {
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
                throw new AssertionError("Exception happened, while reading from console", in.ioException());
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

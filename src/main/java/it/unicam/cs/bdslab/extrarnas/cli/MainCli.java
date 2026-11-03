package it.unicam.cs.bdslab.extrarnas.cli;

import it.unicam.cs.bdslab.extrarnas.view.HomeController;
import it.unicam.cs.bdslab.extrarnas.view.utils.TOOL;
import org.apache.commons.cli.*;
import org.bouncycastle.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This class is the main entry point for the CLI application.
 */
public class MainCli {
    private static final String USAGE = "java -jar Extrarnas-cli.jar <inputFolder> <output> [Options]";

    public static final Logger logger = Logger.getLogger(MainCli.class.getName()    );
    public static void main(String[] args) {
        var options = new Options();
        options
                .addOption("i", true, "input csv file with molecules to process")
                .addOption("o", true, "output directory")
                .addOption("t", true , String.format("tools [%s]", String.join(",", TOOL.getAvailableTools())))
                .addOption("h", false, "print help");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(USAGE, options);
                System.exit(0);
            }

            String sharedFolder = cmd.getOptionValue("o");
            String inputCsv = cmd.getOptionValue("i");
            String tool = cmd.getOptionValue("t");
            List<TOOL> tools = Arrays.stream(tool
                    .split(","))
                    .map(TOOL::fromString)
                    .toList();

            if (tools.stream().anyMatch(Objects::isNull)) {
                int index = tools.indexOf(null);
                System.err.println("Invalid tool at index: " + index + " tool: \"" + tool.split(",")[index] + "\"");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(USAGE, options);
                System.exit(1);
            }

            if (tools.isEmpty()) {
                System.err.println("No valid tools specified.");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(USAGE, options);
                System.exit(1);
            }

            for (TOOL t : tools) {
                System.out.println(t.toString());
            }

        } catch (Exception e) {
            System.err.println("Error parsing command line options: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, options);
            System.exit(1);
        }



    }

}

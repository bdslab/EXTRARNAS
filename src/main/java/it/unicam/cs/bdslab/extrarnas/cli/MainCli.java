package it.unicam.cs.bdslab.extrarnas.cli;

import it.unicam.cs.bdslab.extrarnas.view.utils.TOOL;
import org.apache.commons.cli.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This class is the main entry point for the CLI application.
 */
public class MainCli {
    private static final String USAGE = "java -jar EXTRARNAS-cli.jar -i <input.csv> -o <output-directory> -t <tools> [Options]";

    public static final Logger logger = Logger.getLogger(MainCli.class.getName()    );
    public static void main(String[] args) {
        var options = new Options();
        options
                .addOption(Option.builder("i").hasArg().desc("input csv file with molecules to process").build())
                .addOption(Option.builder("o").hasArg().desc("output directory").build())
                .addOption(Option.builder("t").hasArg()
                        .desc(String.format("comma-separated tools [%s]", String.join(",", TOOL.getAvailableTools())))
                        .build())
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

            if (sharedFolder == null || inputCsv == null || tool == null) {
                System.err.println("Missing required options: -i <input.csv>, -o <output-directory>, -t <tools>");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(USAGE, options);
                System.exit(1);
            }

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

            Path sharedDirectoryPath = Path.of(sharedFolder);
            if (!sharedDirectoryPath.toFile().exists() || !sharedDirectoryPath.toFile().isDirectory()) {
                logger.severe("the shared folder does not exist or is not a directory: " + sharedFolder);
                System.exit(1);
            }

            Path csvPath = Path.of(inputCsv);
            if (!csvPath.toFile().exists() || !csvPath.toFile().isFile()) {
                logger.severe("the input CSV file does not exist: " + inputCsv);
                System.exit(1);
            }

            var app = new ExtrarnasApplication(
                    sharedDirectoryPath,
                    csvPath,
                    tools
            );

            app.run();

        } catch (Exception e) {
            System.err.println("Error parsing command line options: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, options);
            System.exit(1);
        }



    }

}

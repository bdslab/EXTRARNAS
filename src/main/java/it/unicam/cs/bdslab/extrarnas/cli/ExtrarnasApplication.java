package it.unicam.cs.bdslab.extrarnas.cli;

import it.unicam.cs.bdslab.extrarnas.controller.CsvPreprocessingListener;
import it.unicam.cs.bdslab.extrarnas.controller.DockerController;
import it.unicam.cs.bdslab.extrarnas.controller.ExtendedBPSEQExportController;
import it.unicam.cs.bdslab.extrarnas.models.CsvRowResult;
import it.unicam.cs.bdslab.extrarnas.models.StructureInfo;
import it.unicam.cs.bdslab.extrarnas.parser.output.RNASecondaryStructurePrinter;
import it.unicam.cs.bdslab.extrarnas.view.HomeController;
import it.unicam.cs.bdslab.extrarnas.view.utils.TOOL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static it.unicam.cs.bdslab.extrarnas.view.utils.TOOL.*;

public class ExtrarnasApplication {
    private final Logger logger = Logger.getLogger(ExtrarnasApplication.class.getName());
    private final Path sharedFolder;
    private final Path csv;
    private final List<TOOL> tools;

    public ExtrarnasApplication(Path sharedFolder, Path csv, List<TOOL> tools) {
        this.sharedFolder = sharedFolder;
        this.csv = csv;
        this.tools = tools;
    }

    /**
     * Run the application.
     * Load molecules from the csv file and process them in the shared directory using the DockerController.
     * The output in a specified format (by now BPSEQ and BPSEQ) can be found in the /output folder.
     */
    public void run() {
        try {
            this.logger.info("Starting application...");
            DockerController.getInstance().buildDockerContainerBy(
                    new File(HomeController.dockerfileAllToolsPath),
                    HomeController.dockerAllToolsImage,
                    HomeController.dockerAllToolsImageTag,
                    HomeController.dockerAllToolsContainer,
                    this.sharedFolder
            );

            this.importMolecules();

            for (TOOL tool : tools) {
                var action = actionsMap.get(tool);
                if (action == null)
                    throw new RuntimeException("No action mapped for the tool " + tool);
                logger.info("Executing " + tool);
                action.run();
                logger.info("End Execution " + tool);
                ExtendedBPSEQExportController.getInstance().exportForTool(
                        tool,
                        this.sharedFolder,
                        RNASecondaryStructurePrinter.OutputFormat.BPSEQ,
                        RNASecondaryStructurePrinter.OutputFormat.EXTENDED_BPSEQ
                );
            }
            DockerController.getInstance().stopContainerByNameOrId(HomeController.dockerAllToolsContainer, 10);
            if (DockerController.getInstance().isContainerRunning(HomeController.dockerX3DNAContainer)) {
                DockerController.getInstance().stopContainerByNameOrId(HomeController.dockerX3DNAContainer, 10);
            }

        } catch (IOException | InterruptedException e) {
            this.logger.severe("Error occurred while running the application: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void importMolecules() {
        CsvPreprocessingListener listener = new CsvPreprocessingListener() {
            @Override
            public void onRowStarted(String pdbId, String chainFilter, int rowNumber, int totalRows) {
                logger.info("Processing row " + rowNumber + " of " + totalRows);
            }

            @Override
            public void onRowCompleted(CsvRowResult result, int completedRows, int totalRows, List<StructureInfo> workspaceStructures) {
                logger.info("Completed row " + completedRows + " of " + totalRows);
            }
        };

        try {
            DockerController.getInstance().preprocessCsvAndCollectStructures(
                    this.sharedFolder,
                    this.csv,
                    listener
            );
        } catch (IOException e) {
            logger.severe("Error occurred while preprocessing CSV: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private final Map<TOOL, Runnable> actionsMap = Map.of(
        RNAPOLIS_ANNOTATOR, () -> {
            try {
                DockerController.getInstance().rnapolisAnnotator();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },
        RNAVIEW, () -> {
            try {
                DockerController.getInstance().rnaView();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },
        BARNABA, () -> {
            try {
                DockerController.getInstance().baRNAba();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },
        BPNET, () -> {
            try {
                DockerController.getInstance().bpnet();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },
        FR3D, () -> {
            try {
                DockerController.getInstance().fr3d();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },
        X3DNA, () -> {
            try {
                DockerController.getInstance().x3dnaBy(HomeController.dockerX3DNAContainer);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        },
        MC_ANNOTATE, () -> {
            try {
                DockerController.getInstance().mcAnnotate();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
}

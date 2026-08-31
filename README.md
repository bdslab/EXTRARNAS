# EXTRARNAS (Tool for the Analysis of RNA Structures)

EXTRARNAS is a Java application designed to analyze RNA 3D structures and extract their secondary structures and base-pairing interactions. It provides a guided interface to configure and run specialized bioinformatics tools inside isolated environments, generating standardized outputs.

## Related Publication

The current version of EXTRARNAS is described in the following preprint, which corresponds to the short paper accepted for presentation at the 21st International Conference on Computational Intelligence Methods for Bioinformatics and Biostatistics (CIBB 2026):


Di Petta, F., Rosati, P., Hierro Canchari, P., Quadrini, M., and Tesei, L.

**EXTRARNAS: A Framework for Extracting RNA Structures with Multiple Tools.**

bioRxiv, 2026.

https://doi.org/10.64898/2026.08.27.747497

Conference website: https://cibb2026.teralab.ai/

## Citation

If you use EXTRARNAS in your research, please cite:

- the publication above;
- the corresponding software release available on Zenodo:  [https://doi.org/10.5281/zenodo.21238912](https://doi.org/10.5281/zenodo.21238912).

## Prerequisites

- **Java 21 (or later) Runtime Environment (JRE)** installed on your system. See, e.g., [Java Downloads](https://www.oracle.com/it/java/technologies/downloads/)
- **Docker Desktop** installed and running on your system. See [Docker.com](https://www.docker.com/products/docker-desktop/)
- **JavaFX 21** (optional). See [JavaFX Download](https://www.oracle.com/java/technologies/downloads/javafx/)

## Features & Workflow

When running the application, you will be guided through a configuration process:

1. **Shared Volume / Workspace Setup:** The software will ask you to specify a local directory to be shared with the Docker container. This is necessary to pass input data to the extraction tools and to securely retrieve their outputs.

2. **Add CSV molecules list:** Use this button to select a CSV file from any accessible folder and add its molecules to the persistent shared workspace. Structures already present in the workspace's `preprocessed` folder are restored when EXTRARNAS starts. The file must contain two columns: `id` and `chain`, where `id` is the PDB code (4 letters or digits) and `chain` is the author/PDB chain identifier (`_atom_site.auth_asym_id`). Do not use the mmCIF `_atom_site.label_asym_id`. Chain IDs are case-sensitive; use semicolons to request multiple chains or `*` to process all RNA chains in the structure. Example:

| id   | chain |
|------|-------|
| 4PLX | A     |
| 6QNR | A     |
| 4V5K | BB;DB |
| 2KOC | *     |

*Note: `*` means that all chains in the structure will be processed.*

The CSV file itself does not need to be inside the shared workspace because it is read by the EXTRARNAS host application. Any local PDB files referenced by their IDs must still be placed in the shared workspace so that Docker-based tools can access them. Deleting a structure from the table also removes its generated PDB and mmCIF files from `preprocessed`.

3. **Tool Selection:** Choose which extraction tool you want to run.

4. **Structure Analysis Level:**
   * **Secondary Structure:** Analyzes and extracts canonical cis Watson–Crick base pairs.
   * **Extended Secondary Structure:** Extracts all Leontis–Westhof base-pair types.

5. **Output Formats:**
   * **BPSEQ:** Standard output reporting only canonical base pairs.
   * **Extended BPSEQ:** An extended BPSEQ format in which the third column corresponds to canonical cis Watson–Crick pairs (as in BPSEQ), while the remaining columns encode the other base-pair types according to the Leontis–Westhof nomenclature.

   The extended format includes the following columns:

   | id | nt | cWW | tWW | cWH | tWH | cWS | tWS | cHH | tHH | cHS | tHS | cSS | tSS |
   |-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|

### Canonical vs Non-Canonical Base Pairs
In EXTRARNAS, **Canonical base pairs** correspond strictly to standard cis Watson-Crick pairs (A-U, G-C, G-U).

## The Role of Docker

EXTRARNAS leverages **Docker containers** to run its underlying analysis tools.
The standard analysis environment is provided by the `extrarnas-core` image and is tagged with the EXTRARNAS release, for example `extrarnas-core:1.0.2-cibb2026`. Optional tools with separate licensing requirements, such as X3DNA-DSSR, use dedicated images.
Using containers guarantees that:
- You do not need to manually install complex third-party tools, compilers, or specific language versions (such as old Python versions) on your machine.
- Executions are perfectly reproducible and run in an isolated environment.
- *Note:* The `docker/` folder distributed alongside the application contains the Dockerfiles required by the software to build and run the images correctly.

## Build from Source

The build process automatically packages all required dependencies into a self-contained ("fat") executable JAR.
To compile the application and generate the executable package, run:

```bash
mvn clean package
```
This will place the executable `EXTRARNAS-fat.jar` and its required `docker/` folder in the `target/` directory.


## Usage

To start EXTRARNAS, simply run the jar file via terminal. 
Make sure you are in the same folder where the `.jar` and the `docker/` 
directory reside. Make sure to also have installed and running Docker. 

We provide three pre-built bundles for Windows, macOS, and Linux.
Choose the bundle corresponding to your operating system.

If your OS is not listed try to build the
application from source. If the application is not starting up or if you get 
an error when loading the CSV file see the next section (Troubleshooting).

Run the following command on your specific jar:

```bash
java -jar EXTRARNAS-<specific-bundle>.jar
```
e.g.

```bash
java -jar EXTRARNAS-windows.jar
```


## Troubleshooting

### JavaFX Startup problem

If the application does not start because of a JavaFX error, Download JavaFX 21 and launch the application as follows:

```bash
java --module-path "path\to\javafx\lib" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.graphics,javafx.media -jar EXTRARNAS-<specific-bundle>.jar
```

### Molecules loading error

If you receive an error when loading the CSV, try to add this option:
```bash
java -Dcom.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize=true -jar EXTRARNAS-<specific-bundle>.jar
```

*(You don't need to specify extra complex classpath flags since it's a "fat" jar containing all its dependencies).*

### Current limitations

mmCIF→PDB conversion for bundled PDB entries may fail in a small number of cases because of placeholder crystallographic records produced by BeEM.

These issues are limited to a few legacy structures and do not affect the majority of RNA entries available from the Protein Data Bank.

## License

Please refer to the `LICENSE` file in the repository for more details.

## Availability

- GitHub repository:
  https://github.com/bdslab/EXTRARNAS

- Software release (Zenodo):
  https://doi.org/10.5281/zenodo.21238912

- Preprint:
  https://doi.org/10.64898/2026.08.27.747497
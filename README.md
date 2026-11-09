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
- Docker provides isolated and consistent execution environments.
  However, rebuilding an image at different times may install different
  versions of third-party annotation tools, as their dependencies are
  not currently pinned.
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

### Installing X3DNA-DSSR

EXTRARNAS uses **X3DNA-DSSR** as an external tool. Since X3DNA-DSSR requires a license, you must first request access to the software and follow the official X3DNA licensing procedure.

Once you receive the software archive, follow these steps:

1. Make sure you downloaded the **Linux distribution**, as X3DNA-DSSR is executed inside the Docker environment.
2. Place the downloaded ZIP archive inside the following directory:

   `docker/x3dna-tool`

3. Rename the archive exactly as follows:

   `dssr-basic-linux-v2.8.1.zip`

The archive name and location are required by the Docker configuration, so make sure they match exactly.

After completing the installation procedure and starting EXTRARNAS through Docker, the required Docker image should be built successfully and the X3DNA-DSSR tool should become available in the application's tool list.

If the tool does not appear in the list or does not work correctly when executed, check the application and Docker logs for error messages. In most cases, the issue is caused by an incorrect archive name, an incorrect file location, or by using a distribution that is not compatible with the Linux Docker environment. Verify these steps and repeat the installation procedure if necessary.

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

## Current limitations

mmCIF→PDB conversion for bundled PDB entries may fail in a small number of cases because of placeholder crystallographic records produced by BeEM.

These issues are limited to a few legacy structures and do not affect the majority of RNA entries available from the Protein Data Bank.

### Non-standard nucleotides in local PDB files

When processing user-provided structures in the legacy PDB format,
EXTRARNAS cannot guarantee the correct recognition of all non-standard
or chemically modified nucleotides.

Nucleotide identification relies on BioJava's chemical component
classification. Some components that are part of an RNA polymer may be
classified as non-polymeric and therefore not recognized as
nucleotides. For example, guanosine triphosphate (GTP) may be
classified as NON-POLYMER, even when it represents the first residue
of an RNA chain.

Unlike mmCIF files, legacy PDB files may not provide sufficient
information to reliably establish whether a non-standard component
belongs to the RNA polymer, particularly when they are manually
constructed and lack records such as SEQRES or MODRES.

As a result, some non-standard nucleotides may be excluded from the
reconstructed RNA sequence and subsequent structural annotations,
potentially affecting sequence length, residue numbering, and
extracted interactions.

For reliable processing of RNA structures containing non-standard
nucleotides, the mmCIF format is recommended. When using manually
constructed PDB files, users should verify that all expected
nucleotides are correctly recognized and included in the output.

Sequence reconstruction and residue numbering

### Sequence reconstruction and residue numbering in local PDB files

User-provided legacy PDB files may not contain sufficient information
to reconstruct the complete RNA sequence, particularly when they lack
SEQRES records.

Gaps in residue numbering do not necessarily correspond to unmodelled
nucleotides. They may result from numbering conventions, residues
intentionally removed during model construction, or modifications to
an existing structure.

When a reliable reference sequence is unavailable, EXTRARNAS
reconstructs the sequence from the nucleotide residues present in the
coordinate records, without automatically introducing unmodelled
positions for numbering gaps.

Users should verify that the reconstructed sequence and residue
mapping are consistent with the intended RNA model. Whenever possible,
the mmCIF format is recommended.

### X3DNA-DSSR Licensing

X3DNA-DSSR licensing: X3DNA-DSSR is not bundled with EXTRARNAS or its
distributed Docker images. Users must obtain the executable
independently under the appropriate academic or commercial license.
Licensing information and downloads are available from the official
DSSR licensing portal.

## License

Please refer to the `LICENSE` file in the repository for more details.

## Availability

- GitHub repository:
  https://github.com/bdslab/EXTRARNAS

- Software release (Zenodo):
  https://doi.org/10.5281/zenodo.21238912

- Preprint:
  https://doi.org/10.64898/2026.08.27.747497
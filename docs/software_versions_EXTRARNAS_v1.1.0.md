# EXTRARNAS - Software environment manifest v1.1.0

## Container images

| Image tag | Image ID (sha256) | Role |
| --- | --- | --- |
| `extrarnas-core:2.0.1` | `27a30b6ced8b4c96fff3ed821d73777993e02454f2fdc045dab39c116751d3dd` | Six redistributable annotators and preprocessing |
| `x3dna-image:latest` | `dec1bd37e571a8493d3df59dfda498fd5e6b68f79d79d0471bf149615a08cc5f` | Separately licensed X3DNA-DSSR |

These are **local image IDs**, not registry-published digests. A rebuild from the current Dockerfiles is not guaranteed to reproduce these images because dependencies and Git checkouts are not pinned.

## Annotators

| Annotation tool | Installed version / source identifier | Verification method |
| --- | --- | --- |
| RNApolis Annotator | `RNApolis==0.11.5` | `python -m pip freeze` in `extrarnas-core:2.0.1` |
| FR3D (`fr3d-python`) | Git commit `2ef4fb1c31576700649d0e99b33d258ef92732fe` | `git -C /home/fr3d-python rev-parse HEAD`; pip metadata `fr3d @ file:///home/fr3d-python` |
| BPNet | Git commit `6fd9ebcf8765b004913629ff27aa90143affc428` | `git -C /home/bpnet rev-parse HEAD` |
| Barnaba | Git commit `48ac4a9496c68fc48698f65c5e0d9563761681ec` | `git -C /home/barnaba rev-parse HEAD`; also in `pip freeze` editable URL |
| RNAView | Git commit `45a2d7fcb301cdcbd12da2f9d3229ae1bb8aea31` | `git -C /home/RNAView rev-parse HEAD` |
| MC-Annotate | Binary SHA-256 `285b89a65916133ae49c56db440dfbe38227fc648805cc759d8e37be418b0139` | `sha256sum /home/MC-Annotate/MC-Annotate`; version number not determined |
| X3DNA-DSSR | `v2.8.1-2026mar12`; binary SHA-256 `fc4d0e973fe2e82f3d70778a662a75fedc687dffa770d36d3e6e8cdcab165097` | `x3dna-dssr --version`; `sha256sum /home/x3dna-dssr` in `x3dna-image:latest` |

## Other processing components

| Component | Version / identifier | Verification method |
| --- | --- | --- |
| BeEM | Git commit `4d71e6bf120312669859fc08b8e0c97918913779` | `git -C /home/BeEM rev-parse HEAD` |
| mmcif-pdbx | `2.1.0` | `pip freeze` |
| BioPython | `1.88` | `pip freeze` |
| ViennaRNA | `2.7.2` | `pip freeze` |
| Python base image | `python:3.10-slim` | Dockerfile on `dev`; precise interpreter patch level not queried |
| Java runtime (host) | Java 21 reported for the experiments in manuscript | Not verified by the supplied Docker commands; record `java -version` separately |
| EXTRARNAS application | TBD: match Java app release tag and Git commit to experiment | Not determined from Docker outputs |

## Python environment (`extrarnas-core:2.0.1`)

This is the entire supplied output of `python -m pip freeze`:

```text
appdirs==1.4.4
-e git+https://github.com/srnas/barnaba.git@48ac4a9496c68fc48698f65c5e0d9563761681ec#egg=barnaba
biopython==1.88
certifi==2026.10.23
cftime==1.6.5
charset-normalizer==3.5.2
cloudpickle==3.1.2
faiss-cpu==1.15.1
fr3d @ file:///home/fr3d-python
future==1.0.0
graphviz==0.21
idna==3.20
joblib==1.6.0
mdtraj==1.10.3
mmcif==1.2.1
mmcif-pdbx==2.1.0
msgpack==1.2.4
netCDF4==1.7.4
numpy==2.2.6
ordered-set==4.1.0
orjson==3.13.0
packaging==26.3
pandas==2.3.3
PuLP==3.3.2
pyparsing==3.3.3
python-dateutil==3.0.0
pytz==2026.5
requests==2.34.2
RNApolis==0.11.5
scikit-learn==1.7.2
scipy==1.15.3
six==1.17.0
threadpoolctl==3.7.0
tqdm==4.70.1
tzdata==2026.5
urllib3==2.8.0
ViennaRNA==2.7.2
```

## Distribution and licensing

X3DNA-DSSR is **not included** in the publicly distributed EXTRARNAS images or application. The authors used a separately licensed DSSR executable to generate its annotations. Redistributing the binary or licensed archive requires appropriate authorization. Users can reproduce downstream comparisons from the published interaction outputs without redistributing X3DNA-DSSR.


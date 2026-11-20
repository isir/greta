# Greta

Welcome to the public repository of the Greta project.

Greta is a virtual character engine that generates real-time socio-emotional behaviors to support natural interactions with human users. It is the result of research projects conducted by Catherine Pelachaud and her research group.

- **Platform:** Modular Java software designed for Java 8, primarily on Windows. Java 9 and later are not officially supported.
- **Licensing:** The `master` branch is licensed under the [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.html). The `master-lgpl` branch preserves the previous [GNU LGPL v3](https://www.gnu.org/licenses/lgpl-3.0.html) version. When distributing modifications, follow the terms of the applicable branch license.
- **External dependencies:** Speech synthesis via [MaryTTS](http://mary.dfki.de/) or CereProc, and 3D rendering via Ogre or Unity.

![Greta logo](https://user-images.githubusercontent.com/54807091/88184824-7c5ba280-cc33-11ea-875e-0a785d95075f.png)

## Updates

### Latest: Greta 2.1 --- Sam VAP turn-taking and MODIFF VQ-VAE real-time facial pipeline

This update integrates two complementary real-time systems: **Sam VAP** for reactive turn-taking and backchannel behaviors, and **MODIFF VQ-VAE** for continuous, context-dependent facial behavior generation. Both are integrated with Greta's OpenFace feature stream.

#### Sam VAP (TurnManagement + VAP model)

- Added a self-contained `TurnManagement/sam_vap/` bundle:
  - `vap_code/` contains the Python inference stack.
  - `checkpoints/` is the default location for VAP model weights, which are **not included in Git**.
  - `demo/noxi/` is optional and supports offline testing with `HybridRealTime.py`.
- Updated `RealTimeGreta.py` to load VAP from `sam_vap/vap_code/`, use `sam_vap/checkpoints/` by default, and resolve relative paths from `TurnManagement/`.
- Added launch scripts:
  - `run_RealTimeGreta.bat` for multimodal real-time inference.
  - `run_RealTimeGreta_audio_only.bat` for audio-only inference.
- Aligned `HybridRealTime.py` with file-based replay for offline experiments.
- Integrated the Java `TurnManagementModel`, which starts the batch-based Python pipeline and converts Python turn-management actions (head nods, verbal backchannels, and turn shifts) into FML messages. Actions are received on **TCP 5961**, and agent feedback uses **TCP 5960**.
- When Sam VAP is enabled, OpenFace features are streamed to Python over **TCP 5963**.
- VAP checkpoints can be requested from **pelachaud@isir.upmc.fr**. Large `.ckpt` files and demo `.wav`/`.npy` artifacts are excluded from Git.

#### MODIFF VQ-VAE (conditional facial behavior generation)

- Added the real-time inference pipeline at `bin/Common/Data/MODIFFVQ/RealTimePipeVQ.py`.
- Uses a **VQ-VAE** and an **E3 conditional transformer** to predict therapist upper-face Action Units (AUs) from 17 OpenFace client AU columns, with optional Wav2Vec2 audio features and dialog-act conditioning.
- The Greta `MODIFF_VQ` module receives predicted AU data as CSV over **TCP 5561**.
- Client features are forwarded from Greta over **TCP 5963** through the Sam VAP/OpenFace integration path. Optional dialog-act input uses **TCP 50202**.
- Expects the following model assets under `bin/Common/Data/MODIFFVQ/model/`:
  - `vqvae_model.pt`
  - `predictor_model.pt`
  - `norm_stats.npy`
- Runs using the **`greta_modiff_vqvae`** Conda environment and the `bat_run_modiff_vq.bat` launcher.
- Supports temporal smoothing, per-AU amplification, and optional AU12/AU06 coactivation controlled by `ENABLE_COACTIVATION`.

Together, **Sam VAP** generates reactive turn-taking behaviors while **MODIFF VQ-VAE** generates continuous empathic facial dynamics from the shared OpenFace feature stream. Example experiment configurations connect `TurnManagementModel`, `MODIFF_VQ`, and `OpenFace` (such as the **Nezih Sam Vap Greta XML** configuration).

### September 3, 2024: License change and new functionality (EN/FR)

- Changed the `master` branch license from LGPL v3 to GPL v3.
- Moved the previous LGPL version of `master` to the `master-lgpl` branch.
- Added an incremental behavior realizer module.
- Added an LLM module with Mistral, including incremental processing.
- Added DeepASR, a deep-learning-based incremental automatic speech recognition module.
- Added the MeaningMiner module.
- Added NVBG, a nonverbal behavior generator.
- At the time of this update, English and French were the supported languages.

## Installation and usage

For general instructions on installing, compiling, and using Greta, see the [Wiki](https://github.com/gretaproject/greta/wiki).

### Running the new real-time pipelines

1. **Prepare Sam VAP:** Place the VAP checkpoints in `TurnManagement/sam_vap/checkpoints/`. The `vap_code/` inference stack is included in the repository, but the checkpoint weights must be obtained separately.
2. **Prepare MODIFF VQ-VAE:** Make sure `vqvae_model.pt`, `predictor_model.pt`, and `norm_stats.npy` are available in `bin/Common/Data/MODIFFVQ/model/`, and set up the `greta_modiff_vqvae` Conda environment.
3. **Start the appropriate pipeline:** Use `run_RealTimeGreta.bat` for multimodal Sam VAP, `run_RealTimeGreta_audio_only.bat` for audio-only Sam VAP, or `bat_run_modiff_vq.bat` for MODIFF VQ-VAE. In integrated Greta experiments, the Java `TurnManagementModel` starts its configured Sam VAP batch launcher.
4. **Configure Greta:** Enable `OpenFace`, `TurnManagementModel`, and/or `MODIFF_VQ` in the relevant experiment XML, depending on which pipeline(s) you want to use. `HybridRealTime.py` supports file-based Sam VAP replay without a live session.

#### TCP port reference

| TCP port | Component / data flow | Purpose |
| --- | --- | --- |
| `5960` | Greta ↔ Sam VAP | Agent feedback |
| `5961` | Sam VAP → `TurnManagementModel` | Turn-taking actions mapped to FML |
| `5963` | OpenFace / Greta → Python | Multimodal client features shared with the Sam VAP and MODIFF integration |
| `5561` | MODIFF VQ-VAE → `MODIFF_VQ` | Predicted AU CSV |
| `50202` | Dialog-act client → MODIFF VQ-VAE | Optional dialog-act conditioning |

**Note:** Model checkpoints and weights must be available locally before running their respective inference pipelines. The Sam VAP checkpoint weights are not stored in Git.

## Functionality

The Greta project is divided into this public repository and a private repository where Catherine Pelachaud's research group develops future functionality.

If you use any resources provided by this project in a publication, please cite the corresponding paper(s).

### SAIBA architecture

Greta is a SAIBA-compliant agent architecture. It accepts Behavior Markup Language (BML) and Function Markup Language (FML) files to produce agent behaviors.

Mancini, M., Niewiadomski, R., Bevacqua, E., & Pelachaud, C. (2008, November).  
*Greta: a SAIBA compliant ECA system.*  
In *Troisième Workshop sur les Agents Conversationnels Animés*.

### MPEG-4 animation

Greta's animation engine produces MPEG-4-compatible animation, outputting frames of Body Animation Parameters (BAPs) and Facial Animation Parameters (FAPs). The platform can also accept these animation parameters to render a virtual character using Ogre3D or Unity3D.

Niewiadomski, R., Bevacqua, E., Mancini, M., & Pelachaud, C. (2009, May).  
*Greta: an interactive expressive ECA system.*  
In *Proceedings of the 8th International Conference on Autonomous Agents and Multiagent Systems, Volume 2* (pp. 1399–1400). International Foundation for Autonomous Agents and Multiagent Systems.

### Listener backchannels

An important part of human communication consists of subtle listening behaviors. These *backchannels* include head nods and short vocalizations such as “mm-hmm.” Greta provides a configurable system to generate these behaviors automatically and support more lifelike interactions.

Bevacqua, E., Pammi, S., Hyniewska, S. J., Schröder, M., & Pelachaud, C. (2010, September).  
*Multimodal backchannels for embodied conversational agents.*  
In *International Conference on Intelligent Virtual Agents* (pp. 194–200). Springer, Berlin, Heidelberg.


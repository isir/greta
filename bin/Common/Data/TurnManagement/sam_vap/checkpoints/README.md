# VAP checkpoints (not stored in Git)

The Sam VAP checkpoints are **~50 MB (audio-only)** and **~800 MB (multimodal)**. They are **excluded from this repository** (see root `.gitignore`).

## Request the files

Send an email to obtain a download link or archive:

**`pelachaud@isir.upmc.fr`**

Suggested subject: `Greta Sam VAP checkpoint request`

Include briefly:

- Your name and affiliation  
- Intended use (research / demo / integration)  
- Which Greta branch or release you cloned (e.g. `joel_modiff_vq`)

## Files to install here

After you receive them, place these files **in this folder** (`sam_vap/checkpoints/`):

| File | Mode | Used by |
|------|------|---------|
| `VapGPT_25Hz_ad20s_134-epoch9-val_0.04519_double_CAascade_double_shift1000_5S_25hz_Agressive_HNw.ckpt` | Multimodal (full Sam VAP) | `run_RealTimeGreta.bat` (default) |
| `VapGPT_25Hz_ad20s_134-epoch9-val_0.07268_Audio_only_5s_25hz_double_double_Shift_1000.ckpt` | Audio-only VAP | `run_RealTimeGreta_audio_only.bat` |

Paths in the batch files are relative to `TurnManagement/`; do not rename the files unless you also update the batch files and/or `--checkpoint` arguments.

## Verify

From `bin/Common/Data/TurnManagement/` (conda env `py311_rvap`):

```bat
python RealTimeGreta.py --checkpoint sam_vap\checkpoints\VapGPT_25Hz_ad20s_134-epoch9-val_0.04519_double_CAascade_double_shift1000_5S_25hz_Agressive_HNw.ckpt
```

If the checkpoint is missing, `RealTimeGreta.py` exits with an explicit “checkpoint not found” message pointing to this folder.

---

## Fichiers checkpoint (non versionnés)

Les poids `.ckpt` ne sont **pas** dans le dépôt Git (taille).

**Demande par courriel :** `pelachaud@isir.upmc.fr`

Copiez les deux fichiers listés ci-dessus dans ce répertoire après réception.

## Git / GitHub Desktop

Do **not** commit `.ckpt` files (GitHub limit 100 MB per file; multimodal checkpoint is ~800 MB). They are listed in the repository `.gitignore`. If GitHub Desktop still shows them, uncheck them in the commit list. If they were already staged:

## Maintainers (Git)

If checkpoints were committed before this policy, remove them from the index (files stay on disk):

```bat
git rm --cached bin/Common/Data/TurnManagement/sam_vap/checkpoints/*.ckpt
git commit -m "Stop tracking Sam VAP checkpoint binaries"
```

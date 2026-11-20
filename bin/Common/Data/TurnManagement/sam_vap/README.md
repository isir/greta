# Sam VAP assets (`TurnManagement/sam_vap`)

Self-contained VAP (Voice Activity Projection) bundle for Greta turn management:

| Path | Role | Version control |
|------|------|-----------------|
| `vap_code/` | Python model code (`model.py`, encoders, loaders, …) | **Committed** in the repo |
| `checkpoints/` | Trained `.ckpt` weights | **Not committed** — too large; see [checkpoints/README.md](checkpoints/README.md) |
| `demo/noxi/` | Optional Paris session files for `run_HybridRealTime.bat` | Small CSVs may be committed; **`.wav` / `.npy` are ignored** — see [demo/README.md](demo/README.md) |

## After cloning the repository

1. Read [checkpoints/README.md](checkpoints/README.md) and request the checkpoint files by email.
2. Copy the two `.ckpt` files into `checkpoints/` using the **exact filenames** listed there.
3. Launch Greta with the Sam VAP config (`TurnManagementModel` → `run_RealTimeGreta.bat`).

Override VAP Python path (optional): environment variable `GRETA_VAP_CODE_DIR`.

# Sam VAP demo data (optional)

Used only by **`run_HybridRealTime.bat`** (offline file-based inference), not by live Greta Sam VAP.

- **`noxi/`** — Paris multimodal sessions (audio, OpenFace-style CSVs, optional face image `.npy` under `video_paris/`).
- Paths in `run_HybridRealTime.bat` default to `sam_vap/demo/noxi/Paris_14-*`.

Large artifacts (`.wav`, `.npy`) are **gitignored**. You may copy them from your internal VAP dataset export or request them together with the checkpoints (see [../checkpoints/README.md](../checkpoints/README.md)).

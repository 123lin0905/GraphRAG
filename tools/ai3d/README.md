# AI 复原 / 程序化建模脚本

- `hy.py` / `hys.py`：调用 Hugging Face Space `tencent/Hunyuan3D-2`（免费 ZeroGPU）由单张文物照片生成网格（`hy.py` 含贴图，`hys.py` 仅几何）。用法：`python3 hys.py <name> <img> [steps] [octree]`，输出 `out/<name>_white.glb`。
- `finish.py`：清理退化面 → pyfqmr 减面 → 居中立于地面、归一化尺寸 → 赋予青铜/黄金/玉/象牙/陶 PBR 材质（顶点色 + metallic/roughness）→ 导出 GLB。用法：`python3 finish.py in.glb out.glb bronze 140000`。
- `keepmain.py`：只保留 AI 网格的主体连通分量，去掉照片中的展示支架/漂浮碎片。用法：`python3 keepmain.py in.glb out.glb [最小比例]`。
- `render.py`：渲染 +Z（glTF 正面）/±X 三个视角并输出扁平度，用于检查生成结果与朝向。用法：`python3 render.py in.glb out.png`。
- `proc.py`：程序化建模（金杖、象牙、玉边璋、太阳形器、龟背形网格状器、眼形器、青铜神树），输出到 `final/`。
- 依赖：`pip install gradio_client trimesh pyfqmr shapely mapbox_earcut scipy networkx matplotlib`。
生成结果属于推测性复原，非真实扫描，仅供教学演示。

"""照片浮雕法（薄片文物）：抠图 alpha 轮廓 → 挤出薄片厚度 → 按亮度做少量浮雕 → 轻微弧面。
python3 relief.py in_rgba.png out.glb [宽cm] [厚cm] [浮雕cm] [网格宽px]"""
import sys, numpy as np, trimesh
from PIL import Image
from scipy import ndimage
a = sys.argv
W_CM = float(a[3]) if len(a) > 3 else 21.0; T_CM = float(a[4]) if len(a) > 4 else 0.4
R_CM = float(a[5]) if len(a) > 5 else 0.35; GW = int(a[6]) if len(a) > 6 else 320
im = Image.open(a[1]).convert('RGBA')
bb = im.getchannel('A').point(lambda p: 255 if p > 128 else 0).getbbox(); im = im.crop(bb)
gh = int(round(GW * im.height / im.width)); im = im.resize((GW, gh), Image.LANCZOS)
A = np.asarray(im)[:, :, 3] > 128
A = ndimage.binary_opening(A, iterations=1)
lab, n = ndimage.label(A); sz = ndimage.sum(A, lab, range(1, n + 1))
A = np.isin(lab, 1 + np.where(sz >= 0.02 * sz.max())[0])  # 去掉支撑柱、碎点
L = np.asarray(im.convert('L'), float) / 255.0
L = ndimage.gaussian_filter(L, 1.2)
lo, hi = np.percentile(L[A], [5, 95]); L = np.clip((L - lo) / (hi - lo + 1e-6), 0, 1)
s = 1.0 / GW  # 每像素宽度（单位：整体宽 = 1）
t = T_CM / W_CM; r = R_CM / W_CM
# 顶点为像素角点 (gh+1, GW+1)
Hc = np.pad(L, 1, mode='edge'); Hv = (Hc[:-1, :-1] + Hc[1:, :-1] + Hc[:-1, 1:] + Hc[1:, 1:]) / 4
yy, xx = np.mgrid[0:gh + 1, 0:GW + 1]
X = (xx - GW / 2) * s; Y = (gh - yy) * s
bend = 0.035 * (1 - (2 * X) ** 2)  # 轻微弧面
Zf = bend + t / 2 + r * Hv; Zb = bend - t / 2
idx = lambda i, j: i * (GW + 1) + j
N = (gh + 1) * (GW + 1)
V = np.concatenate([np.c_[X.ravel(), Y.ravel(), Zf.ravel()], np.c_[X.ravel(), Y.ravel(), Zb.ravel()]])
F = []
ci, cj = np.where(A)
a0 = idx(ci, cj); a1 = idx(ci, cj + 1); a2 = idx(ci + 1, cj + 1); a3 = idx(ci + 1, cj)
F += [np.c_[a0, a3, a2], np.c_[a0, a2, a1]]              # 正面 (+Z)
F += [np.c_[a0 + N, a2 + N, a3 + N], np.c_[a0 + N, a1 + N, a2 + N]]  # 背面
P = np.pad(A, 1)
for di, dj, e0, e1 in [(-1, 0, a0, a1), (1, 0, a2, a3), (0, -1, a3, a0), (0, 1, a1, a2)]:
    m = ~P[ci + 1 + di, cj + 1 + dj]
    u, v = e0[m], e1[m]
    F += [np.c_[u, v + N, v], np.c_[u, u + N, v + N]]
mesh = trimesh.Trimesh(V, np.concatenate(F), process=True)
mesh.remove_unreferenced_vertices()
if not mesh.is_winding_consistent: trimesh.repair.fix_winding(mesh)
if mesh.volume < 0: mesh.invert()
trimesh.Scene(mesh).export(a[2]); print(a[2], len(mesh.faces), 'faces watertight', mesh.is_watertight, 'extent', np.ptp(mesh.bounds, 0).round(3))

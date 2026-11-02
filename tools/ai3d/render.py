"""Quick check render: python3 render.py in.glb out.png  (front/side/top, plus flatness ratio)"""
import sys, numpy as np, trimesh, matplotlib
matplotlib.use('Agg'); import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
s = trimesh.load(sys.argv[1], force='scene'); m = trimesh.util.concatenate(list(s.dump()))
if len(m.faces) > 40000:
    import finish
    m = finish.decimate(m, 40000)
ext = np.ptp(m.bounds, axis=0); print('extent xyz', ext.round(3), 'flat ratio', round(ext.min() / ext.max(), 3), 'faces', len(m.faces))
fig = plt.figure(figsize=(12, 4))
L = np.array([0.3, 0.5, 0.8]); L /= np.linalg.norm(L)
for i, (el, az, t) in enumerate([(10, 90, '+Z (glTF front)'), (10, 0, '+X'), (10, 180, '-X')]):
    ax = fig.add_subplot(1, 3, i + 1, projection='3d')
    V = m.vertices[:, [0, 2, 1]]  # y-up -> z-up
    sh = np.clip(np.abs(m.face_normals[:, [0, 2, 1]] @ L[[0, 2, 1]]), 0.15, 1)
    pc = Poly3DCollection(V[m.faces], facecolors=np.c_[sh * .8, sh * .65, sh * .4], edgecolor='none')
    ax.add_collection3d(pc); c = V.mean(0); r = np.ptp(V, 0).max() / 2
    ax.set_xlim(c[0]-r, c[0]+r); ax.set_ylim(c[1]-r, c[1]+r); ax.set_zlim(c[2]-r, c[2]+r)
    ax.view_init(el, az); ax.set_title(t); ax.axis('off')
plt.tight_layout(); plt.savefig(sys.argv[2], dpi=70)

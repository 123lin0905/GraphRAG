"""陶盉补第三足：照片只拍到两足，复制左足绕器身竖轴旋转 -90° 放到背面，形成三足。python3 taohe_leg.py in.glb out.glb"""
import sys, numpy as np, trimesh
m = trimesh.util.concatenate(list(trimesh.load(sys.argv[1], force='scene').dump()))
v, f = m.vertices, m.faces
cx = -0.10; cz = float(np.median(v[np.abs(v[:, 1] + 0.05) < 0.02][:, 2]))
sel = np.all((v[f][:, :, 1] < -0.16) & (v[f][:, :, 0] < -0.09), axis=1)
leg = m.submesh([np.where(sel)[0]], append=True)
T = trimesh.transformations.rotation_matrix(np.radians(-90), [0, 1, 0], [cx, 0, cz])
leg.apply_transform(T)
out = trimesh.util.concatenate([m, leg])
trimesh.Scene(out).export(sys.argv[2]); print(len(leg.faces), 'leg faces', out.bounds.round(2))

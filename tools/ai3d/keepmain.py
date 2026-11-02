"""Keep main connected component(s) of an AI mesh (drops stands/floaters): python3 keepmain.py in.glb out.glb [min_frac]"""
import sys, trimesh
m = trimesh.util.concatenate(list(trimesh.load(sys.argv[1], force='scene').dump()))
cs = sorted(m.split(only_watertight=False), key=lambda c: -len(c.faces))
frac = float(sys.argv[3]) if len(sys.argv) > 3 else 0.05
keep = [c for c in cs if len(c.faces) >= frac * len(cs[0].faces)]
out = trimesh.util.concatenate(keep); out.export(sys.argv[2]); print(len(cs), '->', len(keep), len(out.faces))

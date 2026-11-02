"""Apply PBR material (vertex-colour patina) to a mesh, decimate, centre/orient, export GLB.
usage: python3 finish.py <in.glb> <out.glb> <material> [target_faces] [rotx_deg] [roty_deg]
"""
import sys, numpy as np, trimesh
from trimesh.visual.material import PBRMaterial

MATS = {
    # base rgb, alt rgb (patina / variation), metallic, roughness
    'bronze':  ((0.47, 0.36, 0.22), (0.30, 0.37, 0.29), 0.70, 0.50),
    'gold':    ((1.00, 0.76, 0.33), (0.85, 0.60, 0.22), 1.00, 0.28),
    'jade':    ((0.60, 0.67, 0.52), (0.42, 0.50, 0.38), 0.00, 0.30),
    'ivory':   ((0.90, 0.84, 0.70), (0.72, 0.62, 0.46), 0.00, 0.55),
    'pottery': ((0.64, 0.45, 0.32), (0.48, 0.33, 0.24), 0.00, 0.90),
}


def noise3(p, freq):
    # cheap smooth-ish value noise via summed sines (deterministic)
    rng = np.random.default_rng(7)
    acc = np.zeros(len(p))
    for k in range(6):
        d = rng.normal(size=3); d /= np.linalg.norm(d)
        f = freq * (1.7 ** k)
        acc += np.sin(p @ d * f + rng.uniform(0, 6.28)) / (1.6 ** k)
    return acc


def paint(mesh, mat):
    base, alt, metal, rough = MATS[mat]
    v = mesh.vertices
    ext = np.ptp(v, axis=0).max()
    p = (v - v.mean(0)) / ext
    n = noise3(p, 9.0)
    t = np.clip((n - 0.1) * 0.55 + 0.3, 0, 1)
    if mat == 'bronze':
        # patina collects in cavities: use vertex normal facing down / inward hint + noise
        t = np.clip(t * 0.85 + 0.15 * (mesh.vertex_normals[:, 1] < -0.2), 0, 1)
    c = np.array(base)[None] * (1 - t[:, None]) + np.array(alt)[None] * t[:, None]
    c *= (0.9 + 0.1 * np.sin(noise3(p, 30.0)))[:, None]
    c = np.clip(c, 0, 1)
    # vertex colours are linear in glTF; convert approx sRGB->linear
    lin = c ** 2.2
    rgba = np.concatenate([(lin * 255).astype(np.uint8), np.full((len(v), 1), 255, np.uint8)], 1)
    m = PBRMaterial(name=mat, baseColorFactor=[255, 255, 255, 255], metallicFactor=metal, roughnessFactor=rough, doubleSided=True)
    mesh.visual = trimesh.visual.ColorVisuals(mesh, vertex_colors=rgba)
    return m


def decimate(mesh, target):
    import pyfqmr
    m = trimesh.Trimesh(mesh.vertices, mesh.faces, process=True)
    m.update_faces(m.nondegenerate_faces()); m.update_faces(m.unique_faces()); m.remove_unreferenced_vertices()
    if len(m.faces) <= target:
        return m
    ms = pyfqmr.Simplify()
    ms.setMesh(np.ascontiguousarray(m.vertices, dtype=np.float64), np.ascontiguousarray(m.faces, dtype=np.int32))
    ms.simplify_mesh(target_count=target, aggressiveness=7, preserve_border=True, verbose=False)
    v, f, _ = ms.getMesh()
    return trimesh.Trimesh(v, f, process=True)


def finish(mesh, out, mat, target=160000, rx=0.0, ry=0.0, flat=False):
    mesh = decimate(mesh, target)
    if flat:
        mesh.unmerge_vertices()
    if rx: mesh.apply_transform(trimesh.transformations.rotation_matrix(np.radians(rx), [1, 0, 0]))
    if ry: mesh.apply_transform(trimesh.transformations.rotation_matrix(np.radians(ry), [0, 1, 0]))
    b = mesh.bounds
    mesh.apply_translation([-(b[0][0] + b[1][0]) / 2, -b[0][1], -(b[0][2] + b[1][2]) / 2])
    mesh.apply_scale(1.0 / np.ptp(mesh.bounds, axis=0).max())
    if mesh.is_watertight: mesh.fix_normals()
    pbr = paint(mesh, mat)
    # export via a scene with the PBR material attached as a TextureVisuals-free material: write manually
    sc = trimesh.Scene()
    sc.add_geometry(mesh, node_name='model', geom_name='model')
    glb = trimesh.exchange.gltf.export_glb(sc, include_normals=True)
    glb = set_material(glb, pbr)
    open(out, 'wb').write(glb)
    print(out, len(mesh.faces), 'faces', round(len(glb) / 1e6, 2), 'MB')


def set_material(glb, pbr):
    """Replace/insert a PBR material on every primitive (trimesh ColorVisuals export has no PBR params)."""
    import json, struct
    l0 = struct.unpack('<I', glb[12:16])[0]
    js = json.loads(glb[20:20 + l0])
    rest = glb[20 + l0:]
    js['materials'] = [{'name': pbr.name, 'doubleSided': True, 'pbrMetallicRoughness': {
        'baseColorFactor': [1, 1, 1, 1], 'metallicFactor': float(pbr.metallicFactor), 'roughnessFactor': float(pbr.roughnessFactor)}}]
    for m in js['meshes']:
        for p in m['primitives']:
            p['material'] = 0
    j = json.dumps(js, separators=(',', ':')).encode()
    j += b' ' * ((4 - len(j) % 4) % 4)
    body = struct.pack('<II', len(j), 0x4E4F534A) + j + rest
    return struct.pack('<III', 0x46546C67, 2, 12 + len(body)) + body


if __name__ == '__main__':
    a = sys.argv
    s = trimesh.load(a[1], force='scene')
    mesh = trimesh.util.concatenate(list(s.dump()))
    finish(mesh, a[2], a[3], int(a[4]) if len(a) > 4 else 160000, float(a[5]) if len(a) > 5 else 0, float(a[6]) if len(a) > 6 else 0)

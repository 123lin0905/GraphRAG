"""Procedural (程序化) models for artifacts with simple, well-documented geometry.
Writes raw meshes to out/<name>_proc.glb, then finish.py paints/normalises.
Proportions follow public descriptions (approximate; teaching demo only)."""
import numpy as np, trimesh
from shapely.geometry import Polygon, Point
from shapely import affinity
from trimesh.creation import cylinder, icosphere, torus, extrude_polygon
import finish

R = trimesh.transformations.rotation_matrix
OUT = 'final'


def sweep(path, radii, sides=24):
    """Tube along polyline `path` (N,3) with per-point radius; closed caps."""
    path = np.asarray(path, float); n = len(path)
    t = np.gradient(path, axis=0); t /= np.linalg.norm(t, axis=1)[:, None]
    up = np.array([0, 0, 1.0]) if abs(t[0][2]) < 0.9 else np.array([1.0, 0, 0])
    nrm = np.cross(t[0], up); nrm /= np.linalg.norm(nrm)
    frames = []
    for i in range(n):  # parallel transport
        if i:
            nrm = nrm - t[i] * (nrm @ t[i]); nrm /= np.linalg.norm(nrm)
        frames.append((nrm.copy(), np.cross(t[i], nrm)))
    ang = np.linspace(0, 2 * np.pi, sides, endpoint=False)
    V = []
    for i in range(n):
        a, b = frames[i]
        V += [path[i] + radii[i] * (np.cos(q) * a + np.sin(q) * b) for q in ang]
    F = []
    for i in range(n - 1):
        for j in range(sides):
            p0, p1 = i * sides + j, i * sides + (j + 1) % sides
            F += [[p0, p1 + sides, p1], [p0, p0 + sides, p1 + sides]]
    V = np.array(V); c0 = len(V); c1 = c0 + 1
    V = np.vstack([V, path[0], path[-1]])
    for j in range(sides):
        F += [[c0, j, (j + 1) % sides], [c1, (n - 1) * sides + (j + 1) % sides, (n - 1) * sides + j]]
    m = trimesh.Trimesh(V, F, process=True); m.fix_normals()
    return m


def jinzhang():
    # 金杖: ~142cm long, ~2.3cm diameter, gold sheath; engraved section (人头/鱼/鸟) near one end shown as relief bands
    L, r = 1.42, 0.0115
    m = cylinder(radius=r, height=L, sections=64)
    m = m.subdivide().subdivide()
    v = m.vertices; z = v[:, 2]
    # engraved section ~46cm at upper end: shallow relief chevrons + rings
    sec = (z > L / 2 - 0.46) & (z < L / 2 - 0.02)
    th = np.arctan2(v[:, 1], v[:, 0])
    relief = np.where(sec, 0.0009 * (np.sin(z * 260 + 3 * th) > 0.6) + 0.0006 * (np.abs(np.sin(z * 40)) > 0.97), 0)
    v[:, :2] *= (1 + relief / r)[:, None]
    m.vertices = v
    for zz in (L / 2 - 0.47, L / 2 - 0.015, -L / 2 + 0.015):
        m += torus(r, 0.0012, major_sections=64, minor_sections=8).apply_translation([0, 0, zz])
    m.apply_transform(R(np.pi / 2, [0, 1, 0]))  # lie along X
    m.apply_translation([0, 0.06, 0])
    # low display stand (two cradles) in same gold would be misleading -> omit; rod floats slightly above floor
    return m, 'gold'


def xiangya():
    # 象牙: curved tapering tusk ~1.2m
    s = np.linspace(0, 1, 80)
    ang = s * np.radians(70)
    Rc = 0.95
    path = np.stack([Rc * np.sin(ang), Rc * (1 - np.cos(ang)) + 0.09, 0.08 * s ** 2], 1)
    radii = 0.075 * (1 - s) ** 0.75 + 0.004
    m = sweep(path, radii, sides=40)
    v = m.vertices  # growth-ring ripples
    m.vertices = v + m.vertex_normals * (0.0012 * np.sin(v[:, 0] * 90))[:, None]
    return m, 'ivory'


def yuzhang():
    # 玉边璋（牙璋）: thin flat blade, forked concave tip, handle with side teeth (扉牙) and a perforation
    w_h, w_b, L = 0.07, 0.10, 0.60  # handle width, blade end width, total length (scaled; real ~54cm)
    right = [(w_h / 2, 0), (w_h / 2, 0.12)]
    for k in range(4):  # 扉牙 teeth
        y = 0.125 + k * 0.018
        right += [(w_h / 2 + 0.014, y), (w_h / 2 + 0.006, y + 0.009), (w_h / 2 + 0.014, y + 0.012), (w_h / 2, y + 0.018)]
    right += [(w_h / 2 + 0.004, 0.21), (w_b / 2, L - 0.06), (w_b / 2 + 0.006, L), (0, L - 0.075)]
    left = [(-x, y) for x, y in right[:-1]][::-1]
    poly = Polygon(right + left)
    poly = poly.buffer(0)
    poly = poly.difference(Point(0, 0.07).buffer(0.008, 24))
    m = extrude_polygon(poly, 0.010)
    m.apply_translation([0, 0, -0.005])
    return m, 'jade'


def taiyang():
    # 太阳形器: ~85cm wheel, raised central boss, 5 spokes, outer rim; stands upright facing +Z
    D = 0.85; rim_r = D / 2 - 0.035
    parts = [torus(rim_r, 0.03, major_sections=128, minor_sections=24)]
    boss = icosphere(4, radius=0.14); v = boss.vertices; v[:, 2] = np.maximum(v[:, 2], 0) * 0.75; boss.vertices = v
    parts += [boss, torus(0.14, 0.018, major_sections=96, minor_sections=16)]
    for k in range(5):
        a = np.pi / 2 + k * 2 * np.pi / 5
        p0 = np.array([0.15 * np.cos(a), 0.15 * np.sin(a), 0]); p1 = np.array([rim_r * np.cos(a), rim_r * np.sin(a), 0])
        s = np.linspace(0, 1, 24)[:, None]
        parts.append(sweep(p0 + s * (p1 - p0), 0.028 - 0.012 * s[:, 0], sides=20))
    for k in range(5):  # small mounting holes look: knobs on rim
        a = np.pi / 2 + k * 2 * np.pi / 5
        parts.append(icosphere(2, radius=0.02).apply_translation([rim_r * np.cos(a), rim_r * np.sin(a), 0.022]))
    m = trimesh.util.concatenate(parts)
    m.apply_translation([0, D / 2, 0])
    return m, 'bronze'


def guibei():
    # 龟背形网格状器: oval domed bronze lattice (two shells), side handles
    a, b, c = 0.30, 0.20, 0.09  # semi-axes x (length), z (width), y (height of each shell)
    def surf(u, w, sign):  # u in [-1,1] along x, w along z
        r2 = u ** 2 + w ** 2
        return np.stack([a * u, sign * c * np.sqrt(np.clip(1 - r2, 0, 1)), b * w], -1)
    parts = []
    for sign in (1, -1):
        for g in np.linspace(-0.85, 0.85, 9):  # bars along z
            lim = np.sqrt(1 - g ** 2) * 0.995; t = np.linspace(-lim, lim, 40)
            parts.append(sweep(surf(np.full_like(t, g), t, sign), np.full(40, 0.007), 10))
        for g in np.linspace(-0.85, 0.85, 7):  # bars along x
            lim = np.sqrt(1 - g ** 2) * 0.995; t = np.linspace(-lim, lim, 50)
            parts.append(sweep(surf(t, np.full_like(t, g), sign), np.full(50, 0.007), 10))
    q = np.linspace(0, 2 * np.pi, 160)
    ring = np.stack([a * np.cos(q), 0 * q, b * np.sin(q)], 1)
    parts.append(sweep(ring, np.full(160, 0.016), 14))
    for sx in (1, -1):  # handles (simplified loops)
        h = torus(0.045, 0.011, major_sections=48, minor_sections=12)
        h.apply_transform(R(np.pi / 2, [1, 0, 0]))
        h.apply_translation([sx * (a + 0.035), 0, 0])
        parts.append(h)
    m = trimesh.util.concatenate(parts)
    m.apply_translation([0, c + 0.01, 0])
    return m, 'bronze'


def yanxing():
    # 眼形器: lozenge (菱形) bronze plate with raised eyeball boss, rim, corner holes; upright
    W, H = 0.62, 0.30
    outer = Polygon([(-W / 2, 0), (0, H / 2), (W / 2, 0), (0, -H / 2)])
    plate = extrude_polygon(outer.difference(Point(0, 0).buffer(0.002)), 0.008)
    rimp = outer.difference(affinity.scale(outer, 0.88, 0.82))
    rim = extrude_polygon(rimp, 0.02)
    holes = []
    eye = icosphere(4, radius=0.08); v = eye.vertices; v[:, 0] *= 1.6; v[:, 2] = np.maximum(v[:, 2], 0) * 0.7; eye.vertices = v
    pupil = icosphere(3, radius=0.035); v = pupil.vertices; v[:, 2] = np.maximum(v[:, 2], 0) * 0.6; pupil.vertices = v
    pupil.apply_translation([0, 0, 0.052])
    lid = torus(0.085, 0.009, major_sections=96, minor_sections=12); lid.vertices[:, 0] *= 1.6
    m = trimesh.util.concatenate([plate, rim, eye, pupil, lid])
    # gentle curvature like the originals
    v = m.vertices; v[:, 2] -= 0.25 * (v[:, 0] ** 2); m.vertices = v
    m.apply_translation([0, H / 2 + 0.01, 0])
    return m, 'bronze'


def shenshu():
    # 青铜神树（一号神树）: 三山形底座, 主干, 三层九枝, 枝端花果与立鸟, 一龙沿干而下（形制简化）
    parts = []
    H = 1.0
    base_ring = np.linspace(0, 2 * np.pi, 3, endpoint=False)
    for a in base_ring:  # three arched "mountain" legs
        t = np.linspace(0, np.pi, 30)
        d = np.array([np.cos(a), 0, np.sin(a)]); p = np.array([-np.sin(a), 0, np.cos(a)])
        path = [0.11 * np.cos(tt) * p + 0.11 * d + np.array([0, 0.13 * np.sin(tt), 0]) for tt in t]
        parts.append(sweep(np.array(path), np.full(30, 0.016), 14))
    parts.append(torus(0.12, 0.012, major_sections=96, minor_sections=12).apply_transform(R(np.pi / 2, [1, 0, 0])).apply_translation([0, 0.004, 0]))
    ys = np.linspace(0.12, H, 60)
    parts.append(sweep(np.stack([0 * ys, ys, 0 * ys], 1), 0.022 - 0.012 * (ys - 0.12) / H, 20))
    for tier, (h, L) in enumerate([(0.38, 0.26), (0.62, 0.21), (0.84, 0.16)]):
        for k in range(3):
            a = k * 2 * np.pi / 3 + tier * np.pi / 3
            d = np.array([np.cos(a), 0, np.sin(a)])
            s_ = np.linspace(0, 1, 40)
            path = np.array([d * L * si + np.array([0, h + L * (0.55 * np.sin(np.pi * si * 0.9) - 0.35 * si ** 2), 0]) for si in s_])
            parts.append(sweep(path, 0.009 - 0.004 * s_, 12))
            end = path[-1]
            fruit = icosphere(2, radius=0.022); fruit.vertices[:, 1] *= 1.25
            parts.append(fruit.apply_translation(end + [0, -0.012, 0]))
            # small bird standing on the fruit
            body = icosphere(2, radius=0.016); body.vertices[:, 0] *= 1.6
            body.apply_transform(R(-a, [0, 1, 0])).apply_translation(end + [0, 0.026, 0])
            head = icosphere(1, radius=0.009).apply_translation(end + d * 0.022 + [0, 0.04, 0])
            parts += [body, head]
            mid = path[18]  # flower bud midway, pointing up
            bud = icosphere(1, radius=0.013); bud.vertices[:, 1] *= 1.5
            parts.append(bud.apply_translation(mid + [0, 0.02, 0]))
    top = icosphere(2, radius=0.03); top.vertices[:, 1] *= 1.3
    parts.append(top.apply_translation([0, H + 0.02, 0]))
    t = np.linspace(0, 1, 120)  # dragon descending the trunk
    path = np.stack([0.06 * np.cos(t * 7) + 0.03, H * 0.9 - t * (H * 0.78), 0.06 * np.sin(t * 7)], 1)
    parts.append(sweep(path, 0.012 * np.sin(np.pi * np.clip(t * 1.1, 0, 1)) + 0.003, 12))
    head = icosphere(2, radius=0.025); head.vertices[:, 0] *= 1.6
    parts.append(head.apply_translation(path[-1] + [0.02, -0.01, 0]))
    m = trimesh.util.concatenate(parts)
    return m, 'bronze'


if __name__ == '__main__':
    import sys
    for name in (sys.argv[1:] or ['jinzhang', 'xiangya', 'yuzhang', 'taiyang', 'guibei', 'yanxing', 'shenshu']):
        mesh, mat = globals()[name]()
        mesh.export(f'out/{name}_proc.glb')
        finish.finish(mesh, f'{OUT}/{name}.glb', mat, 200000, flat=(name == 'yuzhang'))

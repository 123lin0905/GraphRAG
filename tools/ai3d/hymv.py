# Hunyuan3D-2mv 多视角形状生成：python3 hymv.py <name> <front> <left|-> [steps] [octree]
import sys, shutil, time
from gradio_client import Client, handle_file
name, front, left = sys.argv[1:4]
c = Client('tencent/Hunyuan3D-2mv', verbose=False)
t=time.time()
r = c.predict(None, None, handle_file(front), None, None if left=='-' else handle_file(left), None,
  int(sys.argv[4]) if len(sys.argv)>4 else 30, 5.0, 1234, int(sys.argv[5]) if len(sys.argv)>5 else 256, True, 8000, False, api_name='/shape_generation')
print(r[2] if len(r)>2 else r, time.time()-t)
p=r[0]["value"] if isinstance(r[0],dict) else r[0]; shutil.copy(p, f'out/{name}_white.glb')

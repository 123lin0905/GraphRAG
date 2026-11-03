# Hunyuan3D-2mv 多视角 + 贴图：python3 hymvt.py <name> <front> <left|->
import sys, shutil, time
from gradio_client import Client, handle_file
name, front, left = sys.argv[1:4]
c = Client('tencent/Hunyuan3D-2mv', verbose=False)
t=time.time()
r = c.predict(None, None, handle_file(front), None, None if left=='-' else handle_file(left), None,
  30, 5.0, 1234, 256, True, 8000, False, api_name='/generation_all')
print(r[3] if len(r)>3 else r, time.time()-t)
g=lambda x: x["value"] if isinstance(x,dict) else x
shutil.copy(g(r[0]), f'out/{name}_white.glb'); shutil.copy(g(r[1]), f'out/{name}_tex.glb')

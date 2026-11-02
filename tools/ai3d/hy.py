import sys, shutil, time, os
from gradio_client import Client, handle_file
name, img = sys.argv[1], sys.argv[2]
c = Client('tencent/Hunyuan3D-2', verbose=False)
t=time.time()
r = c.predict(None, handle_file(img), None,None,None,None, 30, 5.0, 1234, 256, True, 8000, False, api_name='/generation_all')
print(r[0], r[1], r[3], time.time()-t)
shutil.copy(r[0], f'out/{name}_white.glb')
shutil.copy(r[1], f'out/{name}_tex.glb')

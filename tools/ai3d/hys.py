import sys, shutil, time
from gradio_client import Client, handle_file
name, img = sys.argv[1], sys.argv[2]
c = Client('tencent/Hunyuan3D-2', verbose=False)
t=time.time()
r = c.predict(None, handle_file(img), None,None,None,None, int(sys.argv[3]) if len(sys.argv)>3 else 30, 5.0, 1234, int(sys.argv[4]) if len(sys.argv)>4 else 256, True, 8000, False, api_name='/shape_generation')
print(r, time.time()-t)
p=r[0]["value"] if isinstance(r[0],dict) else r[0]; shutil.copy(p, f'out/{name}_white.glb')

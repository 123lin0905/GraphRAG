from PIL import Image
import sys
from rembg import remove, new_session
s=new_session(sys.argv[1])
jobs={'niutou':('niutou_wiki.jpg',(0,0,1,0.93)),'niutou_side':('niutou_jinsha.jpg',(0.25,0.08,0.8,0.93)),
'longhuzun':('longhu_jin11.jpg',(60/530,40/600,495/530,570/600)),
'shoumian':('shoumian_wm556.jpg',(85/520,110/600,440/520,378/600)),
'taohe':('taohe_wiki.jpg',(95/450,0,360/450,505/600))}
for n,(f,(a,b,c,d)) in jobs.items():
  im=Image.open('refs/'+f).convert('RGB'); W,H=im.size
  im=im.crop((int(a*W),int(b*H),int(c*W),int(d*H))); im.thumbnail((1024,1024))
  o=remove(im,session=s); o.save(f'prep/{n}_'+sys.argv[1]+'.png')
  bg=Image.new('RGB',o.size,(255,0,255)); bg.paste(o,mask=o.split()[3]); bg.thumbnail((400,400)); bg.save(f'prep/v_{n}_'+sys.argv[1]+'.jpg')
  print(n,o.size)

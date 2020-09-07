import requests
res = requests.post('http://192.168.20.170:5000/syncImage', json={"mytext":"lalala"})
if res.ok:
    print(res.json())

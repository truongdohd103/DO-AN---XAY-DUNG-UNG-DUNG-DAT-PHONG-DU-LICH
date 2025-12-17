import requests
r = requests.post('http://127.0.0.1:5000/api/chat',
                  json={'message':'Hướng dẫn tôi đặt phòng','session_id':'test1'}, timeout=120)
print(r.json()['response'])
import requests
import time

while True:
    requests.post("http://localhost:8080/simulateCode", json={'code': 200})
    time.sleep(0.05)
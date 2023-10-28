import requests
import time
import random

while True:
    status_code = random.choice([500, 404, 401])
    requests.post("http://localhost:8080/simulateCode", json={'code': status_code})
    time.sleep(0.1)
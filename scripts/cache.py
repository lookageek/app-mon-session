import requests
import time

while True:
    requests.get("http://localhost:8080/cache")
    time.sleep(3)
import requests
import time

while True:
    requests.get("http://localhost:8080/getData?chance=2")
    time.sleep(0.2)
### Docker Setup

create a private docker network so that both prometheus and grafana sit in the network
```sh
docker network create monitoring
```

Use the prometheus.yml provided to start up the prometheus instance, it will start scraping data from the `localhost:9090`

```sh
docker run -d \
  --name prometheus \
  -p 9090:9090 \
  --network monitoring \
  -v /path/to/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

Once this is up, you need to download the grafana container and run that too

```sh
docker run -d \
  --name grafana \
  -p 3000:3000 \
  --network monitoring \
  grafana/grafana
```

### Setting up grafana

Open grafana in `localhost:3000`, default username/password is admin/admin, it will ask to change.

Then go to data sources and add the prometheus data source. Only thing to fill is the prometheus URL, for which one needs to find out the IP address where docker container of prometheus is running
This can found in inspecting the docker container from the shell

```sh
docker ps
# find the container ID for the prometheus
docker inspect container_id
# find the IP address in at the end
```

### APIs

- POST `/simulateCode` takes a JSON payload of status code which you want the API to give you back, like 
  ```json
  {"code": 200}  

  ```
- GET `/getData?chance=20` simulates a slow database query % of times which you send as parameter in chance. For chance=20, a slow DB query is simulated by `IO.sleep`. And the amount is sleep is random between 100 and 1000 millis. 
- GET `/cache` simulates a changing cache size after an operation, items in a cache can go up and down. And this endpoint assigns a random number between 1 and 100 to a custom Gauge metric.
- GET `/queryTimer` wraps an operation which can take a non-deterministic amount of time to complete - simulated by an `IO.sleep` and a histogram metric `db query time`  is used to measure just that operation and not other parts of the request processing
- GET `/metrics` provides metrics is prometheus format that is scraped by the daemon

Tapir provides prometheus middleware out of the box that has counters & histogram for response status code & response time respectively, for all the APIs defined. A gauge & a histogram are created in code to show bespoke metric collection is possible too.


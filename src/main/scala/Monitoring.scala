import cats.effect.IO
import io.prometheus.client.{Counter, Gauge, Histogram}
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

object Monitoring {
  val prometheusMetrics: PrometheusMetrics[IO] = PrometheusMetrics.default[IO]()
  val histogram: Histogram = Histogram.build("db query time", "db query time")
    .register(Monitoring.prometheusMetrics.registry)
  val guage: Gauge = Gauge.build("cacheSize", "cacheSize").register(Monitoring.prometheusMetrics.registry)
}

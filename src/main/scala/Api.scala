
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.comcast.ip4s.IpLiteralSyntax
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

import scala.concurrent.Future
import scala.util.Random
import cats.syntax.all._
import cats.effect._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.language.higherKinds

object Api extends IOApp with StrictLogging {

  private def createServer(): Resource[IO, Server] = {
    case class HttpCode(code: Int)

    val statusCodeEndpoint: ServerEndpoint[Any, IO] =
      endpoint.post
        .in("simulateCode")
        .in(jsonBody[HttpCode])
        .out(statusCode.and(jsonBody[String]))
        .serverLogicPure { p =>
          p.code match {
            case 200 => Right((StatusCode.Ok, "Success"))
            case 404 => Right((StatusCode.NotFound, "Not Found"))
            case 401 => Right((StatusCode.Unauthorized, "Unauthorized"))
            case _   => Right((StatusCode.InternalServerError, "Internal Server Error"))
          }
        }

    val chanceParameter = query[Int]("chance").description("Chance of Slow DB Query")

    val databaseEndpoint: ServerEndpoint[Any, IO] =
      endpoint.get
        .in("getData")
        .in(chanceParameter)
        .out(statusCode.and(jsonBody[String]))
        .serverLogic { c =>

          val random = new Random()
          val chance = 10 - c
          val randNum = random.nextInt(10) + 1

          if (randNum > chance) {
            for {
              _ <- IO.sleep((randNum * 100).milliseconds)
            } yield Right((StatusCode.Ok, "Done"))
          } else {
            IO.pure(Right((StatusCode.Ok, "Done")))
          }
        }

    val cacheData: ServerEndpoint[Any, IO] =
      endpoint.get
        .in("cache")
        .out(statusCode)
        .serverLogicPure { p =>
          val random = new Random()
          Monitoring.guage.set(random.nextInt(100))

          Right(StatusCode.Ok)
        }

    val timeTheQuery: ServerEndpoint[Any, IO] =
      endpoint.get
        .in("queryTimer")
        .out(statusCode)
        .serverLogic { c =>
          val random = new Random()
          for {
            _   <- IO.sleep(10.milliseconds) // <- do not care to measure
            res <- Clock[IO].timed(IO.sleep((random.nextInt(10) * 100).nanoseconds)) // <- measure this operation
          } yield {
            Monitoring.histogram.observe(res._1.length)
            Right(StatusCode.Ok)
          }
        }

    val serverOptions: Http4sServerOptions[IO] = Http4sServerOptions
      .customiseInterceptors[IO]
      .metricsInterceptor(Monitoring.prometheusMetrics.metricsInterceptor())
      .options

    val endpoints =
      List(
        statusCodeEndpoint,
        databaseEndpoint,
        cacheData,
        timeTheQuery,
        Monitoring.prometheusMetrics.metricsEndpoint
      )

    val routes: HttpRoutes[IO] = Http4sServerInterpreter[IO](serverOptions).toRoutes(endpoints)

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(Router("/" -> routes).orNotFound)
      .build
  }

  override def run(args: List[String]): IO[ExitCode] = createServer().use(_ => IO.never).as(ExitCode.Success)
}
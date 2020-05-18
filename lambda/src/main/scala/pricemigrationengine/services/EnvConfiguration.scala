package pricemigrationengine.services

import java.lang.System.getenv
import java.time.LocalDate

import pricemigrationengine.model.{CohortTableConfig, Config, ConfigurationFailure, DynamoDBConfig, DynamoDBEndpointConfig, ZuoraConfig}
import zio.{IO, ZIO, ZLayer}

object EnvConfiguration {
  def env(name: String): IO[ConfigurationFailure, String] =
    optionalEnv(name)
      .collect(ConfigurationFailure(s"No value for '$name' in environment")) {
        case Some(value) => value
      }

  def optionalEnv(name: String): IO[ConfigurationFailure, Option[String]] =
    ZIO
      .effect(Option(getenv(name)))
      .mapError(e => ConfigurationFailure(e.getMessage))

  val impl: ZLayer[Any, Nothing, Configuration] = ZLayer.succeed {
    new Configuration.Service {
      val config: IO[ConfigurationFailure, Config] = for {
        stage <- env("stage")
        earliestStartDate <- env("earliestStartDate").map(LocalDate.parse)
        batchSize <- env("batchSize").map(_.toInt)
      } yield
        Config(
          earliestStartDate,
        )
    }
  }

  val zuoraImpl: ZLayer[Any, Nothing, ZuoraConfiguration] = ZLayer.succeed {
    new ZuoraConfiguration.Service {
      val config: IO[ConfigurationFailure, ZuoraConfig] = for {
        zuoraApiHost <- env("zuoraApiHost")
        zuoraClientId <- env("zuoraClientId")
        zuoraClientSecret <- env("zuoraClientSecret")
      } yield
          ZuoraConfig(
            zuoraApiHost,
            zuoraClientId,
            zuoraClientSecret
          )
    }
  }

  val dynamoDbImpl: ZLayer[Any, Nothing, DynamoDBConfiguration] = ZLayer.succeed {
    new DynamoDBConfiguration.Service {
      val config: IO[ConfigurationFailure, DynamoDBConfig] = for {
        dynamoDBServiceEndpointOption <- optionalEnv("dynamodb.serviceEndpoint")
        dynamoDBSigningRegionOption <- optionalEnv("dynamodb.signingRegion")
        dynamoDBEndpoint = dynamoDBServiceEndpointOption
          .flatMap(endpoint => dynamoDBSigningRegionOption.map(region => DynamoDBEndpointConfig(endpoint, region)))
      } yield
        DynamoDBConfig(
          dynamoDBEndpoint
        )
    }
  }

  val cohortTableImp: ZLayer[Any, Nothing, CohortTableConfiguration] = ZLayer.succeed {
    new CohortTableConfiguration.Service {
      val config: IO[ConfigurationFailure, CohortTableConfig] = for {
        stage <- env("stage")
        batchSize <- env("batchSize").map(_.toInt)
      } yield
        CohortTableConfig(
          stage,
          batchSize
        )
    }
  }

}

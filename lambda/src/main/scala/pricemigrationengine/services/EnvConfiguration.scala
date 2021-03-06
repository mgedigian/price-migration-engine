package pricemigrationengine.services

import java.lang.System.getenv

import pricemigrationengine.model._
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
        batchSize <- env("batchSize").map(_.toInt)
      } yield
        CohortTableConfig(
          batchSize
        )
    }
  }

  val salesforceImp: ZLayer[Any, Nothing, SalesforceConfiguration] = ZLayer.succeed {
    new SalesforceConfiguration.Service {
      val config: IO[ConfigurationFailure, SalesforceConfig] = for {
        salesforceAuthUrl <- env("salesforceAuthUrl")
        salesforceClientId <- env("salesforceClientId")
        salesforceClientSecret <- env("salesforceClientSecret")
        salesforceUserName <- env("salesforceUserName")
        salesforcePassword <- env("salesforcePassword")
        salesforceToken <- env("salesforceToken")
      } yield
        SalesforceConfig(
          salesforceAuthUrl,
          salesforceClientId,
          salesforceClientSecret,
          salesforceUserName,
          salesforcePassword,
          salesforceToken
        )
    }
  }

  val stageImp: ZLayer[Any, Nothing, StageConfiguration] = ZLayer.succeed {
    new StageConfiguration.Service {
      val config: IO[ConfigurationFailure, StageConfig] = for {
        stage <- env("stage")
      } yield
        StageConfig(
          stage
        )
    }
  }

  val emailSenderImp: ZLayer[Any, Nothing, EmailSenderConfiguration] = ZLayer.succeed {
    new EmailSenderConfiguration.Service {
      val config: IO[ConfigurationFailure, EmailSenderConfig] =
        for {
          emailSqsQueueName <- env("sqsEmailQueueName")
        } yield
          EmailSenderConfig(
            sqsEmailQueueName = emailSqsQueueName
          )
    }
  }

  val cohortStateMachineImpl: ZLayer[Any, ConfigurationFailure, CohortStateMachineConfiguration] = ZLayer.fromEffect {
    env("cohortStateMachineArn") map { arn =>
      new CohortStateMachineConfiguration.Service {
        val config: IO[ConfigurationFailure, CohortStateMachineConfig] =
          ZIO.succeed(CohortStateMachineConfig(stateMachineArn = arn))
      }
    }
  }
}

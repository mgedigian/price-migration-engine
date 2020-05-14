package pricemigrationengine.handlers

import java.time.LocalDate

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import pricemigrationengine.model.CohortTableFilter.ReadyForEstimation
import pricemigrationengine.model._
import pricemigrationengine.services._
import zio.console.Console
import zio.{App, Runtime, ZEnv, ZIO, ZLayer, console}

object EstimationHandler extends App with RequestHandler[Unit, Unit] {

  def estimation(
      newProductPricing: ZuoraPricingData,
      earliestStartDate: LocalDate,
      item: CohortItem
  ): ZIO[Zuora, Failure, EstimationResult] = {
    val result = for {
      subscription <- Zuora.fetchSubscription(item.subscriptionName)
      invoicePreview <- Zuora.fetchInvoicePreview(subscription.accountId)
    } yield EstimationResult(newProductPricing, subscription, invoicePreview, earliestStartDate)
    result.absolve
  }

  val main: ZIO[Logging with Configuration with CohortTable with Zuora, Failure, Unit] =
    for {
      config <- Configuration.config
      newProductPricing <- Zuora.fetchProductCatalogue.map(ZuoraProductCatalogue.productPricingMap)
      cohortItems <- CohortTable.fetch(ReadyForEstimation, config.batchSize)
      results = cohortItems.mapM(
        item =>
          estimation(newProductPricing, config.earliestStartDate, item).tapBoth(
            e => Logging.error(s"Failed to estimate amendment data: $e"),
            result => Logging.info(s"Estimated result: $result")
        )
      )
      _ <- results.foreach(
        result =>
          CohortTable
            .update(result)
            .tapBoth(
              e => Logging.error(s"Failed to update Cohort table: $e"),
              _ => Logging.info(s"Wrote $result to Cohort table")
          )
      )
    } yield ()

  private def env(
      loggingLayer: ZLayer[Any, Nothing, Logging]
  ): ZLayer[Any, Any, Logging with Configuration with CohortTable with Zuora] = {
    val configLayer = EnvConfiguration.impl
    val cohortTableLayer =
      loggingLayer ++ configLayer >>>
        DynamoDBClient.dynamoDB ++ loggingLayer ++ configLayer >>>
        DynamoDBZIOLive.impl ++ loggingLayer ++ configLayer >>>
        CohortTableLive.impl
    val zuoraLayer =
      configLayer ++ loggingLayer >>>
        ZuoraLive.impl
    loggingLayer ++ configLayer ++ cohortTableLayer ++ zuoraLayer
  }

  private val runtime = Runtime.default

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    main
      .provideSomeLayer(
        env(Console.live >>> ConsoleLogging.impl)
      )
      // output any failures in service construction - there's probably a better way to do this
      .foldM(
        e => console.putStrLn(s"Failed: $e") *> ZIO.succeed(1),
        _ => console.putStrLn("Succeeded!") *> ZIO.succeed(0)
      )

  def handleRequest(unused: Unit, context: Context): Unit =
    runtime.unsafeRun(
      main.provideSomeLayer(
        env(LambdaLogging.impl(context))
      )
    )
}
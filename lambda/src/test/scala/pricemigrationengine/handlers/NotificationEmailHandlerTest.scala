package pricemigrationengine.handlers

import java.time._
import java.time.temporal.ChronoUnit

import pricemigrationengine.StubClock
import pricemigrationengine.model.CohortTableFilter.{AmendmentComplete, EstimationComplete}
import pricemigrationengine.model._
import pricemigrationengine.services._
import zio.Exit.Success
import zio.Runtime.default
import zio._
import zio.stream.ZStream

import scala.collection.mutable.ArrayBuffer

class NotificationEmailHandlerTest extends munit.FunSuite {
  val stubLogging = console.Console.live >>> ConsoleLogging.impl
  val expectedSubscriptionName = "Sub-0001"
  val expectedStartDate = LocalDate.of(2020, 1, 1)
  val expectedCurrency = "GBP"
  val expectedBillingFrequency = "Monthly"
  val expectedOldPrice = BigDecimal(11.11)
  val expectedEstimatedNewPrice = BigDecimal(22.22)

  def createStubCohortTable(updatedResultsWrittenToCohortTable:ArrayBuffer[CohortItem], cohortItem: CohortItem) = {
    ZLayer.succeed(
      new CohortTable.Service {
        override def fetch(
          filter: CohortTableFilter,
          beforeDateInclusive: Option[LocalDate]
        ): IO[CohortFetchFailure, ZStream[Any, CohortFetchFailure, CohortItem]] = {
          assertEquals(filter, AmendmentComplete)
          assertEquals(
            beforeDateInclusive,
            Some(
              LocalDate
                .from(StubClock.expectedCurrentTime.plus(30, ChronoUnit.DAYS).atOffset(ZoneOffset.UTC))
            )
          )
          IO.succeed(ZStream(cohortItem))
        }

        override def put(cohortItem: CohortItem): ZIO[Any, CohortUpdateFailure, Unit] = ???

        override def update(result: CohortItem): ZIO[Any, CohortUpdateFailure, Unit] = {
          updatedResultsWrittenToCohortTable.addOne(result)
          IO.succeed(())
        }
      }
    )
  }

  private def stubSFClient() = {
    ZLayer.succeed(
      new SalesforceClient.Service {
        override def getSubscriptionByName(
            subscriptionName: String
        ): IO[SalesforceClientFailure, SalesforceSubscription] = ???

        override def createPriceRise(
            priceRise: SalesforcePriceRise
        ): IO[SalesforceClientFailure, SalesforcePriceRiseCreationResponse] = ???

        override def updatePriceRise(
            priceRiseId: String, priceRise: SalesforcePriceRise
        ): IO[SalesforceClientFailure, Unit] = ???
      }
    )
  }

  test("SalesforcePriceRiseCreateHandler should get records from cohort table and SF") {
    val stubSalesforceClient = stubSFClient()
    val updatedResultsWrittenToCohortTable = ArrayBuffer[CohortItem]()

    val cohortItem = CohortItem(
      subscriptionName = expectedSubscriptionName,
      processingStage = AmendmentComplete,
      startDate = Some(expectedStartDate),
      currency = Some(expectedCurrency),
      oldPrice = Some(expectedOldPrice),
      estimatedNewPrice = Some(expectedEstimatedNewPrice),
      billingPeriod = Some(expectedBillingFrequency)
    )

    val stubCohortTable = createStubCohortTable(updatedResultsWrittenToCohortTable, cohortItem)

    assertEquals(
      default.unsafeRunSync(
        NotificationEmailHandler.main
          .provideLayer(
            stubLogging ++ stubCohortTable ++ StubClock.clock ++ stubSalesforceClient
          )
      ),
      Success(())
    )
  }
}
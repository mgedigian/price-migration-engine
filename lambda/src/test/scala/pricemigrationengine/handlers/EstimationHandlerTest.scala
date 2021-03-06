package pricemigrationengine.handlers

import java.time.LocalDate

import pricemigrationengine.Fixtures
import zio.random.Random
import zio.{Chunk, Runtime, UIO, ULayer, ZLayer}

class EstimationHandlerTest extends munit.FunSuite {

  private val absoluteEarliestStartDate = LocalDate.of(2020, 6, 2)

  private val random: ULayer[Random] = ZLayer.succeed(
    new Random.Service {
      def nextIntBetween(minInclusive: Int, maxExclusive: Int): UIO[Int] = UIO.succeed(1)

      def nextBoolean: UIO[Boolean] = ???
      def nextBytes(length: Int): UIO[Chunk[Byte]] = ???
      def nextDouble: UIO[Double] = ???
      def nextDoubleBetween(minInclusive: Double, maxExclusive: Double): UIO[Double] = ???
      def nextFloat: UIO[Float] = ???
      def nextFloatBetween(minInclusive: Float, maxExclusive: Float): UIO[Float] = ???
      def nextGaussian: UIO[Double] = ???
      def nextInt: UIO[Int] = ???
      def nextIntBounded(n: Int): UIO[Int] = ???
      def nextLong: UIO[Long] = ???
      def nextLongBetween(minInclusive: Long, maxExclusive: Long): UIO[Long] = ???
      def nextLongBounded(n: Long): UIO[Long] = ???
      def nextPrintableChar: UIO[Char] = ???
      def nextString(length: Int): UIO[String] = ???
      def setSeed(seed: Long): UIO[Unit] = ???
      def shuffle[A](list: List[A]): UIO[List[A]] = ???
    }
  )

  private val env = random

  private val runtime = Runtime.default

  test("spreadEarliestStartDate: gives default value for a quarterly subscription") {
    val earliestStartDateCalc = EstimationHandler.spreadEarliestStartDate(
      subscription = Fixtures.subscriptionFromJson("QuarterlyVoucher/Subscription.json"),
      invoicePreview = Fixtures.invoiceListFromJson("QuarterlyVoucher/InvoicePreview.json"),
      absoluteEarliestStartDate
    )
    val earliestStartDate = runtime.unsafeRun(earliestStartDateCalc.provideLayer(env))
    assertEquals(earliestStartDate, LocalDate.of(2020, 6, 2))
  }

  test("spreadEarliestStartDate: gives randomised value for a monthly subscription") {
    val earliestStartDateCalc = EstimationHandler.spreadEarliestStartDate(
      subscription = Fixtures.subscriptionFromJson("Monthly/Subscription.json"),
      invoicePreview = Fixtures.invoiceListFromJson("Monthly/InvoicePreview.json"),
      absoluteEarliestStartDate
    )
    val earliestStartDate = runtime.unsafeRun(earliestStartDateCalc.provideLayer(env))
    assertEquals(earliestStartDate, LocalDate.of(2020, 7, 2))
  }
}

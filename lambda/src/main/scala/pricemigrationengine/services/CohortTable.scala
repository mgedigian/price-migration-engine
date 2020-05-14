package pricemigrationengine.services

import pricemigrationengine.model._
import zio.stream.ZStream
import zio.{IO, ZIO}

case class CohortTableKey(subscriptionNumber: String)

object CohortTable {
  trait Service {
    def fetch(
      filter: CohortTableFilter,
      batchSize: Int
    ): IO[CohortFetchFailure, ZStream[Any, CohortFetchFailure, CohortItem]]

    def update(result: EstimationResult): ZIO[Any, CohortUpdateFailure, Unit]
  }

  def fetch(
    filter: CohortTableFilter,
    batchSize: Int
  ): ZIO[CohortTable, CohortFetchFailure, ZStream[Any, CohortFetchFailure, CohortItem]] =
    ZIO.accessM(_.get.fetch(filter, batchSize))

  def update(result: EstimationResult): ZIO[CohortTable, CohortUpdateFailure, Unit] =
    ZIO.accessM(_.get.update(result))
}
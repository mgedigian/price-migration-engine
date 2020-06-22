package pricemigrationengine.model

import java.time.LocalDate

/**
  * Specification of a cohort.
  *
  * @param cohortName Name that uniquely identifies a cohort, eg. "Vouchers 2020"
  * @param earliestPriceMigrationStartDate Earliest date on which any sub in the cohort can have price migrated.
  *                                        The actual date for any sub will depend on its billing dates.
  * @param importStartDate Date on which to start importing data from the source S3 bucket.
  * @param migrationCompleteDate Date on which the final step in the price migration was complete for every sub in the cohort.
  */
case class CohortSpec(
    cohortName: String,
    earliestPriceMigrationStartDate: LocalDate,
    importStartDate: LocalDate,
    migrationCompleteDate: Option[LocalDate]
)

object CohortSpec {

  def isActive(spec: CohortSpec)(date: LocalDate): Boolean =
    !spec.importStartDate.isAfter(date) && spec.migrationCompleteDate.forall(_.isAfter(date))
}
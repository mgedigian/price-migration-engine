package pricemigrationengine.model

sealed trait CohortTableFilter { val value: String }

object CohortTableFilter {

  // ++++++++++ Normal states ++++++++++

  case object ReadyForEstimation extends CohortTableFilter { override val value: String = "ReadyForEstimation" }

  case object EstimationComplete extends CohortTableFilter { override val value: String = "EstimationComplete" }

  case object SalesforcePriceRiceCreationComplete extends CohortTableFilter {
    override val value: String = "SalesforcePriceRiseCreationComplete"
  }

  case object NotificationSendComplete extends CohortTableFilter {
    override val value: String = "NotificationSendComplete"
  }

  case object NotificationSendDateWrittenToSalesforce extends CohortTableFilter {
    override val value: String = "NotificationSendDateWrittenToSalesforce"
  }

  case object AmendmentComplete extends CohortTableFilter { override val value: String = "AmendmentComplete" }

  case object AmendmentWrittenToSalesforce extends CohortTableFilter {
    override val value: String = "AmendmentWrittenToSalesforce"
  }

  // ++++++++++ Exceptional states ++++++++++

  /*
   * Status of a sub that has been cancelled since the price migration process began,
   * so is ineligible for further processing.
   */
  case object Cancelled extends CohortTableFilter { override val value: String = "Cancelled" }

  case object EstimationFailed extends CohortTableFilter { override val value: String = "EstimationFailed" }

  case object NotificationSendProcessingOrError extends CohortTableFilter {
    override val value: String = "NotificationSendProcessingOrError"
  }

  val all = Set(
    ReadyForEstimation,
    EstimationFailed,
    EstimationComplete,
    SalesforcePriceRiceCreationComplete,
    AmendmentComplete,
    Cancelled,
    NotificationSendProcessingOrError,
    NotificationSendComplete,
    NotificationSendDateWrittenToSalesforce,
    AmendmentWrittenToSalesforce
  )
}

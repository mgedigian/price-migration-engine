package pricemigrationengine

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import zio.Has

package object services {

  type ZuoraConfiguration = Has[ZuoraConfiguration.Service]
  type DynamoDBConfiguration = Has[DynamoDBConfiguration.Service]
  type CohortTableConfiguration = Has[CohortTableConfiguration.Service]
  type SalesforceConfiguration = Has[SalesforceConfiguration.Service]
  type StageConfiguration = Has[StageConfiguration.Service]
  type EmailSenderConfiguration = Has[EmailSenderConfiguration.Service]
  type CohortStateMachineConfiguration = Has[CohortStateMachineConfiguration.Service]

  type CohortStateMachine = Has[CohortStateMachine.Service]
  type CohortSpecTable = Has[CohortSpecTable.Service]
  type CohortTable = Has[CohortTable.Service]
  type CohortTableDdl = Has[CohortTableDdl.Service]
  type Zuora = Has[Zuora.Service]
  type Logging = Has[Logging.Service]
  type DynamoDBClient = Has[AmazonDynamoDB]
  type DynamoDBZIO = Has[DynamoDBZIO.Service]
  type SalesforceClient = Has[SalesforceClient.Service]
  type S3 = Has[S3.Service]
  type EmailSender = Has[EmailSender.Service]
}

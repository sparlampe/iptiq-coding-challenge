package io.pusteblume.loadbalancer.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object ProviderJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val providerInfoJson: RootJsonFormat[ProviderRegistrationInfo] = jsonFormat3(ProviderRegistrationInfo)
}

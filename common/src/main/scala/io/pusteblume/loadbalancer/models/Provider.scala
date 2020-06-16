package io.pusteblume.loadbalancer.models

case class ProviderRegistrationInfo(id: String, port: Int, maxCapacity: Int)
case class Provider(id: String, ip: String, port: Int, maxCapacity: Int)
case class ProviderState(providerInfo: Provider)

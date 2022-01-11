package com.goyeau.kubernetes.client.api

import cats.effect.Async
import com.goyeau.kubernetes.client.KubeConfig
import com.goyeau.kubernetes.client.operation._
import com.goyeau.kubernetes.client.util.CachedExecToken
import io.circe._
import io.k8s.api.networking.v1.{Ingress, IngressList}
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.implicits._

private[client] class IngressessApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig,
    val cachedExecToken: Option[CachedExecToken[F]]
)(implicit
    val F: Async[F],
    val listDecoder: Decoder[IngressList],
    val resourceDecoder: Decoder[Ingress],
    encoder: Encoder[Ingress]
) extends Listable[F, IngressList]
    with Watchable[F, Ingress] {
  val resourceUri: Uri = uri"/apis" / "networking.k8s.io" / "v1" / "ingresses"

  def namespace(namespace: String): NamespacedIngressesApi[F] =
    new NamespacedIngressesApi(httpClient, config, cachedExecToken, namespace)
}

private[client] class NamespacedIngressesApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig,
    val cachedExecToken: Option[CachedExecToken[F]],
    namespace: String
)(implicit
    val F: Async[F],
    val resourceEncoder: Encoder[Ingress],
    val resourceDecoder: Decoder[Ingress],
    val listDecoder: Decoder[IngressList]
) extends Creatable[F, Ingress]
    with Replaceable[F, Ingress]
    with Gettable[F, Ingress]
    with Listable[F, IngressList]
    with Deletable[F]
    with GroupDeletable[F]
    with Watchable[F, Ingress] {
  val resourceUri: Uri = uri"/apis" / "networking.k8s.io" / "v1" / "namespaces" / namespace / "ingresses"
}

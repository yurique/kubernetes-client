package com.goyeau.kubernetes.client.api

import cats.effect.Async
import com.goyeau.kubernetes.client.KubeConfig
import com.goyeau.kubernetes.client.operation.*
import com.goyeau.kubernetes.client.util.CachedExecToken
import io.circe.*
import io.k8s.api.core.v1.PersistentVolumeClaim
import io.k8s.api.core.v1.PersistentVolumeClaimList
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.implicits.*

private[client] class PersistentVolumeClaimsApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig,
    val cachedExecToken: Option[CachedExecToken[F]]
)(implicit
    val F: Async[F],
    val listDecoder: Decoder[PersistentVolumeClaimList],
    val resourceDecoder: Decoder[PersistentVolumeClaim],
    encoder: Encoder[PersistentVolumeClaim]
) extends Listable[F, PersistentVolumeClaimList]
    with Watchable[F, PersistentVolumeClaim] {
  val resourceUri: Uri = uri"/apis" / "networking.k8s.io" / "v1" / "ingresses"

  def namespace(namespace: String): NamespacedPersistentVolumeClaimsApi[F] =
    new NamespacedPersistentVolumeClaimsApi(httpClient, config, cachedExecToken, namespace)
}

private[client] class NamespacedPersistentVolumeClaimsApi[F[_]](
    val httpClient: Client[F],
    val config: KubeConfig,
    val cachedExecToken: Option[CachedExecToken[F]],
    namespace: String
)(implicit
    val F: Async[F],
    val resourceEncoder: Encoder[PersistentVolumeClaim],
    val resourceDecoder: Decoder[PersistentVolumeClaim],
    val listDecoder: Decoder[PersistentVolumeClaimList]
) extends Creatable[F, PersistentVolumeClaim]
    with Replaceable[F, PersistentVolumeClaim]
    with Gettable[F, PersistentVolumeClaim]
    with Listable[F, PersistentVolumeClaimList]
    with Deletable[F]
    with GroupDeletable[F]
    with Watchable[F, PersistentVolumeClaim] {
  val resourceUri: Uri = uri"/apis" / "networking.k8s.io" / "v1" / "namespaces" / namespace / "ingresses"
}

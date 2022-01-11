package com.goyeau.kubernetes.client

import cats.effect._
import com.goyeau.kubernetes.client.api._
import com.goyeau.kubernetes.client.crd.{CrdContext, CustomResource, CustomResourceList}
import com.goyeau.kubernetes.client.util.{CachedExecToken, SslContexts}
import io.circe.{Decoder, Encoder}
import org.http4s.client.Client
import org.http4s.jdkhttpclient.{JdkHttpClient, JdkWSClient, WSClient}
import org.typelevel.log4cats.Logger

import java.net.http.HttpClient

class KubernetesClient[F[_]: Async: Logger](
    httpClient: Client[F],
    wsClient: WSClient[F],
    config: KubeConfig,
    cachedExecToken: Option[CachedExecToken[F]]
) {
  lazy val namespaces = new NamespacesApi(httpClient, config, cachedExecToken)
  lazy val pods = new PodsApi(
    httpClient,
    wsClient,
    config,
    cachedExecToken
  )
  lazy val jobs                      = new JobsApi(httpClient, config, cachedExecToken)
  lazy val cronJobs                  = new CronJobsApi(httpClient, config, cachedExecToken)
  lazy val deployments               = new DeploymentsApi(httpClient, config, cachedExecToken)
  lazy val statefulSets              = new StatefulSetsApi(httpClient, config, cachedExecToken)
  lazy val replicaSets               = new ReplicaSetsApi(httpClient, config, cachedExecToken)
  lazy val services                  = new ServicesApi(httpClient, config, cachedExecToken)
  lazy val serviceAccounts           = new ServiceAccountsApi(httpClient, config, cachedExecToken)
  lazy val configMaps                = new ConfigMapsApi(httpClient, config, cachedExecToken)
  lazy val secrets                   = new SecretsApi(httpClient, config, cachedExecToken)
  lazy val horizontalPodAutoscalers  = new HorizontalPodAutoscalersApi(httpClient, config, cachedExecToken)
  lazy val podDisruptionBudgets      = new PodDisruptionBudgetsApi(httpClient, config, cachedExecToken)
  lazy val customResourceDefinitions = new CustomResourceDefinitionsApi(httpClient, config, cachedExecToken)
  lazy val ingresses                 = new IngressessApi(httpClient, config, cachedExecToken)
  lazy val leases                    = new LeasesApi(httpClient, config, cachedExecToken)

  def customResources[A: Encoder: Decoder, B: Encoder: Decoder](context: CrdContext)(implicit
      listDecoder: Decoder[CustomResourceList[A, B]],
      encoder: Encoder[CustomResource[A, B]],
      decoder: Decoder[CustomResource[A, B]]
  ) = new CustomResourcesApi[F, A, B](httpClient, config, cachedExecToken, context)
}

object KubernetesClient {
  def apply[F[_]: Async: Logger](config: KubeConfig): Resource[F, KubernetesClient[F]] =
    for {
      client <- Resource.eval {
        Sync[F].delay(HttpClient.newBuilder().sslContext(SslContexts.fromConfig(config)).build())
      }
      httpClient <- JdkHttpClient[F](client)
      wsClient   <- JdkWSClient[F](client)
      cachedExecToken <- config.authInfoExec match {
        case None => Resource.pure[F, Option[CachedExecToken[F]]](Option.empty)
        case Some(authInfoExec) =>
          Resource
            .eval(
              CachedExecToken[F](authInfoExec)
            )
            .map(Some(_))
      }
    } yield new KubernetesClient(
      httpClient,
      wsClient,
      config,
      cachedExecToken
    )

  def apply[F[_]: Async: Logger](config: F[KubeConfig]): Resource[F, KubernetesClient[F]] =
    Resource.eval(config).flatMap(apply(_))
}

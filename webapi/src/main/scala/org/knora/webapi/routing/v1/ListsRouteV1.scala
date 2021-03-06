/*
 * Copyright © 2015-2021 the contributors (see Contributors.md).
 *
 * This file is part of Knora.
 *
 * Knora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Knora.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.knora.webapi.routing.v1

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.knora.webapi.exceptions.BadRequestException
import org.knora.webapi.feature.FeatureFactoryConfig
import org.knora.webapi.messages.StringFormatter
import org.knora.webapi.messages.v1.responder.listmessages._
import org.knora.webapi.routing.{Authenticator, KnoraRoute, KnoraRouteData, RouteUtilV1}

/**
  * Provides API routes that deal with lists.
  */
class ListsRouteV1(routeData: KnoraRouteData) extends KnoraRoute(routeData) with Authenticator {

  /**
    * Returns the route.
    */
  override def makeRoute(featureFactoryConfig: FeatureFactoryConfig): Route = {

    val stringFormatter = StringFormatter.getGeneralInstance

    path("v1" / "hlists" / Segment) { iri =>
      get { requestContext =>
        val requestMessageFuture = for {
          userProfile <- getUserADM(
            requestContext = requestContext,
            featureFactoryConfig = featureFactoryConfig
          ).map(_.asUserProfileV1)
          listIri = stringFormatter.validateAndEscapeIri(iri,
                                                         throw BadRequestException(s"Invalid param list IRI: $iri"))

          requestMessage = requestContext.request.uri.query().get("reqtype") match {
            case Some("node")  => NodePathGetRequestV1(listIri, userProfile)
            case Some(reqtype) => throw BadRequestException(s"Invalid reqtype: $reqtype")
            case None          => HListGetRequestV1(listIri, userProfile)
          }
        } yield requestMessage

        RouteUtilV1.runJsonRouteWithFuture(
          requestMessageFuture,
          requestContext,
          settings,
          responderManager,
          log
        )
      }
    } ~
      path("v1" / "selections" / Segment) { iri =>
        get { requestContext =>
          val requestMessageFuture = for {
            userProfile <- getUserADM(
              requestContext = requestContext,
              featureFactoryConfig = featureFactoryConfig
            ).map(_.asUserProfileV1)
            selIri = stringFormatter.validateAndEscapeIri(iri,
                                                          throw BadRequestException(s"Invalid param list IRI: $iri"))

            requestMessage = requestContext.request.uri.query().get("reqtype") match {
              case Some("node")  => NodePathGetRequestV1(selIri, userProfile)
              case Some(reqtype) => throw BadRequestException(s"Invalid reqtype: $reqtype")
              case None          => SelectionGetRequestV1(selIri, userProfile)
            }
          } yield requestMessage

          RouteUtilV1.runJsonRouteWithFuture(
            requestMessageFuture,
            requestContext,
            settings,
            responderManager,
            log
          )
        }
      }
  }
}

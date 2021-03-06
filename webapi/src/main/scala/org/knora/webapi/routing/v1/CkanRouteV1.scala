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
import org.knora.webapi.feature.FeatureFactoryConfig
import org.knora.webapi.messages.v1.responder.ckanmessages.CkanRequestV1
import org.knora.webapi.routing.{Authenticator, KnoraRoute, KnoraRouteData, RouteUtilV1}

/**
  * A route used to serve data to CKAN. It is used be the Ckan instance running under http://data.humanities.ch.
  */
class CkanRouteV1(routeData: KnoraRouteData) extends KnoraRoute(routeData) with Authenticator {

  /**
    * Returns the route.
    */
  override def makeRoute(featureFactoryConfig: FeatureFactoryConfig): Route = {

    path("v1" / "ckan") {
      get { requestContext =>
        val requestMessage = for {
          userProfile <- getUserADM(
            requestContext = requestContext,
            featureFactoryConfig = featureFactoryConfig
          )
          params = requestContext.request.uri.query().toMap
          project: Option[Seq[String]] = params.get("project").map(_.split(","))
          limit: Option[Int] = params.get("limit").map(_.toInt)
          info: Boolean = params.getOrElse("info", false) == true
        } yield
          CkanRequestV1(
            projects = project,
            limit = limit,
            info = info,
            featureFactoryConfig = featureFactoryConfig,
            userProfile = userProfile
          )

        RouteUtilV1.runJsonRouteWithFuture(
          requestMessage,
          requestContext,
          settings,
          responderManager,
          log
        )
      }
    }
  }
}

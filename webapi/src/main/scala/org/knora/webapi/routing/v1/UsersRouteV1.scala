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

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.apache.commons.validator.routines.UrlValidator
import org.knora.webapi.exceptions.BadRequestException
import org.knora.webapi.feature.FeatureFactoryConfig
import org.knora.webapi.messages.v1.responder.usermessages._
import org.knora.webapi.routing.{Authenticator, KnoraRoute, KnoraRouteData, RouteUtilV1}

/**
  * Provides a spray-routing function for API routes that deal with lists.
  */
class UsersRouteV1(routeData: KnoraRouteData) extends KnoraRoute(routeData) with Authenticator {

  private val schemes = Array("http", "https")
  private val urlValidator = new UrlValidator(schemes)

  /**
    * Returns the route.
    */
  override def makeRoute(featureFactoryConfig: FeatureFactoryConfig): Route = {

    path("v1" / "users") {
      get {
        /* return all users */
        requestContext =>
          val requestMessage = for {
            userProfile <- getUserADM(
              requestContext = requestContext,
              featureFactoryConfig = featureFactoryConfig
            ).map(_.asUserProfileV1)
          } yield UsersGetRequestV1(userProfile)

          RouteUtilV1.runJsonRouteWithFuture(
            requestMessage,
            requestContext,
            settings,
            responderManager,
            log
          )
      }
    } ~
      path("v1" / "users" / Segment) { value =>
        get {
          /* return a single user identified by iri or email */
          parameters("identifier" ? "iri") { (identifier: String) => requestContext =>
            /* check if email or iri was supplied */
            val requestMessage = if (identifier == "email") {
              for {
                userProfile <- getUserADM(
                  requestContext = requestContext,
                  featureFactoryConfig = featureFactoryConfig
                ).map(_.asUserProfileV1)
              } yield
                UserProfileByEmailGetRequestV1(
                  email = value,
                  userProfileType = UserProfileTypeV1.RESTRICTED,
                  featureFactoryConfig = featureFactoryConfig,
                  userProfile = userProfile
                )
            } else {
              for {
                userProfile <- getUserADM(
                  requestContext = requestContext,
                  featureFactoryConfig = featureFactoryConfig
                ).map(_.asUserProfileV1)
                userIri = stringFormatter.validateAndEscapeIri(value,
                                                               throw BadRequestException(s"Invalid user IRI $value"))
              } yield
                UserProfileByIRIGetRequestV1(
                  userIri = userIri,
                  userProfileType = UserProfileTypeV1.RESTRICTED,
                  featureFactoryConfig = featureFactoryConfig,
                  userProfile = userProfile
                )
            }

            RouteUtilV1.runJsonRouteWithFuture(
              requestMessage,
              requestContext,
              settings,
              responderManager,
              log
          )
          }
        }
      } ~
      path("v1" / "users" / "projects" / Segment) { userIri =>
        get {
          /* get user's project memberships */
          requestContext =>
            val requestMessage = for {
              userProfile <- getUserADM(
                requestContext = requestContext,
                featureFactoryConfig = featureFactoryConfig
              ).map(_.asUserProfileV1)
              checkedUserIri = stringFormatter.validateAndEscapeIri(
                userIri,
                throw BadRequestException(s"Invalid user IRI $userIri"))
            } yield
              UserProjectMembershipsGetRequestV1(
                userIri = checkedUserIri,
                userProfileV1 = userProfile,
                apiRequestID = UUID.randomUUID()
              )

            RouteUtilV1.runJsonRouteWithFuture(
              requestMessage,
              requestContext,
              settings,
              responderManager,
              log
            )
        }
      } ~
      path("v1" / "users" / "projects-admin" / Segment) { userIri =>
        get {
          /* get user's project admin memberships */
          requestContext =>
            val requestMessage = for {
              userProfile <- getUserADM(
                requestContext = requestContext,
                featureFactoryConfig = featureFactoryConfig
              ).map(_.asUserProfileV1)
              checkedUserIri = stringFormatter.validateAndEscapeIri(
                userIri,
                throw BadRequestException(s"Invalid user IRI $userIri"))
            } yield
              UserProjectAdminMembershipsGetRequestV1(
                userIri = checkedUserIri,
                userProfileV1 = userProfile,
                apiRequestID = UUID.randomUUID()
              )

            RouteUtilV1.runJsonRouteWithFuture(
              requestMessage,
              requestContext,
              settings,
              responderManager,
              log
            )
        }
      } ~
      path("v1" / "users" / "groups" / Segment) { userIri =>
        get {
          /* get user's group memberships */
          requestContext =>
            val requestMessage = for {
              userProfile <- getUserADM(
                requestContext = requestContext,
                featureFactoryConfig = featureFactoryConfig
              ).map(_.asUserProfileV1)
              checkedUserIri = stringFormatter.validateAndEscapeIri(
                userIri,
                throw BadRequestException(s"Invalid user IRI $userIri"))
            } yield
              UserGroupMembershipsGetRequestV1(
                userIri = checkedUserIri,
                userProfileV1 = userProfile,
                apiRequestID = UUID.randomUUID()
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

/*
 * Copyright © 2015-2018 the contributors (see Contributors.md).
 *
 *  This file is part of Knora.
 *
 *  Knora is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Knora is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Knora.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.knora.webapi.messages.v2.routing.authenticationmessages

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.knora.webapi.IRI
import org.knora.webapi.exceptions.BadRequestException
import org.knora.webapi.messages.admin.responder.usersmessages.UserIdentifierADM
import spray.json._

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// API requests

/**
  * Represents an API request payload that asks the Knora API server to authenticate the user and create a JWT token.
  * Only one of IRI, username, or email as identifier is allowed.
  *
  * @param iri      the user's IRI.
  * @param email    the user's email.
  * @param username the user's username.
  * @param password the user's password.
  */
case class LoginApiRequestPayloadV2(iri: Option[IRI] = None,
                                    email: Option[String] = None,
                                    username: Option[String] = None,
                                    password: String) {

  val identifyingParameterCount: Int = List(
    iri,
    email,
    username
  ).flatten.size

  // something needs to be set
  if (identifyingParameterCount == 0) throw BadRequestException("Empty user identifier is not allowed.")

  if (identifyingParameterCount > 1) throw BadRequestException("Only one option allowed for user identifier.")

  // Password needs to be supplied
  if (password.isEmpty) throw BadRequestException("Password needs to be supplied.")
}

/**
  * An abstract knora credentials class.
  */
sealed abstract class KnoraCredentialsV2()

/**
  * Represents id/password credentials that a user can supply within the authorization header or as URL parameters.
  *
  * @param identifier the supplied id.
  * @param password   the supplied password.
  */
case class KnoraPasswordCredentialsV2(identifier: UserIdentifierADM, password: String) extends KnoraCredentialsV2

/**
  * Represents token credentials that a user can supply withing the authorization header or as URL parameters.
  *
  * @param token the supplied json web token.
  */
case class KnoraTokenCredentialsV2(token: String) extends KnoraCredentialsV2

/**
  * Represents session credentials that a user can supply within the cookie header.
  *
  * @param token the supplied session token.
  */
case class KnoraSessionCredentialsV2(token: String) extends KnoraCredentialsV2

/**
  * Represents a response Knora returns when communicating with the 'v2/authentication' route during the 'login' operation.
  *
  * @param token is the returned json web token.
  */
case class LoginResponse(token: String)

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// JSON formatting

/**
  * A spray-json protocol for generating Knora API v2 JSON for property values.
  */
trait AuthenticationV2JsonProtocol extends DefaultJsonProtocol with NullOptions with SprayJsonSupport {
  implicit val loginApiRequestPayloadV2Format: RootJsonFormat[LoginApiRequestPayloadV2] =
    jsonFormat(LoginApiRequestPayloadV2, "iri", "email", "username", "password")
  implicit val SessionResponseFormat: RootJsonFormat[LoginResponse] = jsonFormat1(LoginResponse.apply)
}

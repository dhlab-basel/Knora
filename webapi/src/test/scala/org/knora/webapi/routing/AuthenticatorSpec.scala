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

package org.knora.webapi.routing

import akka.testkit.ImplicitSender
import akka.util.Timeout
import org.knora.webapi._
import org.knora.webapi.exceptions.{BadCredentialsException, BadRequestException}
import org.knora.webapi.messages.StringFormatter
import org.knora.webapi.messages.admin.responder.usersmessages.{UserADM, UserIdentifierADM}
import org.knora.webapi.messages.v2.routing.authenticationmessages.{KnoraPasswordCredentialsV2, KnoraTokenCredentialsV2}
import org.knora.webapi.routing.Authenticator.AUTHENTICATION_INVALIDATION_CACHE_NAME
import org.knora.webapi.sharedtestdata.SharedTestDataADM
import org.knora.webapi.util.cache.CacheUtil
import org.scalatest.PrivateMethodTester

import scala.concurrent.Future

object AuthenticatorSpec {
  private val rootUser = SharedTestDataADM.rootUser
  private val rootUserEmail = rootUser.email
  private val rootUserPassword = "test"
}

class AuthenticatorSpec extends CoreSpec("AuthenticationTestSystem") with ImplicitSender with PrivateMethodTester {

  implicit val timeout: Timeout = settings.defaultTimeout

  implicit val stringFormatter: StringFormatter = StringFormatter.getGeneralInstance

  private val getUserByIdentifier = PrivateMethod[Future[UserADM]]('getUserByIdentifier)
  private val authenticateCredentialsV2 = PrivateMethod[Future[Boolean]]('authenticateCredentialsV2)

  "During Authentication" when {
    "called, the 'getUserADMByEmail' method " should {
      "succeed with the correct 'email' " in {
        val resF = Authenticator invokePrivate getUserByIdentifier(
          UserIdentifierADM(maybeEmail = Some(AuthenticatorSpec.rootUserEmail)),
          defaultFeatureFactoryConfig,
          system,
          responderManager,
          timeout,
          executionContext)
        resF map { res =>
          assert(res == AuthenticatorSpec.rootUser)
        }
      }

      "fail with the wrong 'email' " in {
        val resF = Authenticator invokePrivate getUserByIdentifier(
          UserIdentifierADM(maybeEmail = Some("wronguser@example.com")),
          defaultFeatureFactoryConfig,
          system,
          responderManager,
          timeout,
          executionContext)
        resF map { res =>
          assertThrows(BadCredentialsException)
        }
      }

      "fail when not providing anything " in {
        an[BadRequestException] should be thrownBy {
          Authenticator invokePrivate getUserByIdentifier(UserIdentifierADM(),
                                                          defaultFeatureFactoryConfig,
                                                          system,
                                                          responderManager,
                                                          timeout,
                                                          executionContext)
        }
      }
    }

    "called, the 'authenticateCredentialsV2' method" should {
      "succeed with correct email/password" in {
        val correctPasswordCreds =
          KnoraPasswordCredentialsV2(UserIdentifierADM(maybeEmail = Some(AuthenticatorSpec.rootUserEmail)),
                                     AuthenticatorSpec.rootUserPassword)
        val resF = Authenticator invokePrivate authenticateCredentialsV2(Some(correctPasswordCreds),
                                                                         defaultFeatureFactoryConfig,
                                                                         system,
                                                                         responderManager,
                                                                         executionContext)
        resF map { res =>
          assert(res)
        }
      }
      "fail with unknown email" in {
        val wrongPasswordCreds =
          KnoraPasswordCredentialsV2(UserIdentifierADM(maybeEmail = Some("wrongemail@example.com")), "wrongpassword")
        val resF = Authenticator invokePrivate authenticateCredentialsV2(Some(wrongPasswordCreds),
                                                                         defaultFeatureFactoryConfig,
                                                                         system,
                                                                         responderManager,
                                                                         executionContext)
        resF map { res =>
          assertThrows(BadCredentialsException)
        }
      }
      "fail with wrong password" in {
        val wrongPasswordCreds =
          KnoraPasswordCredentialsV2(UserIdentifierADM(maybeEmail = Some(AuthenticatorSpec.rootUserEmail)),
                                     "wrongpassword")
        val resF = Authenticator invokePrivate authenticateCredentialsV2(Some(wrongPasswordCreds),
                                                                         defaultFeatureFactoryConfig,
                                                                         system,
                                                                         responderManager,
                                                                         executionContext)
        resF map { res =>
          assertThrows(BadCredentialsException)
        }
      }
      "succeed with correct token" in {
        val token = JWTHelper.createToken("myuseriri", settings.jwtSecretKey, settings.jwtLongevity)
        val tokenCreds = KnoraTokenCredentialsV2(token)
        val resF = Authenticator invokePrivate authenticateCredentialsV2(Some(tokenCreds),
                                                                         defaultFeatureFactoryConfig,
                                                                         system,
                                                                         responderManager,
                                                                         executionContext)
        resF map { res =>
          assert(res)
        }
      }
      "fail with invalidated token" in {
        val token = JWTHelper.createToken("myuseriri", settings.jwtSecretKey, settings.jwtLongevity)
        val tokenCreds = KnoraTokenCredentialsV2(token)
        CacheUtil.put(AUTHENTICATION_INVALIDATION_CACHE_NAME, tokenCreds.token, tokenCreds.token)
        val resF = Authenticator invokePrivate authenticateCredentialsV2(Some(tokenCreds),
                                                                         defaultFeatureFactoryConfig,
                                                                         system,
                                                                         responderManager,
                                                                         executionContext)
        resF map { res =>
          assertThrows(BadCredentialsException)
        }
      }
      "fail with wrong token" in {
        val tokenCreds = KnoraTokenCredentialsV2("123456")
        val resF = Authenticator invokePrivate authenticateCredentialsV2(Some(tokenCreds),
                                                                         defaultFeatureFactoryConfig,
                                                                         system,
                                                                         responderManager,
                                                                         executionContext)
        resF map { res =>
          assertThrows(BadCredentialsException)
        }
      }

    }
  }
}

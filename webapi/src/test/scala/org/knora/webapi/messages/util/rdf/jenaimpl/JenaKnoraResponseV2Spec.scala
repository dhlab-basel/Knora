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

package org.knora.webapi.util.rdf.jenaimpl

import org.knora.webapi.feature.{FeatureToggle, ToggleStateOn}
import org.knora.webapi.util.rdf.KnoraResponseV2Spec

/**
  * Tests [[org.knora.webapi.messages.v2.responder.KnoraResponseV2]] with the Jena API.
  */
class JenaKnoraResponseV2Spec extends KnoraResponseV2Spec(FeatureToggle("jena-rdf-library", ToggleStateOn(1)))

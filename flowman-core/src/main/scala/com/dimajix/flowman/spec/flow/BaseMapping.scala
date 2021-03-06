/*
 * Copyright 2018 Kaya Kupferschmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimajix.flowman.spec.flow

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.spark.storage.StorageLevel

import com.dimajix.flowman.execution.Context
import com.dimajix.flowman.spec.MappingIdentifier
import com.dimajix.flowman.types.StructType

/**
  * Common base implementation for the MappingType interface
  */
abstract class BaseMapping extends Mapping {
    @JsonProperty("broadcast") private[spec] var _broadcast:String = "false"
    @JsonProperty("checkpoint") private[spec] var _checkpoint:String = "false"
    @JsonProperty("cache") private[spec] var _cache:String = "NONE"

    def broadcast(implicit context: Context) : Boolean = context.evaluate(_broadcast).toBoolean
    def checkpoint(implicit context: Context) : Boolean = context.evaluate(_checkpoint).toBoolean
    def cache(implicit context: Context) : StorageLevel = StorageLevel.fromString(context.evaluate(_cache))

    /**
      * Returns the schema as produced by this mapping, relative to the given input schema
      * @param context
      * @param input
      * @return
      */
    override def describe(context:Context, input:Map[MappingIdentifier,StructType]) : StructType = {
        require(context != null)
        require(input != null)

        throw new UnsupportedOperationException(s"Schema inference not supported for mapping $name of type $category")
    }
}

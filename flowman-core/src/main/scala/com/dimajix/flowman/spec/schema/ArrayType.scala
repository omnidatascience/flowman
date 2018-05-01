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

package com.dimajix.flowman.spec.schema

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.spark.sql.types.DataType


case class ArrayType @JsonCreator(mode = JsonCreator.Mode.DISABLED)(
        @JsonProperty(value="elementType") elementType:FieldType,
        @JsonProperty(value="containsNull") containsNull:Boolean = true
    ) extends ContainerType {

    @JsonCreator
    def this() = { this(null, true) }

    override def sparkType : DataType = {
        org.apache.spark.sql.types.ArrayType(elementType.sparkType, containsNull)
    }

    override def parse(value:String, granularity:String) : Any = ???
    override def interpolate(value: FieldValue, granularity:String) : Iterable[Any] = ???
}

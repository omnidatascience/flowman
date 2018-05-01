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

package com.dimajix.flowman.spec

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.dimajix.flowman.spec.schema.ArrayValue
import com.dimajix.flowman.spec.schema.FieldValue
import com.dimajix.flowman.spec.schema.RangeValue
import com.dimajix.flowman.spec.schema.SingleValue


class ValueOrRangeTest extends FlatSpec with Matchers {
    "A FieldValue" should "be readable from a single value" in {
        val spec =
            """
              |"someValue"
            """.stripMargin

        val result = ObjectMapper.parse[FieldValue](spec)
        result shouldBe a [SingleValue]
        result.asInstanceOf[SingleValue].value should be("someValue")
    }

    it should "be readable from an array value" in {
        val spec =
            """
              |["someValue", "secondValue"]
            """.stripMargin

        val result = ObjectMapper.parse[FieldValue](spec)
        result shouldBe a [ArrayValue]
        result.asInstanceOf[ArrayValue].values should be (Array("someValue","secondValue"))
    }

    it should "be readable from an unquoted array value" in {
        val spec =
            """
              |[someValue, secondValue]
            """.stripMargin

        val result = ObjectMapper.parse[FieldValue](spec)
        result shouldBe a [ArrayValue]
        result.asInstanceOf[ArrayValue].values should be (Array("someValue","secondValue"))
    }

    it should "be readable from a range definition value" in {
        val spec =
            """
              |start: "someValue"
              |end: "secondValue"
            """.stripMargin

        val result = ObjectMapper.parse[FieldValue](spec)
        result shouldBe a [RangeValue]
        result.asInstanceOf[RangeValue].start should be ("someValue")
        result.asInstanceOf[RangeValue].end should be ("secondValue")
    }

    it should "be readable from an unquoted range definition value" in {
        val spec =
            """
              |start: someValue
              |end: secondValue
            """.stripMargin

        val result = ObjectMapper.parse[FieldValue](spec)
        result shouldBe a [RangeValue]
        result.asInstanceOf[RangeValue].start should be ("someValue")
        result.asInstanceOf[RangeValue].end should be ("secondValue")
    }
}

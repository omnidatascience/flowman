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

package com.dimajix.flowman.types

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.FlatSpec
import org.scalatest.Matchers


class IntegerTypeTest extends FlatSpec with Matchers {
    lazy val mapper = {
        val mapper = new ObjectMapper(new YAMLFactory())
        mapper.registerModule(DefaultScalaModule)
        mapper
    }

    "A IntegerType" should "parse strings" in {
        IntegerType.parse("12") should be (12)
    }

    it should "support interpolation of SingleValues" in {
        IntegerType.interpolate(SingleValue("12"), null).head should be (12)
    }

    it should "support interpolation of SingleValues with granularity" in {
        IntegerType.interpolate(SingleValue("12"), "3").head should be (12)
        IntegerType.interpolate(SingleValue("13"), "3").head should be (12)
        IntegerType.interpolate(SingleValue("14"), "3").head should be (12)
        IntegerType.interpolate(SingleValue("15"), "3").head should be (15)
    }

    it should "support interpolation of ArrayValues" in {
        val result = IntegerType.interpolate(ArrayValue(Array("12","27")), null)
        result.head should be (12)
        result.drop(1).head should be (27)
    }

    it should "support interpolation of ArrayValues with granularity" in {
        val result = IntegerType.interpolate(ArrayValue(Array("12","16","17")), "3").toSeq
        result(0) should be (12)
        result(1) should be (15)
        result(2) should be (15)
    }

    it should "support interpolation of Ranges" in {
        val result = IntegerType.interpolate(RangeValue("12","16"), null)
        result.toSeq should be (Seq(12,13,14,15))
    }

    it should "support interpolation of Ranges with granularity" in {
        val result = IntegerType.interpolate(RangeValue("12","16"), "2")
        result.toSeq should be (Seq(12,14))

        val result2 = IntegerType.interpolate(RangeValue("13","17"), "2")
        result2.toSeq should be (Seq(12,14))
    }

    it should "support interpolation of Ranges with steps" in {
        val result = IntegerType.interpolate(RangeValue("12","16", "2"), null)
        result.toSeq should be (Seq(12,14))

        val result2 = IntegerType.interpolate(RangeValue("13","17", "2"), null)
        result2.toSeq should be (Seq(13,15))
    }

    it should "support interpolation of Ranges with steps and granularity" in {
        val result = IntegerType.interpolate(RangeValue("12","16", "2"), "2")
        result.toSeq should be (Seq(12,14))

        val result1 = IntegerType.interpolate(RangeValue("13","17", "2"), "2")
        result1.toSeq should be (Seq(12,14))

        val result2 = IntegerType.interpolate(RangeValue("13","17", "3"), "2")
        result2.toSeq should be (Seq(12,16))
    }

    "A int type" should "be deserializable" in {
        val spec = "int"

        val result = mapper.readValue(spec, classOf[FieldType])
        result should be (IntegerType)
        result.sparkType should be (org.apache.spark.sql.types.IntegerType)
    }
    it should "be deserializable in long form" in {
        val spec = "integer"

        val result = mapper.readValue(spec, classOf[FieldType])
        result should be (IntegerType)
        result.sparkType should be (org.apache.spark.sql.types.IntegerType)
    }

    it should "provide the correct SQL type" in {
        val ftype = IntegerType
        ftype.sqlType should be ("integer")
    }
}

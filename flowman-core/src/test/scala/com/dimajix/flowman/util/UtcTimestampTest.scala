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

package com.dimajix.flowman.util

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import org.scalatest.FlatSpec
import org.scalatest.Matchers


class UtcTimestampTest extends FlatSpec with Matchers {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.S]").withZone(ZoneOffset.UTC)
    def parseDateTime(value:String) = new Timestamp(LocalDateTime.parse(value, formatter).toEpochSecond(ZoneOffset.UTC) * 1000l)

    "A UtcTimestamp" should "parse strings" in {
        UtcTimestamp.parse("2017-12-01 12:21:20").asInstanceOf[Timestamp] should be (parseDateTime("2017-12-01 12:21:20"))
        UtcTimestamp.parse("2017-12-01 12:21:20").asInstanceOf[Timestamp] should be (new Timestamp(1512130880*1000l))
        UtcTimestamp.parse("2017-12-01 12:21:20.0").asInstanceOf[Timestamp] should be (parseDateTime("2017-12-01 12:21:20"))
        UtcTimestamp.parse("2017-12-01 12:21:20.0").asInstanceOf[Timestamp] should be (new Timestamp(1512130880*1000l))
    }

    it should "be serialized as string identically" in {
        UtcTimestamp.parse("2017-12-01 12:21:20").toString should be ("2017-12-01 12:21:20.0")
    }

}
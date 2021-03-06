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

package com.dimajix.flowman.spec.state

import java.nio.file.Files
import java.nio.file.Path

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.dimajix.flowman.execution.Session
import com.dimajix.flowman.spec.Namespace
import com.dimajix.flowman.spec.ObjectMapper
import com.dimajix.flowman.spec.connection.JdbcConnection
import com.dimajix.flowman.spec.task.Job
import com.dimajix.flowman.types.StringType


class JdbcStateStoreTest extends FlatSpec with Matchers with BeforeAndAfter {
    var tempDir:Path = _

    before {
        tempDir = Files.createTempDirectory("jdbc_logged_runner_test")
    }
    after {
        tempDir.toFile.listFiles().foreach(_.delete())
        tempDir.toFile.delete()
    }

    "The JdbcStateStoreProvider" should "throw an exception on missing connection" in {
        val db = tempDir.resolve("mydb")
        val spec =
            """
              |kind: jdbc
              |connection: logger
            """.stripMargin
        val monitor = ObjectMapper.parse[StateStoreProvider](spec)

        val job = Job.builder()
            .setName("job")
            .build()
        val session = Session.builder()
            .build()

        a[NoSuchElementException] shouldBe thrownBy(monitor.createStateStore(session))
    }

    it should "be parseable" in {
        val spec =
            """
              |kind: jdbc
              |connection: logger
            """.stripMargin

        val monitor = ObjectMapper.parse[StateStoreProvider](spec)
        monitor shouldBe a[JdbcStateStoreProvider]
    }
}

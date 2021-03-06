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

package com.dimajix.flowman.tools.exec.mapping

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.kohsuke.args4j.Argument
import org.slf4j.LoggerFactory

import com.dimajix.flowman.execution.Executor
import com.dimajix.flowman.spec.Project
import com.dimajix.flowman.spec.task.CountMappingTask
import com.dimajix.flowman.tools.exec.ActionCommand


class CountCommand extends ActionCommand {
    private val logger = LoggerFactory.getLogger(classOf[CountCommand])

    @Argument(usage = "specifies the mapping to count", metaVar = "<mapping>", required = true)
    var mapping: String = ""

    override def executeInternal(executor:Executor, project: Project) : Boolean = {
        val task = CountMappingTask(mapping)

        Try {
            task.execute(executor)
        } match {
            case Success(_) =>
                logger.info("Successfully counted  mapping")
                true
            case Failure(e) =>
                logger.error(s"Caught exception while counting mapping '$mapping", e)
                false
        }
    }
}

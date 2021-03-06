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

package com.dimajix.flowman.tools.exec

import com.dimajix.flowman.execution.Executor
import com.dimajix.flowman.execution.Session
import com.dimajix.flowman.spec.Project


abstract class ActionCommand extends Command {
    override def execute(project:Project, session: Session): Boolean = {
        super.execute(project, session)

        // Create project specific executor
        val executor = session.getExecutor(project)
        val result = executeInternal(executor, project)

        // Cleanup caches, but after printing error message. Otherwise it looks confusing when the error occured
        session.executor.cleanup()

        result
    }

    def executeInternal(executor:Executor, project: Project) : Boolean
}

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

package com.dimajix.flowman.execution

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.dimajix.flowman.LocalSparkSession


class ContextTest extends FlatSpec with Matchers {
    "Evaluation" should "work (1)" in {
        val context = new RootContext(null, Seq())
        context.setEnvironment("env_1", "value_1", SettingLevel.NONE)
        context.evaluate("$env_1") should be ("value_1")
    }

    "Evaluation" should "work (2)" in {
        val context = new RootContext(null, Seq())
        context.setEnvironment("env_1", "value_1", SettingLevel.NONE)
        context.setEnvironment("env_2", "$env_1", SettingLevel.NONE)
        context.setEnvironment("env_3", "$env_2", SettingLevel.NONE)
        context.evaluate("$env_2") should be ("value_1")
        context.evaluate("$env_3") should be ("value_1")
    }

    "Evaluation" should "not replace unknown vars" in {
        val context = new RootContext(null, Seq())
        context.setEnvironment("env_1", "value_1", SettingLevel.NONE)
        context.evaluate("$env_2") should be ("$env_2")
        context.setEnvironment("env_2", "value_2", SettingLevel.NONE)
        context.evaluate("$env_2") should be ("value_2")
    }
}

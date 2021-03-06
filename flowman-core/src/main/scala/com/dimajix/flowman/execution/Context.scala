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

import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf

import com.dimajix.flowman.hadoop.FileSystem
import com.dimajix.flowman.spec.ConnectionIdentifier
import com.dimajix.flowman.spec.JobIdentifier
import com.dimajix.flowman.spec.TargetIdentifier
import com.dimajix.flowman.spec.Profile
import com.dimajix.flowman.spec.Project
import com.dimajix.flowman.spec.RelationIdentifier
import com.dimajix.flowman.spec.MappingIdentifier
import com.dimajix.flowman.spec.Namespace
import com.dimajix.flowman.spec.connection.Connection
import com.dimajix.flowman.spec.flow.Mapping
import com.dimajix.flowman.spec.model.Relation
import com.dimajix.flowman.spec.target.Target
import com.dimajix.flowman.spec.task.Job

case class SettingLevel(
    level:Int
)
object SettingLevel {
    val SCOPE_OVERRIDE = new SettingLevel(500)
    val GLOBAL_OVERRIDE = new SettingLevel(300)
    val PROJECT_PROFILE = new SettingLevel(250)
    val PROJECT_SETTING = new SettingLevel(200)
    val NAMESPACE_PROFILE = new SettingLevel(150)
    val NAMESPACE_SETTING = new SettingLevel(100)
    val NONE = new SettingLevel(0)
}

object Context {
    abstract class Builder {
        def withEnvironment(env: Seq[(String, Any)]): Builder
        def withConfig(env:Map[String,String]) : Builder
        def withConnections(env:Map[String,Connection]) : Builder
        def withProfile(profile:Profile) : Builder

        def withConfig(config:Map[String,String], level:SettingLevel) : Builder
        def withConnections(env:Map[String,Connection], level:SettingLevel) : Builder
        def withEnvironment(env:Seq[(String,Any)], level:SettingLevel) : Builder

        def build() : Context
    }
}

abstract class Context {
    /**
      * Returns the namespace associated with this context. Can be null
      * @return
      */
    def namespace : Namespace

    /**
      * Returns the project associated with this context. Can be null
      * @return
      */
    def project : Project

    /**
      * Returns the root context in a hierarchy of connected contexts
      * @return
      */
    def root : Context

    /**
      * Evaluates a string containing expressions to be processed.
      *
      * @param string
      * @return
      */
    def evaluate(string: String): String

    /**
      * Try to retrieve the specified database connection. Performs lookups in parent context if required
      *
      * @param identifier
      * @return
      */
    def getConnection(identifier: ConnectionIdentifier): Connection
    /**
      * Returns a specific named Mapping. The Transform can either be inside this Contexts project or in a different
      * project within the same namespace
      *
      * @param identifier
      * @return
      */
    def getMapping(identifier: MappingIdentifier) : Mapping
    /**
      * Returns a specific named Relation. The RelationType can either be inside this Contexts project or in a different
      * project within the same namespace
      *
      * @param identifier
      * @return
      */
    def getRelation(identifier: RelationIdentifier): Relation
    /**
      * Returns a specific named Target. The TargetType can either be inside this Contexts project or in a different
      * project within the same namespace
      *
      * @param identifier
      * @return
      */
    def getTarget(identifier: TargetIdentifier): Target
    /**
      * Returns a specific named Job. The JobType can either be inside this Contexts project or in a different
      * project within the same namespace
      *
      * @param identifier
      * @return
      */
    def getJob(identifier: JobIdentifier): Job

    /**
      * Returns all configuration options as a key-value map
      *
      * @return
      */
    def config: Map[String, String]
    def rawConfig : Map[String,(String, Int)]

    /**
      * Returns the current environment used for replacing variables
      *
      * @return
      */
    def environment: Map[String, Any]
    def rawEnvironment : Map[String,(Any, Int)]

    def getProjectContext(projectName:String) : Context
    def getProjectContext(project:Project) : Context

    /**
      * Returns the FileSystem as configured in Hadoop
      * @return
      */
    def fs : FileSystem

    /**
      * Returns a SparkConf object, which contains all Spark settings as specified in the conifguration. The object
      * is not necessarily the one used by the Spark Session!
      * @return
      */
    def sparkConf : SparkConf

    /**
      * Returns a Hadoop Configuration object which contains all settings form the configuration. The object is not
      * necessarily the one used by the active Spark session
      * @return
      */
    def hadoopConf : Configuration
}

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

import scala.collection.mutable

import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.RuntimeConfig
import org.apache.spark.sql.SparkSession

import com.dimajix.flowman.catalog.Catalog
import com.dimajix.flowman.spec.Project
import com.dimajix.flowman.spec.MappingIdentifier
import com.dimajix.flowman.spec.Namespace


abstract class Executor {
    def session: Session

    def project : Project
    def namespace : Namespace

    def root: Executor

    /**
      * Returns the appropriate runner
      *
      * @return
      */
    def runner : Runner

    /**
      * Returns (or lazily creates) a SparkSession of this Executor. The SparkSession will be derived from the global
      * SparkSession, but a new derived session with a separate namespace will be created.
      *
      * @return
      */
    def spark: SparkSession

    /**
      * Returns the Spark configuration
      */
    def sparkConf : RuntimeConfig = spark.conf

    /**
      * Returns the Hadoop configuration as used by Spark
      * @return
      */
    def hadoopConf : Configuration = spark.sparkContext.hadoopConfiguration

    /**
      * Returns true if a SparkSession is already available
      * @return
      */
    def sparkRunning: Boolean

    /**
      * Returns the table catalog used for managing table instances
      * @return
      */
    def catalog: Catalog = session.catalog

    /**
      * Returns the Context associated with this Executor. This context will be used for looking up
      * databases and relations and for variable substition
      *
      * @return
      */
    def context : Context

    /**
      * Creates an instance of a table of a Dataflow, or retrieves it from cache
      *
      * @param identifier
      */
    def instantiate(identifier: MappingIdentifier) : DataFrame

    /**
      * Releases any temporary tables
      */
    def cleanup() : Unit

    /**
      * Returns the DataFrame cache of Mappings used in this Executor hierarchy.
      * @return
      */
    protected[execution] def cache : mutable.Map[(String,String),DataFrame]
}

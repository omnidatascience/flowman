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

package com.dimajix.flowman

import java.io.File
import java.io.IOException
import java.util.UUID

import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.conf.HiveConf.ConfVars
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite
import org.scalatest.mockito.MockitoSugar


trait LocalSparkSession extends BeforeAndAfterAll  with MockitoSugar { this:Suite =>
    var spark: SparkSession = _

    override def beforeAll() : Unit = {
        val builder = SparkSession.builder()
            .master("local[4]")
            .config("spark.ui.enabled", "false")
            .config("spark.sql.shuffle.partitions", "8")

        val tempDir = createTempDir()
        val localMetastorePath = new File(tempDir, "metastore").getCanonicalPath
        val localWarehousePath = new File(tempDir, "wharehouse").getCanonicalPath

        // We have to mask all properties in hive-site.xml that relates to metastore
        // data source as we used a local metastore here.
        val hiveConfVars = HiveConf.ConfVars.values()
        hiveConfVars.foreach { confvar =>
            if (confvar.varname.contains("datanucleus") ||
                confvar.varname.contains("jdo")) {
                builder.config(confvar.varname, confvar.getDefaultExpr())
            }
        }
        builder.config("javax.jdo.option.ConnectionURL", s"jdbc:derby:;databaseName=$localMetastorePath;create=true")
            .config("datanucleus.rdbms.datastoreAdapterClassName", "org.datanucleus.store.rdbms.adapter.DerbyAdapter")
            .config(ConfVars.METASTOREURIS.varname, "")
            .config("spark.sql.streaming.checkpointLocation", createTempDir().toPath().toString)
            .config("spark.sql.warehouse.dir", localWarehousePath)
            .enableHiveSupport()
        spark = builder.getOrCreate()
        spark.sparkContext.setLogLevel("WARN")
    }
    override def afterAll() : Unit = {
        if (spark != null) {
            spark.stop()
            spark = null
        }
    }

    /**
      * Create a directory inside the given parent directory.
      * The directory is guaranteed to be newly created, and is not marked for automatic
      * deletion.
      */
    private def createDirectory(root: String): File = {
        var attempts = 0
        val maxAttempts = 10
        var dir: File = null
        while (dir == null) {
            attempts += 1
            if (attempts > maxAttempts) {
                throw new IOException(
                    s"Failed to create a temp directory (under ${root}) after ${maxAttempts}")
            }
            try {
                dir = new File(root, "spark-" + UUID.randomUUID.toString)
                if (dir.exists() || !dir.mkdirs()) {
                    dir = null
                }
            } catch { case e: SecurityException => dir = null; }
        }

        dir
    }

    /**
      * Create a temporary directory inside the given parent directory.
      * The directory will be automatically deleted when the VM shuts down.
      */
    private def createTempDir(root: String = System.getProperty("java.io.tmpdir")): File = {
        val dir = createDirectory(root)
        dir
    }
}

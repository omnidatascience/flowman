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

package com.dimajix.flowman.spec.flow

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.ArrayType
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.dimajix.flowman.LocalSparkSession
import com.dimajix.flowman.execution.Session
import com.dimajix.flowman.spec.MappingIdentifier
import com.dimajix.flowman.spec.Module
import com.dimajix.flowman.{types => ftypes}


class ConformMappingTest extends FlatSpec with Matchers with LocalSparkSession {
    private val inputJson =
        """
          |{
          |  "str_col": "1234",
          |  "double_col": 12.34,
          |  "embedded" : {
          |    "some_string": "567",
          |    "struct_array": [
          |       {
          |         "value": 123
          |       },
          |       {
          |         "value": 456
          |       }
          |    ]
          |  }
          |}""".stripMargin

    private var inputDf: DataFrame = _

    override def beforeAll(): Unit = {
        super.beforeAll()

        val spark = this.spark
        import spark.implicits._

        val inputRecords = Seq(inputJson.replace("\n", ""))
        val inputDs = spark.createDataset(inputRecords)
        inputDf = spark.read.json(inputDs)
    }

    "A ConformMapping" should "be parseable" in {
        val spec =
            """
              |mappings:
              |  my_structure:
              |    kind: conform
              |    input: some_mapping
              |    types:
              |      long: string
              |      date: timestamp
            """.stripMargin

        val project = Module.read.string(spec).toProject("project")
        val mapping = project.mappings("my_structure")

        mapping shouldBe an[ConformMapping]
    }

    it should "transform DataFrames correctly" in {
        val mapping = ConformMapping(
            "input_df",
            Map(
                "string" -> "int"
            )
        )

        val session = Session.builder().withSparkSession(spark).build()
        val executor = session.executor
        implicit val context = executor.context

        val outputDf = mapping.execute(executor, Map(MappingIdentifier("input_df") -> inputDf))

        val expectedSchema = StructType(Seq(
            StructField("double_col", DoubleType),
            StructField("embedded", StructType(Seq(
                StructField("some_string", IntegerType),
                StructField("struct_array", ArrayType(
                    StructType(Seq(
                        StructField("value", LongType)
                    ))
                ))
            ))),
            StructField("str_col", IntegerType)
        ))

        outputDf.count should be (1)
        outputDf.schema should be (expectedSchema)
    }

    it should "provide a correct output schema" in {
        val mapping = ConformMapping(
            "input_df",
            Map(
                "string" -> "int"
            )
        )

        val session = Session.builder().withSparkSession(spark).build()
        val executor = session.executor
        implicit val context = executor.context

        val expectedSchema = StructType(Seq(
            StructField("double_col", DoubleType),
            StructField("embedded", StructType(Seq(
                StructField("some_string", IntegerType),
                StructField("struct_array", ArrayType(
                    StructType(Seq(
                        StructField("value", LongType)
                    ))
                ))
            ))),
            StructField("str_col", IntegerType)
        ))

        val outputSchema = mapping.describe(context, Map(MappingIdentifier("input_df") -> ftypes.StructType.of(inputDf.schema)))
        outputSchema.sparkType should be (expectedSchema)
    }

    it should "throw an error for arrays" in {
        val mapping = ConformMapping(
            "input_df",
            Map(
                "long" -> "int"
            )
        )

        val session = Session.builder().withSparkSession(spark).build()
        val executor = session.executor
        implicit val context = executor.context

        an[UnsupportedOperationException] shouldBe thrownBy(mapping.execute(executor, Map(MappingIdentifier("input_df") -> inputDf)))
    }
}
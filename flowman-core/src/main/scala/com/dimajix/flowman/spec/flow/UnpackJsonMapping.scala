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

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.types.StringType
import org.slf4j.LoggerFactory

import com.dimajix.flowman.execution.Context
import com.dimajix.flowman.execution.Executor
import com.dimajix.flowman.spec.MappingIdentifier
import com.dimajix.flowman.spec.flow.UnpackJsonMapping.ColumnMapping
import com.dimajix.flowman.spec.schema.Schema
import com.dimajix.flowman.types.Field
import com.dimajix.flowman.types.StructType


object UnpackJsonMapping {
    class ColumnMapping {
        @JsonProperty(value="name", required=true) private var _name:String = _
        @JsonProperty(value="alias", required=true) private var _alias:String = _
        @JsonProperty(value="schema", required=true) private var _schema: Schema = _

        def name(implicit context: Context) : String = context.evaluate(_name)
        def alias(implicit context: Context) : String = context.evaluate(_alias)
        def schema(implicit context: Context) : Schema = _schema
    }
}


class UnpackJsonMapping extends BaseMapping {
    private val logger = LoggerFactory.getLogger(classOf[UnpackJsonMapping])

    @JsonProperty(value = "input", required = true) private var _input: String = _
    @JsonProperty(value = "columns", required = true) private var _columns: Seq[ColumnMapping] = Seq()
    @JsonProperty(value = "corruptedColumn", required = false) private var _corruptedColumn: String = "_corrupt_record"
    @JsonProperty(value = "allowComments", required = false) private var _allowComments: String = "false"
    @JsonProperty(value = "allowUnquotedFieldNames", required = false) private var _allowUnquotedFieldNames: String = "false"
    @JsonProperty(value = "allowSingleQuotes", required = false) private var _allowSingleQuotes: String = "true"
    @JsonProperty(value = "allowNumericLeadingZeros", required = false) private var _allowNumericLeadingZeros: String = "false"
    @JsonProperty(value = "allowNonNumericNumbers", required = false) private var _allowNonNumericNumbers: String = "true"
    @JsonProperty(value = "allowBackslashEscapingAnyCharacter", required = false) private var _allowBackslashEscapingAnyCharacter: String = "false"
    @JsonProperty(value = "allowUnquotedControlChars", required = false) private var _allowUnquotedControlChars: String = "false"

    def input(implicit context: Context): MappingIdentifier = MappingIdentifier.parse(context.evaluate(_input))

    def columns(implicit context: Context): Seq[ColumnMapping] = _columns

    def corruptedColumn(implicit context: Context): String = context.evaluate(_corruptedColumn)

    def allowComments(implicit context: Context): Boolean = context.evaluate(_allowComments).toBoolean

    def allowUnquotedFieldNames(implicit context: Context): Boolean = context.evaluate(_allowUnquotedFieldNames)
        .toBoolean

    def allowSingleQuotes(implicit context: Context): Boolean = context.evaluate(_allowSingleQuotes).toBoolean

    def allowNumericLeadingZeros(implicit context: Context): Boolean = context.evaluate(_allowNumericLeadingZeros)
        .toBoolean

    def allowNonNumericNumbers(implicit context: Context): Boolean = context.evaluate(_allowNonNumericNumbers).toBoolean

    def allowBackslashEscapingAnyCharacter(implicit context: Context): Boolean = context
        .evaluate(_allowBackslashEscapingAnyCharacter).toBoolean

    def allowUnquotedControlChars(implicit context: Context): Boolean = context.evaluate(_allowUnquotedControlChars)
        .toBoolean

    /**
      * Returns the dependencies (i.e. names of tables in the Dataflow model)
      *
      * @param context
      * @return
      */
    override def dependencies(implicit context: Context): Array[MappingIdentifier] = {
        Array(input)
    }

    /**
      * Executes this MappingType and returns a corresponding DataFrame
      *
      * @param executor
      * @param tables
      * @return
      */
    override def execute(executor: Executor, tables: Map[MappingIdentifier, DataFrame]): DataFrame = {
        implicit val context = executor.context
        val input = this.input
        val columns = this.columns
        logger.info(s"Unpacking JSON columns $columns from mapping '$input'")

        val table = tables(input)
        val options = Map(
            "columnNameOfCorruptRecord" -> corruptedColumn,
            "allowComments" -> allowComments.toString,
            "allowUnquotedFieldNames" -> allowUnquotedFieldNames.toString,
            "allowSingleQuotes" -> allowSingleQuotes.toString,
            "allowNumericLeadingZeros" -> allowNumericLeadingZeros.toString,
            "allowNonNumericNumbers" -> allowNonNumericNumbers.toString,
            "allowBackslashEscapingAnyCharacter" -> allowBackslashEscapingAnyCharacter.toString,
            "allowUnquotedControlChars" -> allowUnquotedControlChars.toString
        )

        columns.foldLeft(table) { (t, c) =>
            val sparkSchema = c.schema.sparkSchema
            t.withColumn(Option(c.alias).getOrElse(c.name), from_json(table(c.name)
                    .cast(StringType), sparkSchema, options))
        }
    }

    /**
      * Returns the schema as produced by this mapping, relative to the given input schema
      *
      * @param context
      * @param input
      * @return
      */
    override def describe(context: Context, input: Map[MappingIdentifier, StructType]): StructType = {
        require(context != null)
        require(input != null)

        implicit val icontext = context
        val schema = input(this.input)
        val fields = schema.fields ++ columns.map(c => Field(Option(c.alias).getOrElse(c.name), StructType(c.schema.fields)))
        StructType(fields)
    }
}

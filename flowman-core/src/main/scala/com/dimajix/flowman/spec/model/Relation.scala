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

package com.dimajix.flowman.spec.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.util.StdConverter
import org.apache.hadoop.fs.Path
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.types.StructType

import com.dimajix.flowman.execution.Context
import com.dimajix.flowman.execution.Executor
import com.dimajix.flowman.spec.Resource
import com.dimajix.flowman.spec.schema.Schema
import com.dimajix.flowman.spi.TypeRegistry
import com.dimajix.flowman.types.Field
import com.dimajix.flowman.types.FieldValue
import com.dimajix.flowman.types.SingleValue


object Relation extends TypeRegistry[Relation] {
    class NameResolver extends StdConverter[Map[String,Relation],Map[String,Relation]] {
        override def convert(value: Map[String,Relation]): Map[String,Relation] = {
            value.foreach(kv => kv._2._name = kv._1)
            value
        }
    }
}

/**
  * Interface class for declaring relations (for sources and sinks) as part of a model
  */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible=true)
@JsonSubTypes(value = Array(
    new JsonSubTypes.Type(name = "jdbc", value = classOf[JdbcRelation]),
    new JsonSubTypes.Type(name = "table", value = classOf[HiveTableRelation]),
    new JsonSubTypes.Type(name = "view", value = classOf[HiveViewRelation]),
    new JsonSubTypes.Type(name = "hiveTable", value = classOf[HiveTableRelation]),
    new JsonSubTypes.Type(name = "hiveView", value = classOf[HiveViewRelation]),
    new JsonSubTypes.Type(name = "file", value = classOf[FileRelation]),
    new JsonSubTypes.Type(name = "local", value = classOf[LocalRelation]),
    new JsonSubTypes.Type(name = "provided", value = classOf[ProvidedRelation]),
    new JsonSubTypes.Type(name = "null", value = classOf[NullRelation])
))
abstract class Relation extends Resource {
    @JsonIgnore private var _name:String = ""

    @JsonProperty(value="kind", required = true) private var _kind: String = _
    @JsonProperty(value="labels", required=false) private var _labels:Map[String,String] = Map()

    /**
      * Returns the name of the relation
      * @return
      */
    final override def name : String = _name

    /**
      * Returns the category of this resource
      * @return
      */
    final override def category: String = "relation"

    /**
      * Returns the specific kind of this resource
      * @return
      */
    final override def kind: String = _kind

    /**
      * Returns a map of user defined labels
      * @param context
      * @return
      */
    final override def labels(implicit context: Context) : Map[String,String] = _labels.mapValues(context.evaluate)

    /**
      * Returns a description of the relation
      * @param context
      * @return
      */
    def description(implicit context: Context) : String

    /**
      * Returns the Schema object which describes all fields of the relation
      * @param context
      * @return
      */
    def schema(implicit context: Context) : Schema

    /**
      * Returns a list of fields
      * @param context
      * @return
      */
    def fields(implicit context: Context) : Seq[Field] = schema.fields

    /**
      * Reads data from the relation, possibly from specific partitions
      *
      * @param executor
      * @param schema - the schema to read. If none is specified, all available columns will be read
      * @param partitions - List of partitions. If none are specified, all the data will be read
      * @return
      */
    def read(executor:Executor, schema:StructType, partitions:Map[String,FieldValue] = Map()) : DataFrame

    /**
      * Writes data into the relation, possibly into a specific partition
      * @param executor
      * @param df - dataframe to write
      * @param partition - destination partition
      */
    def write(executor:Executor, df:DataFrame, partition:Map[String,SingleValue] = Map(), mode:String = "OVERWRITE") : Unit

    /**
      * Removes one or more partitions.
      * @param executor
      * @param partitions
      */
    def clean(executor:Executor, partitions:Map[String,FieldValue] = Map()) : Unit

    /**
      * Reads data from a streaming source
      * @param executor
      * @param schema
      * @return
      */
    def readStream(executor:Executor, schema:StructType) : DataFrame = ???

    /**
      * Writes data to a streaming sink
      * @param executor
      * @param df
      * @return
      */
    def writeStream(executor:Executor, df:DataFrame, mode:OutputMode, checkpointLocation:Path) : StreamingQuery = ???

    /**
      * Returns true if the relation already exists, otherwise it needs to be created prior usage
      * @param executor
      * @return
      */
    def exists(executor:Executor) : Boolean

    /**
      * This method will physically create the corresponding relation. This might be a Hive table or a directory. The
      * relation will not contain any data, but all metadata will be processed
      * @param executor
      */
    def create(executor:Executor, ifNotExists:Boolean=false) : Unit

    /**
      * This will delete any physical representation of the relation. Depending on the type only some meta data like
      * a Hive table might be dropped or also the physical files might be deleted
      * @param executor
      */
    def destroy(executor:Executor, ifExists:Boolean=false) : Unit

    /**
      * This will update any existing relation to the specified metadata.
      * @param executor
      */
    def migrate(executor:Executor) : Unit
}

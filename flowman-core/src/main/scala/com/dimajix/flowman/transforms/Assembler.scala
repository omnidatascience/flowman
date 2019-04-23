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

package com.dimajix.flowman.transforms

import scala.collection.mutable

import org.apache.spark.sql.DataFrame

import com.dimajix.flowman.transforms.schema.ColumnTree
import com.dimajix.flowman.transforms.schema.Node
import com.dimajix.flowman.transforms.schema.NodeOps
import com.dimajix.flowman.transforms.schema.Path
import com.dimajix.flowman.transforms.schema.SchemaTree
import com.dimajix.flowman.transforms.schema.StructNode
import com.dimajix.flowman.types.Field
import com.dimajix.flowman.types.StructType


object Assembler {
    abstract class Builder {
        def build() : Assembler
    }
    class ColumnBuilder extends Builder {
        private var _path = Path()
        private val _keep = mutable.ListBuffer[Path]()
        private val _drop = mutable.ListBuffer[Path]()

        def path(p:String) : ColumnBuilder = {
            _path = Path(p)
            this
        }
        def keep(c:String) : ColumnBuilder = {
            _keep += Path(c)
            this
        }
        def keep(c:Seq[String]) : ColumnBuilder = {
            _keep ++= c.map(Path(_))
            this
        }
        def drop(c:String) : ColumnBuilder = {
            _drop += Path(c)
            this
        }
        def drop(c:Seq[String]) : ColumnBuilder = {
            _drop ++= c.map(Path(_))
            this
        }

        override def build(): Assembler = {
            new ColumnAssembler(_path, _keep, _drop)
        }
    }

    class StructBuilder extends Builder {
        private val _children = mutable.ListBuffer[(Option[String],Builder)]()

        def columns(spec:ColumnBuilder => Unit) : StructBuilder = {
            val builder = new ColumnBuilder
            spec(builder)
            _children += ((None, builder))
            this
        }
        def nest(name:String)(spec:ColumnBuilder => Unit) : StructBuilder = {
            val builder = new ColumnBuilder
            spec(builder)
            _children += ((Some(name), builder))
            this
        }
        def assemble(name:String)(spec:StructBuilder => Unit) : StructBuilder = {
            val builder = new StructBuilder
            spec(builder)
            _children += ((Some(name), builder))
            this
        }

        override def build(): Assembler = {
            val children = _children.map { case(name, builder) => (name, builder.build()) }
            new StructAssembler(children)
        }
    }

    def builder() = new StructBuilder()
}

/**
  * The main class for reassembling DataFrames (and Flowman Schemas)
  */
sealed abstract class Assembler {
    import com.dimajix.flowman.transforms.schema.ColumnTree.implicits._
    import com.dimajix.flowman.transforms.schema.SchemaTree.implicits._

    /**
      * Generic method for reassembling the given Node of a schema tree
      * @param root
      * @param ops
      * @tparam T
      * @return
      */
    def reassemble[T](root:Node[T])(implicit ops:NodeOps[T]) : Node[T]

    /**
      * Reassembles a Spark DataFrame
      * @param df
      * @return
      */
    def reassemble(df:DataFrame) : DataFrame = {
        val tree = ColumnTree.ofSchema(df.schema)
        val newTree = reassemble(tree)
        val columns = newTree.children.map(_.mkValue())
        df.select(columns:_*)
    }

    /**
      * Reassembles a Flowman schema (given as a sequence of fields)
      * @param fields
      * @return
      */
    def reassemble(fields:StructType) : StructType = {
        val tree = SchemaTree.ofSchema(fields)
        val newTree = reassemble(tree)
        val columns = newTree.children.map(_.mkValue())
        StructType(columns)
    }
}


class ColumnAssembler private[transforms] (path:Path, keep:Seq[Path], drop:Seq[Path]) extends Assembler {
    override def reassemble[T](root:Node[T])(implicit ops:NodeOps[T]) : Node[T] = {
        val child = root.find(path)
        if(keep.nonEmpty)
            child.map(_.keep(keep).drop(drop)).getOrElse(Node.empty[T])
        else
            child.map(_.drop(drop)).getOrElse(Node.empty[T])
    }
}

class StructAssembler private[transforms] (columns:Seq[(Option[String],Assembler)]) extends Assembler {
    override def reassemble[T](root:Node[T])(implicit ops:NodeOps[T]) : Node[T] = {
        val children = columns.flatMap { case (name, asm) =>
            if (name.isEmpty) {
                asm.reassemble(root).children
            }
            else {
                Seq(asm.reassemble(root).withName(name.get))
            }
        }
        StructNode("", children)
    }
}
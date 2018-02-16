package com.dimajix.dataflow.spec.schema

import org.apache.spark.sql.types.DataType

import com.dimajix.dataflow.execution.Context


case object FloatType extends FieldType {
    override def sparkType(implicit context: Context) : DataType = org.apache.spark.sql.types.FloatType
    override def parse(value:String) : Any = value.toFloat
    override def interpolate(value: FieldValue, granularity:String) : Iterable[Any] = {
        value match {
            case SingleValue(v) => Seq(parse(v))
            case ArrayValue(values) => values.map(parse)
            case RangeValue(start,end) => {
                start.toFloat until end.toFloat by granularity.toFloat
            }
        }
    }
}
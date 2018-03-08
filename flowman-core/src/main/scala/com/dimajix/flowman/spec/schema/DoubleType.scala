package com.dimajix.flowman.spec.schema

import org.apache.spark.sql.types.DataType


case object DoubleType extends FieldType {
    override def sparkType : DataType = org.apache.spark.sql.types.DoubleType

    override def parse(value:String) : Any = value.toDouble
    override def interpolate(value: FieldValue, granularity:String) : Iterable[Any] = {
        value match {
            case SingleValue(v) => Seq(parse(v))
            case ArrayValue(values) => values.map(parse)
            case RangeValue(start,end) => {
                start.toDouble until end.toDouble by granularity.toDouble
            }
        }
    }
}

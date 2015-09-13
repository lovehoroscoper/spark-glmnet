/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package org.apache.spark.examples.ml

import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.sql.SQLContext
import org.apache.spark.mllib.util.LinearDataGenerator
import org.apache.spark.ml.tuning.{ CrossValidator, AutoGeneratedParamGridBuilder }
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.regression.LinearRegressionWithCD
import org.apache.spark.Logging
import org.apache.spark.sql.Row
import org.apache.spark.sql.DataFrame

/**
 * A simple example demonstrating linear regression model selection using CrossValidator and auto-generated regParam values.
 */
// From spark/examples/src/main/scala/org/apache/spark/examples/ml/CrossValidatorExample.scala
// http://spark.apache.org/docs/latest/ml-guide.html
object LinearRegressionCrossValidatorExample extends Logging {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("LinearRegressionCrossValidatorExample")//.setMaster("local")
    val sc = new SparkContext(conf)

    // nexamples, nfeatures, eps, intercept, fracTest
    val (training, test) = generateData(sc, 1000, 10, 0.1, 6.2, 0.2)

    val lr = (new LinearRegressionWithCD(""))
      .setMaxIter(100)

    val paramGrid = new AutoGeneratedParamGridBuilder()
      .addGrid(lr.elasticNetParam, Array(0.2, 0.3))
      .buildWithAutoGeneratedGrid("lambdaIndex", lr.getMaxIter)

    val crossval = (new CrossValidator(""))
      .setEstimator(lr)
      .setEvaluator(new RegressionEvaluator)
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(2) // Use 3+ in practice

    // Run cross-validation, and choose the best set of parameters.
    val cvModel = crossval.fit(training)
    logInfo(s"Best Model:\n${cvModel.bestModel.explainParams}")

    // Make predictions on test data. cvModel uses the best model found (lrModel).
    val predictions = cvModel.transform(test)
    logInfo(s"Evaluation metric (RMSE): ${new RegressionEvaluator().evaluate(predictions)}")
    //logPredictions(predictions)

    sc.stop()
  }

  private def logPredictions(predictions: DataFrame) = {
    predictions.select("label", "prediction")
      .collect()
      .foreach {
        case Row(label: Double, prediction: Double) =>
          logInfo(s"label - prediction = ${label - prediction}")
      }
  }

  /**
   * Generate a tuple of DataFrames (training, test) consisting of RDD[LabeledPoint] containing sample data for Linear Regression models.
   *
   * @param sc SparkContext to be used for generating the RDD.
   * @param nexamples Number of examples that will be contained in the RDD.
   * @param nfeatures Number of features to generate for each example.
   * @param eps Epsilon factor by which examples are scaled.
   * @param intercept Intercept.
   * @param fracTest Fraction of data to hold out for testing. Default value is 0.2.
   * @param nparts Number of partitions in the RDD. Default value is 2.
   *
   * @return Tuple of DataFrames (training, training) consisting of RDD[LabeledPoint] containing sample data.
   */
  private def generateData(
    sc: SparkContext,
    nexamples: Int,
    nfeatures: Int,
    eps: Double = 0.1,
    intercept: Double = 0.0,
    fracTest: Double = 0.2,
    nparts: Int = 2): (DataFrame, DataFrame) = {
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val data = LinearDataGenerator.generateLinearRDD(sc, nexamples, nfeatures, eps, nparts, intercept)
      .randomSplit(Array(1.0 - fracTest, fracTest), seed = 12345)

    val dataFrames = (data(0).toDF(), data(1).toDF())
    logInfo(s"generated data; nexamples: ${nexamples}, nfeatures: ${nfeatures}, eps: ${eps}, intercept: ${intercept}, fracTest: ${fracTest}, training nexamples: ${dataFrames._1.count}, test nexamples: ${dataFrames._2.count}")
    dataFrames
  }
}
// scalastyle:on println

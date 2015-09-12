# spark-glmnet

## glmnet -  “Regularization Paths for Generalized Linear Models via Coordinate Descent"

This project coded, in Scala, the algorithm  “Regularization Paths for Generalized Linear Models via Coordinate Descent” by Jerome Friedman, Trevor Hastie and Rob Tibshirani of Stanford University (http://web.stanford.edu/~hastie/Papers/glmnet.pdf).  The algorithm is typically referred to as “glmnet” - generalized linear model with elastic net regularization.  Elastic net is the combination of the ridge and lasso regularization methods.  This algorithm is generally faster than traditional methods such as linear regression and is particularly well suited for “fat” datasets (many more features than events).

## Developers
    Mike Bowles
    Jake Belew
    Ben Burford

## Build the code (instructions are for running in Eclipse)
	$ git clone git@github.com:jakebelew/spark-glmnet.git
	(create an Eclipse project and import)
	$ cd spark-glmnet
	(Note: if this is your first time running SBT, you will be “downloading the internet” so it may take a while.)
	$ sbt
	> eclipse with-source=true
	> exit

## Run with test data
	Run org.apache.spark.examples.ml.LinearRegressionCrossValidatorExample in eclipse.
	* It will read in data/sample_linear_regression_data.txt and perform glmnet
	* It will run the data in k=2 folds, with alpha = 0.2 and 0.3, and 100 lambda values.
	* It will choose the “Best fit” combination of alpha and lambda and generate a model on the entire data set using the chosen alpha and lambda.

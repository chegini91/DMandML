package test.com.github.TKnudsen.DMandML.model.semiSupervised.activeLearning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.DMandML.data.classification.IClassificationResult;
import com.github.TKnudsen.DMandML.model.semiSupervised.activeLearning.AbstractActiveLearningModel;
import com.github.TKnudsen.DMandML.model.semiSupervised.activeLearning.uncertaintySampling.SmallestMarginActiveLearning;
import com.github.TKnudsen.DMandML.model.supervised.classifier.IClassifier;
import com.github.TKnudsen.DMandML.model.supervised.classifier.impl.numericalFeatures.RandomForest;

import test.com.github.TKnudsen.DMandML.model.supervised.classifier.ClassificationTest;

/**
 * <p>
 * Title: ActiveLearningTest
 * </p>
 * 
 * <p>
 * Description: Simple test/example of an active learning model (Smallest Margin
 * here) applied on a classifier (Random Forest here).
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class ActiveLearningTest {

	public static void main(String[] args) {

		List<NumericalFeatureVector> featureVectors = ClassificationTest.provideFeatureVectors();

		// please note that in an AL process you usually have
		// 1) already labeled instances (training data)
		// 2) unlabeled instances (candidate data)
		// 3) testing data
		List<NumericalFeatureVector> trainingVectors = new ArrayList<>();
		List<NumericalFeatureVector> candidateVectors = new ArrayList<>();
		List<NumericalFeatureVector> testingVectors = new ArrayList<>();
		for (int i = 0; i < featureVectors.size(); i++)
			if (i % 3 == 0)
				trainingVectors.add(featureVectors.get(i));
			else if (i % 3 == 1)
				candidateVectors.add(featureVectors.get(i));
			else
				testingVectors.add(featureVectors.get(i));

		// Classifier - target variable is fixed to "class"
		IClassifier<NumericalFeatureVector> classifier = new RandomForest();

		// (1) train classification model
		classifier.train(trainingVectors);

//		// (2) test the model (optional)
//		IClassificationResult<NumericalFeatureVector> classificationResult = classifier
//				.createClassificationResult(testingVectors);
//
//		for (NumericalFeatureVector fv : classificationResult.getFeatureVectors()) {
//			System.out.println(fv.getName() + ", predicted CLASS = " + classificationResult.getClass(fv)
//					+ ", true CLASS = " + fv.getAttribute("class") + ", Probabilities: "
//					+ classificationResult.getLabelDistribution(fv));
//		}

		System.out.println("Classification finished");

		// AL PART - note that AL will never train classifiers.
		// It is a use-only relation.
		AbstractActiveLearningModel<NumericalFeatureVector> alModel = new SmallestMarginActiveLearning<NumericalFeatureVector>(
				classifier::createClassificationResult);

		alModel.setCandidates(candidateVectors);

		// assigns an interestingness score to every FV (interestingness = AL
		// applicability) - all candidates
		Map<NumericalFeatureVector, Double> candidateScores = alModel.getCandidateScores();

		// asking the AL model for most applicable FVs for the AL process.
		// Winning 10 FVs
		List<NumericalFeatureVector> mostApplicableFVsList = alModel.suggestCandidates(10);

		// print the probability distributions of candidates predicted by the
		// classifier. These probability distributions have most likely been the
		// criterion for the AL model
		// to identify interesting FVs (e.g., SmallestMargin).
		IClassificationResult<NumericalFeatureVector> classificationResult = classifier
				.createClassificationResult(candidateVectors);

		// For SmallestMargin AL look at the two most likely class probabilities. the
		// winning FV has the smallest margin between best and second best in the entire
		// candidate set. That's the SmallestMargin criterion.
		for (NumericalFeatureVector fv : mostApplicableFVsList) {
			System.out.println(fv.getName() + ", predicted CLASS = " + classificationResult.getClass(fv)
					+ ", true CLASS = " + fv.getAttribute("class") + ", Probabilities: "
					+ classificationResult.getLabelDistribution(fv));
		}
	}

}
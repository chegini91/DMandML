package com.github.TKnudsen.DMandML.model.degreeOfInterest.classBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.IFeatureVectorSupplier;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector.FeatureVectorDistanceMeasureFactory;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector.IFeatureVectorDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.DMandML.data.classification.IProbabilisticClassificationResult;
import com.github.TKnudsen.DMandML.data.classification.IProbabilisticClassificationResultSupplier;
import com.github.TKnudsen.DMandML.data.cluster.featureFV.FeatureVectorCluster;
import com.github.TKnudsen.DMandML.model.unsupervised.clustering.clusterValidity.cluster.AveragePairwiseDistanceCompactnessMeasure;
import com.github.TKnudsen.DMandML.model.unsupervised.clustering.clusterValidity.cluster.ClusterCompactnessMeasure;

/**
 * <p>
 * Title: ClassCompactnessBasedInterestingnessFunction
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2017 Juergen Bernard, https://github.com/TKnudsen/DMandML
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class ClassCompactnessBasedInterestingnessFunction<FV extends AbstractFeatureVector<?, ?>> extends ClassificationBasedInterestingnessFunction<FV> {

	ClusterCompactnessMeasure<FV> clusterCompactnessMeasure;
	private double maxValueOfDiversity = Double.NaN;

	public ClassCompactnessBasedInterestingnessFunction(IFeatureVectorSupplier<FV> featureVectorSupplier, IProbabilisticClassificationResultSupplier<FV> classificationResultSupplier) {
		this(featureVectorSupplier, classificationResultSupplier, new AveragePairwiseDistanceCompactnessMeasure<>());
	}

	public ClassCompactnessBasedInterestingnessFunction(IFeatureVectorSupplier<FV> featureVectorSupplier, IProbabilisticClassificationResultSupplier<FV> classificationResultSupplier, ClusterCompactnessMeasure<FV> clusterCompactnessMeasure) {
		super(featureVectorSupplier, classificationResultSupplier);

		this.clusterCompactnessMeasure = clusterCompactnessMeasure;
	}

	@Override
	public void run() {

		interestingnessScores = new LinkedHashMap<>();

		Map<FV, Double> scores = new HashMap<>();
		List<Double> compactnessScores = new ArrayList<>();

		IProbabilisticClassificationResult<FV> classificationResult = this.getClassificationResultSupplier().get();
		Map<String, List<FV>> classDistributions = classificationResult.getClassDistributions();
		IFeatureVectorDistanceMeasure<FV> distanceMeasure = null;

		for (String label : classDistributions.keySet()) {
			List<FV> fvs = classDistributions.get(label);

			if (distanceMeasure == null)
				distanceMeasure = FeatureVectorDistanceMeasureFactory.createDistanceMeasure(fvs);

			double classCompactness = clusterCompactnessMeasure.getMeasure(new FeatureVectorCluster<>(fvs, distanceMeasure));
			compactnessScores.add(classCompactness);
			for (FV fv : fvs)
				scores.put(fv, classCompactness);
		}

		StatisticsSupport compactnessScoresStatistics = new StatisticsSupport(compactnessScores);
		maxValueOfDiversity = compactnessScoresStatistics.getMax();

		List<FV> featureVectorList = getFeatureVectorSupplier().get();

		for (FV fv : featureVectorList)
			if (scores.containsKey(fv))
				interestingnessScores.put(fv, (1 - MathFunctions.linearScale(0, compactnessScoresStatistics.getMax(), scores.get(fv))));
			else {
				interestingnessScores.put(fv, 0.0);
				System.err.println("ClassCompactnessBasedInterestingnessFunction: unknown FV");
			}
	}

	@Override
	public String getDescription() {
		return "Calculates the compactness of classes of a classification result. ";
	}

	public double getMaxValueOfDiversity() {
		return maxValueOfDiversity;
	}
}
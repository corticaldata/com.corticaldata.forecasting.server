package providers.htm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.joda.time.DateTime;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.algorithms.Classification;
import org.numenta.nupic.algorithms.SDRClassifier;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.encoders.DateEncoder;
import org.numenta.nupic.encoders.ScalarEncoder;
import org.numenta.nupic.model.ComputeCycle;
import org.numenta.nupic.model.Connections;
import org.numenta.nupic.util.ArrayUtils;

public final class HTMProvider {
	
	private static SDRClassifier classifier;
	private static DateEncoder dateEncoder;
	private static ScalarEncoder valueEncoder;
	private static TemporalMemory temporalMemory;
	private static Connections memory;
	private static Map<String, Object> classification = new HashMap<String, Object>();
	private static int count = 0;
	
	public static void initialize() {
		
		// Date encoder
		dateEncoder = DateEncoder
				.builder()
				.name("date")
				.dayOfWeek(3)
				.timeOfDay(5)
				.season(3)
				.periodic(true)
				.build();
		
		// Scalar encoder
		valueEncoder = ScalarEncoder
				.builder()
				.name("value")
				.n(201)
				.w(1)
				.minVal(-100.0)
				.maxVal(100.0)
				.periodic(false)
				.forced(true)
				.build();
		
		// Network configuration
		temporalMemory = new TemporalMemory();
		classifier = new SDRClassifier();
		//classifier = new SDRClassifier(new TIntArrayList(new int[] {1}), 0.1, 0.3, 0);
		memory = new Connections();
		
		Parameters parameters = Parameters.getAllDefaultParameters();
		System.out.println(parameters);
		
		parameters.apply(memory);
		TemporalMemory.init(memory);
	}
	
	public static double forecast(double value, Long date, boolean print) {
		if (classifier == null) {
			initialize();
		}
		
		count++;
		
		// Input through encoder
		
		int[] valueEncoding = valueEncoder.encode(value);
		if (date == null)
			date = 0L;
		int[] dateEncoding = dateEncoder.encode(new DateTime(date));
		
		// Input through temporal memory
		int[] input = ArrayUtils.where(ArrayUtils.concatAll(valueEncoding, dateEncoding), ArrayUtils.WHERE_1);
		ComputeCycle cc = temporalMemory.compute(memory, input, true);
		
		// Get the active cells for classifier input
		int[] activeCellIndexes = Connections.asCellIndexes(cc.activeCells()).stream().mapToInt(p -> p).sorted().toArray();
		//System.out.println(Arrays.toString(activeCellIndexes));
		
		classification.put("bucketIdx", valueEncoder.getBucketIndices(value)[0]);
		classification.put("actValue", value);
		
		Classification<Double> result = classifier.compute(count, classification, activeCellIndexes, true, true);
		/*
		for (int j = 0; j < result.getStatCount(1); j++) {
			System.out.println(result.getActualValue(j) + ", " + result.getStat(1, j));
		}
		*/
		double latestPrediction = result.getMostProbableValue(1);
		if (print) {
			System.out.println("Actual value: " + value + ", Prediction: " + latestPrediction);
		}
		return latestPrediction;
	}
	
	public static void main(String[] args) {
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		Calendar calendar3 = Calendar.getInstance();
		Calendar calendar4 = Calendar.getInstance();
		calendar1.set(Calendar.HOUR_OF_DAY, 9);
		calendar2.set(Calendar.HOUR_OF_DAY, 18);
		calendar3.set(Calendar.HOUR_OF_DAY, 19);
		calendar4.set(Calendar.HOUR_OF_DAY, 3);
		long time1 = calendar1.getTimeInMillis();
		long time2 = calendar2.getTimeInMillis();
		long time3 = calendar3.getTimeInMillis();
		long time4 = calendar3.getTimeInMillis();
		
		// Training, learning
		for (int i = 0; i < 500; i++) {
			forecast(1, time1, false);
			forecast(2, time1, false);
			forecast(3, time1, false);
			forecast(4, time1, false);
			forecast(5, time1, false);
			forecast(6, time1, false);
			
			forecast(1, time2, false);
			forecast(2, time2, false);
			forecast(3, time2, false);
			forecast(7, time2, false);
			forecast(8, time2, false);
			forecast(9, time2, false);
		}
		
		// Testing
		forecast(1, time2, true);
		forecast(2, time2, true);
		forecast(3, time2, true);
		forecast(7, time2, true);
		forecast(8, time2, true);
		forecast(9, time2, true);
		
		forecast(1, time2, true);
		forecast(2, time2, true);
		forecast(3, time2, true);
		forecast(7, time2, true);
		forecast(8, time2, true);
		forecast(9, time2, true);
		
		forecast(1, time1, true);
		forecast(2, time1, true);
		forecast(3, time1, true);
		forecast(4, time1, true);
		forecast(5, time1, true);
		forecast(6, time1, true);
		
		// ¿Qué pasaría en un tiempo parecido a time2 pero desconocido?
		forecast(1, time3, true);
		forecast(2, time3, true);
		forecast(3, time3, true);
		forecast(7, time3, true);
		forecast(8, time3, true);
		forecast(9, time3, true);

		Random random = new Random();
		
		// Dos series distintas
		for (int i = 0; i < 1000; i++) {
			if (random.nextBoolean()) {
				forecast(30, time4, false);
				forecast(40, time4, false);
				forecast(50, time4, false);
			}
			else {
				forecast(70, time4, false);
				forecast(80, time4, false);
			}
		}
		
		System.out.println("--------------");
		
		forecast(70, time4, true);
		forecast(80, time4, true);

		forecast(30, time4, true);
		forecast(40, time4, true);
		forecast(50, time4, true);
		
		forecast(70, time4, true);
		forecast(80, time4, true);

		forecast(70, time4, true);
		forecast(80, time4, true);

		forecast(30, time4, true);
		forecast(40, time4, true);
		forecast(50, time4, true);

		System.out.println("--------------");
		
		forecast(1, time1, true);
		forecast(2, time1, true);
		forecast(3, time1, true);
		forecast(4, time1, true);
		forecast(5, time1, true);
		forecast(6, time1, true);
		
		// ¿Qué pasaría en un tiempo parecido a time2 pero desconocido?
		forecast(1, time3, true);
		forecast(2, time3, true);
		forecast(3, time3, true);
		forecast(7, time3, true);
		forecast(8, time3, true);
		forecast(9, time3, true);
		
		
		
	}
}

import org.apache.log4j.BasicConfigurator;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class IrisClassifier {

    private static int FEATURES_COUNT = 4;
    private static int CLASSES_COUNT = 3;
    public static void main(String[] args) {
        BasicConfigurator.configure(); // Logger required by DL4J
        loadData();

    }
    private static void loadData(){
        try(RecordReader recordReader = new CSVRecordReader(0,','))// Reads every line in the csv file separated by a comma
        {
            recordReader.initialize(new FileSplit(
                    new ClassPathResource("iris.csv").getFile()
            ));
            // Dataset iterator to read all 150 values at once
            // To iterate over dataset we'll use a dataset iterator

            DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, 150, 4, 3);
            DataSet allData = iterator.next();
            allData.shuffle(123);// Seed helps in shuffling the dataset for training

            DataNormalization normalizer = new NormalizerStandardize();
            normalizer.fit(allData);
            normalizer.transform(allData);

            // Data splitting for train-test split
            // 65% training and 35% testing

            SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);
            DataSet trainingData = testAndTrain.getTrain();
            DataSet testingData = testAndTrain.getTest();

            //calling model
            irisNN(trainingData, testingData);

        }
        catch (Exception e) {
            System.out.println("Error: " + e.getLocalizedMessage());
        }
    }

    public static void irisNN (DataSet trainingData, DataSet testingData){
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.1,0.9))
                .l2(0.0001)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(FEATURES_COUNT).nOut(3).build())
                .layer(1, new DenseLayer.Builder().nIn(3).nOut(3).build())
                .layer(2, new OutputLayer.Builder(
                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX)
                        .nIn(3).nOut(CLASSES_COUNT).build())
                .backprop(true).pretrain(false)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(configuration);
        model.init();
        model.fit(trainingData);

        //testing the model

        INDArray output = model.output(testingData.getFeatureMatrix());
        Evaluation eval = new Evaluation(3);
        eval.eval(testingData.getLabels(), output);
        System.out.println(eval.stats());
    }
}

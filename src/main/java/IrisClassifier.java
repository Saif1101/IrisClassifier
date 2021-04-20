import org.apache.log4j.BasicConfigurator;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.io.ClassPathResource;

public class IrisClassifier {
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

        }
        catch (Exception e) {
            System.out.println("Error: " + e.getLocalizedMessage());
        }
    }
}

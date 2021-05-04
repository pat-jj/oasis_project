import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvWriter {
    public static <T> void writeCSV(ArrayList<ArrayList<Integer>> data, String[] header, String filePath)  {
        List<String[]> csvData = createCsvDataSpecial(data, header);

        // default all fields are enclosed in double quotes
        // default separator is a comma
        String filename = filePath.concat("project_data_test.csv");
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeAll(csvData);
        } catch (Exception e){
            System.out.println("failed to write CSV");
        }

    }

    private static List<String[]> createCsvDataSpecial(ArrayList<ArrayList<Integer>> data, String[] header) {

        List<String[]> list = new ArrayList<>();
        list.add(header);

        for (int i = 0; i < data.size(); i++) {
            String[] record = new String[data.get(i).size()];
            for (int j = 0; j < data.get(i).size(); j++) {
                record[j] = String.valueOf(data.get(i).get(j));
            }
            list.add(record);
        }

        return list;
    }
}

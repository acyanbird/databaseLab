import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;


public class BioSystem {

    public static void main(String[] args) {

        // You should only need to fetch the connection details once
        // You might need to change this to either getSocketConnection() or getPortConnection() - see below
        Connection conn = getSocketConnection();

        boolean repeatMenu = true;

        //Assume no files have been provided on the command line - all are set to null
        //For each extra command line argument, add it to the list of files
        String files[] = {null,null,null};
        for(int i = 0; i < args.length; i++){
            files[i] = "'" + args[i] + "'";
        }

        while(repeatMenu){
            System.out.println("_________________________");
            System.out.println("________BioSystem________");
            System.out.println("_________________________");
            System.out.println("1: Delete all data");
            System.out.println("2: Reload Data from " + files[0]);
            System.out.println("3: Reload Data from " + files[1]);
            System.out.println("4: Reload Data from " + files[2]);
            System.out.println("5: Show all species that live more than 1000 years");
            System.out.println("6: Get species with smaller birthweight than...");
            System.out.println("7: List all members of the Falco genus");
            System.out.println("8: List all members of the ... genus");
            System.out.println("9: Show the number of species that have an adult weight greater than 4000g");
            System.out.println("a: Show the common name of all species that have a litter/clutch size greater than 40000.");
            System.out.println("b: Show the species name and common name of species that have an adultweight greater than 200 times their birthweight.");
            System.out.println("c: Change the weight of 'Apis mellifera' ('Honey bee') to be 3g.");
            System.out.println("d: Insert Jungle nightjar.");
            System.out.println("e: Delete Jungle nightjar.");

            System.out.println("q: Quit");

            String menuChoice = readEntry("Please choose an option: ");

            if(menuChoice.length() == 0){
                //Nothing was typed (user just pressed enter) so start the loop again
                continue;
            }
            char option = menuChoice.charAt(0);
            switch(option){
                case '1':
                    deleteAllData(conn);
                    break;
                case '2':
                    if(files[0] == null){
                        System.out.println("Can't load data from NULL file - you should provide a filename");
                    }else{
                        insertAnageData(conn,args[0]);
                    }
                    break;
                case '3':
                    if(files[1] == null){
                        System.out.println("Can't load data from NULL file - you should provide a filename");
                    }else{
                        insertNBNData(conn,args[1]);
                    }
                    break;
                case '4':
                    if(files[2] == null){
                        System.out.println("Can't load data from NULL file - you should provide a filename");
                    }else{
                        insertNBNProvidersAndDataSets(conn, args[2]);
                        insertNBNObsData(conn, args[2]);
                    }
                    break;
                case '5':
                    showAllMoreThan1000Years(conn);
                    break;
                case '6':
                    String maxBirthWeight = readEntry("Please specify a maximum birthweight: ");
                    getSmallerBirthWeight(conn, Float.parseFloat(maxBirthWeight));
                    break;
                case '7':
                    listAllFalcons(conn);
                    break;
                case '8':
                    String genus = readEntry("Please supply a genus such as 'Accipiter', note this is case sensitive so the first letter must be capitalised: ");
                    listAllOfGenus(conn,genus);
                    break;
                case'9':
                    showAdultMoreThan4000g(conn);
                    break;
                case 'a':
                    commonNameLitterCluch(conn);
                    break;
                case 'b':
                    adult400More(conn);
                    break;
                case 'c':
                    changeBee(conn);
                    break;
                case 'd':
                    insertNightjar(conn);
                    break;
                case 'e':
                    deleteNightjar(conn);
                    break;
                case 'q':
                    repeatMenu = false;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /**
     * @param conn An open database connection
     */
    public static void deleteAllData(Connection conn){
        try{
            PreparedStatement delStatement = conn.prepareStatement("DELETE FROM anage;");
            int numRows = delStatement.executeUpdate();
            System.out.println("Deleted " + numRows + " rows");
            /*
            delStatement = conn.prepareStatement("DELETE FROM species_details;");
            numRows = delStatement.executeUpdate();
            System.out.println("Deleted " + numRows + " rows");
            */
            /*
            delStatement = conn.prepareStatement("DELETE FROM observations;");
            numRows = delStatement.executeUpdate();
            System.out.println("Deleted " + numRows + " rows");
            delStatement = conn.prepareStatement("DELETE FROM datasets;");
            numRows = delStatement.executeUpdate();
            System.out.println("Deleted " + numRows + " rows");
            delStatement = conn.prepareStatement("DELETE FROM providers;");
            numRows = delStatement.executeUpdate();
            System.out.println("Deleted " + numRows + " rows");*/
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param conn An open database connection
     * @param data the name of the file that contains the anage TSV dataset
     */
    public static void insertAnageData(Connection conn, String filename1){
        try{
            String[][] data = loadDataFromTSV(filename1);
            PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO anage VALUES (?,?,?,?,?,?,?,?)");
            System.out.println("Inserting " + data.length + " records into anage table");

            int updateFrequency = 5;
            int updatePercentRows = Math.round((float)data.length / (100 / updateFrequency));

            int numRows = 0;

            for(int i = 0; i < data.length; i++){
                String binomialScName = data[i][6] + " " + data[i][7];
                insertStatement.setString(1, binomialScName);
                String commonName = data[i][8];
                insertStatement.setString(2, commonName);
                int gestationDays;
                try{
                    gestationDays = Integer.parseInt(data[i][11]);
                    insertStatement.setInt(3, gestationDays);
                }catch(Exception e){
                    insertStatement.setNull(3,Types.INTEGER);
                }
                float litterSize;
                try{
                    litterSize = Float.parseFloat(data[i][13]);
                    insertStatement.setFloat(4, litterSize);
                }catch(Exception e){
                    insertStatement.setNull(4,Types.DECIMAL);
                }
                float littersPerYear;
                try{
                    littersPerYear = Float.parseFloat(data[i][14]);
                    insertStatement.setFloat(5, littersPerYear);
                }catch(Exception e){
                    insertStatement.setNull(5,Types.DECIMAL);
                }
                float birthWeight;
                try{
                    birthWeight = Float.parseFloat(data[i][16]);
                    insertStatement.setFloat(6, birthWeight);
                }catch(Exception e){
                    insertStatement.setNull(6,Types.DECIMAL);
                }
                float adultWeight;
                try{
                    adultWeight = Float.parseFloat(data[i][18]);
                    insertStatement.setFloat(7, adultWeight);
                }catch(Exception e){
                    insertStatement.setNull(7,Types.DECIMAL);
                }
                float longevity;
                try{
                    longevity = Float.parseFloat(data[i][20]);
                    insertStatement.setFloat(8, longevity);
                }catch(Exception e){
                    insertStatement.setNull(8,Types.DECIMAL);
                }
                numRows += insertStatement.executeUpdate();

                if(numRows % updatePercentRows == 0){
                    System.out.println("Added " + numRows + " out of " + data.length + " (" + Math.round(((float)numRows / data.length)*100) + "%)");
                }
            }
            System.out.println("Added " + numRows + " rows");
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /** Inserts the species data from NBN Atlas
     * @param conn An open database connection
     * @param fn The name of the file that contains the NBN CSV file
     */
    public static void insertNBNData(Connection conn, String fn){
        try{
            String[][] data = loadDataFromCSV(fn);
            PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO species_details VALUES (?,?,?,?,?,?)");
            System.out.println("Inserting " + data.length + " records into species_details table");
            conn.setAutoCommit(false);
            // int updateFrequency = 5;
            // int updatePercentRows = Math.round((float)data.length / (100 / updateFrequency));

            for(int i = 0; i < data.length; i++){
                insertStatement.setString(1, data[i][0]);
                insertStatement.setString(2, data[i][1]);
                insertStatement.setString(3, data[i][2]);
                String s = data[i][5];
                if(!s.equals("")){
                    insertStatement.setString(4, s);
                }else{
                    insertStatement.setNull(4,Types.VARCHAR);
                }
                s = data[i][6];
                if(!s.equals("")){
                    insertStatement.setString(5, s);
                }else{
                    insertStatement.setNull(5,Types.VARCHAR);
                }
                s = data[i][7];
                if(!s.equals("")){
                    insertStatement.setString(6, s);
                }else{
                    insertStatement.setNull(6,Types.VARCHAR);
                }

                insertStatement.addBatch();
            }
            int[] updateCounts = insertStatement.executeBatch();

            int totalAdded = 0;
            for(int i = 0; i < updateCounts.length; i++){
                totalAdded += updateCounts[i];
            }
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Committed " + totalAdded + " rows");
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserts the observation data
     * @param conn An open database connection
     * @param fn The name of the file that contains the NBN CSV Observation data
     */
    public static void insertNBNObsData(Connection conn, String fn){
        try{
            String[][] data = loadDataFromCSV(fn);
            PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO observations VALUES (DEFAULT,?,?,?,?,?,?,?,?,?);");
            // int numRows = 0;
            // int updateFrequency = 5;
            // int updatePercentRows = Math.round((float)data.length / (100 / updateFrequency));
            conn.setAutoCommit(false);

            for(int i = 0; i < data.length; i++){
                //SpeciesID
                insertStatement.setString(1,data[i][8]);

                //Latitude
                float l = Float.parseFloat(data[i][21]);
                insertStatement.setFloat(2,l);
                //Longitude
                l = Float.parseFloat(data[i][22]);
                insertStatement.setFloat(3,l);

                //StartDate
                if(data[i][11].equals("")){
                    //If there's no startdate, ignore this row
                    continue;
                }
                insertStatement.setDate(4,java.sql.Date.valueOf(data[i][11]));

                //DatasetID
                insertStatement.setString(5,data[i][39]);

                //Licence
                insertStatement.setString(6,data[i][2]);

                //RightsHolder
                insertStatement.setString(7,data[i][3]);

                //Recorder
                String r = data[i][25];
                if(r.equals("") || r.toLowerCase().equals("withheld")){
                    insertStatement.setNull(8, Types.VARCHAR);
                }else{
                    insertStatement.setString(8,data[i][25]);
                }
                //Determiner
                r = data[i][26];
                if(r.equals("") || r.toLowerCase().equals("withheld")){
                    insertStatement.setNull(9, Types.VARCHAR);
                }else{
                    insertStatement.setString(9,data[i][26]);
                }

                insertStatement.addBatch();
            }

            int[] updateCounts = insertStatement.executeBatch();

            int totalAdded = 0;
            for(int i = 0; i < updateCounts.length; i++){
                totalAdded += updateCounts[i];
            }
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Committed " + totalAdded + " rows");
            // if(numRows % updatePercentRows == 0){
            //     System.out.println("Added " + numRows + " out of " + data.length + " (" + Math.round(((float)numRows / data.length)*100) + "%)");
            // }

        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserts the data about the datasets and their providers
     * @param conn An open database connection
     * @param fn The name of the file that contains the NBN CSV Observation data
     */
    public static void insertNBNProvidersAndDataSets(Connection conn, String fn){
        //You could also think about merging this with the insertNBNObsData so the loop only has to run once
        //But it's separated here to make it clearer
        try{
            String[][] data = loadDataFromCSV(fn);
            PreparedStatement insertProviderStatement = conn.prepareStatement("INSERT INTO providers VALUES (?, ?) ON CONFLICT DO NOTHING");
            PreparedStatement insertDataSetStatement = conn.prepareStatement("INSERT INTO datasets VALUES (?, ?, ?) ON CONFLICT DO NOTHING");

            conn.setAutoCommit(false);

            for(int i = 0; i < data.length; i++){
                String dataProviderID = data[i][41];
                String dataProviderName = data[i][40];
                String datasetID = data[i][39];
                String datasetName = data[i][38];

                insertProviderStatement.setString(1,dataProviderID);
                insertProviderStatement.setString(2,dataProviderName);
                insertDataSetStatement.setString(1,datasetID);
                insertDataSetStatement.setString(2,datasetName);
                insertDataSetStatement.setString(3,dataProviderID);
                insertProviderStatement.addBatch();
                insertDataSetStatement.addBatch();
            }
            int[] updateCounts = insertProviderStatement.executeBatch();

            int totalProviders = 0;
            for(int i = 0; i < updateCounts.length; i++){
                totalProviders += updateCounts[i];
            }

            updateCounts = insertDataSetStatement.executeBatch();

            int totalDataSets = 0;
            for(int i = 0; i < updateCounts.length; i++){
                totalDataSets += updateCounts[i];
            }

            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Committed " + totalProviders + " providers and " + totalDataSets + " datasets");
            // if(numRows % updatePercentRows == 0){
            //     System.out.println("Added " + numRows + " out of " + data.length + " (" + Math.round(((float)numRows / data.length)*100) + "%)");
            // }
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lists the species and common name of all species in a particular genus
     * This is assumed to be only those species where the first part of the binomial species name matches the genus parameter
     * @param conn An open database connection
     * @param genus The genus being matched
     */
    public static void listAllOfGenus(Connection conn, String genus){
        String selectQuery = "SELECT species, commonname FROM anage WHERE species LIKE ? ORDER BY commonname";
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
            preparedStatement.setString(1, genus + " %");
            ResultSet animals = preparedStatement.executeQuery();

            while (animals.next()) {
                //The longest common name in the entire dataset is 56
                //The longest binomial scientific name is 31
                //So we can format the output like a table:
                System.out.format("%56s, %-31s\t\n", animals.getString(2),animals.getString(1));
            }

            // Always close statements, result sets and connections after use
            // Otherwise you run out of available open cursors!
            preparedStatement.close();
            animals.close();
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lists the species and common name of all falcons (i.e. those of the genus 'Falco')
     * This could also be written to just return listAllOfGenus(conn,'Falco'), but serves as an extra example
     * @param conn
     */
    public static void listAllFalcons(Connection conn){
        String selectQuery = "SELECT species, commonname FROM anage WHERE species LIKE 'Falco %'";
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
            ResultSet animals = preparedStatement.executeQuery();

            while (animals.next()) {
                //The longest common name in the entire dataset is 56
                //The longest binomial scientific name is 31
                //So we can format the output like a table:
                System.out.format("%56s %31s\t\n", animals.getString(2),animals.getString(1));
            }

            // Always close statements, result sets and connections after use
            // Otherwise you run out of available open cursors!
            preparedStatement.close();
            animals.close();
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param conn An open database connection
     * @param filename The filename for the tsv that you are loading
     */
    public static String[][] loadDataFromTSV(String filename) {
        try{
            Scanner scan = new Scanner(new File(filename));

            int lines = -1;
            while(scan.hasNextLine()){
                lines++;
                scan.nextLine();
            }

            scan = new Scanner(new File(filename));
            String headerLine = scan.nextLine();
            String[] headers = headerLine.split("\t");
            int columns = headers.length;

            String[][] res = new String[lines][columns];

            int line = 0;
            while(scan.hasNext()){
                String curLine = scan.nextLine();
                String[] cells = curLine.split("\t");
                res[line] = cells;
                line++;
            }
            return res;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param conn An open database connection
     * @param filename The filename for the csv that you are loading
     */
    public static String[][] loadDataFromCSV(String filename) {

        String[][] loadedData = new String[0][0];
        try {
            CSVReader reader = new CSVReader(new FileReader(filename));
            // BufferedReader csvReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));

            String [] nextLine;

            int lineCount = 0;
            nextLine = reader.readNext();
            int colCount = nextLine.length;
            while ((nextLine = reader.readNext()) != null) {
                lineCount++;
            }

            loadedData = new String[lineCount][colCount];

            reader = new CSVReader(new FileReader(filename));
            lineCount = 0;
            while ((nextLine = reader.readNext()) != null) {
                //Skip the first row
                if(lineCount == 0){
                    lineCount++;
                    continue;
                }

                //Copy this row into loadedData array
                for(int i = 0; i < nextLine.length; i++){
                    loadedData[lineCount-1][i] = nextLine[i];
                }
                lineCount++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch(CsvException e){
            e.printStackTrace();
        }

        return loadedData;

    }

    /**
     * Shows the details of all species that can live for more than 1000 years
     * @param conn An open JDBC connection to database
     */
    private static void showAllMoreThan1000Years(Connection conn){
        String selectQuery = "SELECT commonname,maximumlongevity FROM anage WHERE maximumlongevity > 1000";
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
            ResultSet longLives = preparedStatement.executeQuery();
            while(longLives.next()){
                String commonname = longLives.getString(1);
                float maxAge = longLives.getFloat(2);
                System.out.println(maxAge + ", " + commonname);
            }
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show the details of all animals with a birthweight under a supplied weight
     * @param conn An open database connection
     * @param data a 2d array of String data
     */
    private static void getSmallerBirthWeight(Connection conn, float birthWeight){
        String selectQuery = "SELECT species, commonname, birthweight FROM anage WHERE birthWeight < ?";
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
            preparedStatement.setFloat(1, birthWeight);
            ResultSet animals = preparedStatement.executeQuery();

            while (animals.next()) {
                String commonname = animals.getString(2);
                String species = animals.getString(1);
                float weight = animals.getFloat(3);
                //The longest common name in the entire dataset is 56
                //The longest binomial scientific name is 31
                //So we can format the output like a table:
                System.out.format("%56s %31s\t%2.2f\n", commonname, species, weight);
            }

            // Always close statements, result sets and connections after use
            // Otherwise you run out of available open cursors!
            preparedStatement.close();
            animals.close();
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prompts the user for input
     * @param prompt Prompt for user input
     * @return the text the user typed
     */

    private static String readEntry(String prompt) {

        try {
            StringBuffer buffer = new StringBuffer();
            System.out.print(prompt);
            System.out.flush();
            int c = System.in.read();
            while(c != '\n' && c != -1) {
                buffer.append((char)c);
                c = System.in.read();
            }
            return buffer.toString().trim();
        } catch (IOException e) {
            return "";
        }

    }

    /**
     * Gets the connection to the database using the Postgres driver, connecting via unix sockets
     * @return A JDBC Connection object
     */
    public static Connection getSocketConnection(){
        Properties props = new Properties();
        props.setProperty("socketFactory", "org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");
        props.setProperty("socketFactoryArg",System.getenv("HOME") + "/cs258-postgres/postgres/tmp/.s.PGSQL.5432");
        Connection conn;
        try{
            conn = DriverManager.getConnection("jdbc:postgresql://localhost/nature", props);
            return conn;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the connection to the database using the Postgres driver, connecting via TCP/IP port
     * @return A JDBC Connection object
     */
    public static Connection getPortConnection() {

        String user = "postgres";
        String passwrd = "password";
        Connection conn;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded");
        }

        try {
            conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/nature?user="+ user +"&password=" + passwrd);
            return conn;
        } catch(SQLException e) {
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
            System.out.println("Error retrieving connection");
            return null;
        }


    }


    private static void showAdultMoreThan4000g(Connection conn){
        String selectQuery = "SELECT count(*) FROM anage WHERE anage.adultweight > 4000";
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
            ResultSet Count = preparedStatement.executeQuery();
            while(Count.next()){
                System.out.println(Count.getInt(1));
                // get the column 1 of current row
                // row get down +1 per time
            }
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    private static void commonNameLitterCluch(Connection conn){
        String selectQuery = "SELECT anage.commonname FROM anage WHERE anage.litterorclutchsize > 40000";
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
            ResultSet Count = preparedStatement.executeQuery();
            while(Count.next()){
                String commonName = Count.getString(1);
                // get the column 1 of current row, only 1 column is selected
                // row get down +1 per time
                System.out.println(commonName);
            }
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    private static void adult400More(Connection conn){
        String selectQuery = "SELECT anage.species, anage.commonname FROM anage WHERE anage.adultweight > (200*anage.birthweight)";
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                // get the column 1 of current row, only 1 column is selected
                // row get down +1 per time
                System.out.format("%56s, %-31s\t\n", rs.getString(2),rs.getString(1));
            }
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    private static void changeBee(Connection conn){
        String sql = "UPDATE anage SET adultweight = 3 WHERE species = 'Apis mellifera'";
        // species is primary key
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            // create prepared statement
            int success = preparedStatement.executeUpdate();
            // get int as file discriptor?
            if(success < 0){
                // unsucessful
                System.out.println("Update failed.");
            }
            else {
                System.out.println("Update sucessful.");
            }
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertNightjar(Connection conn){
        String sql = "INSERT INTO anage(Species, CommonName, GestationIncubation, LitterOrClutchSize) VALUES (?,?,?,?)";
        try{
            PreparedStatement psql = conn.prepareStatement(sql);
            // you cant directly write whole, but it's good to have a practice
//            psql.setString(1, "species");
//            psql.setString(2, "commonname");
//            psql.setString(3, "GestationIncubation");
//            psql.setString(4, "LitterOrClutchSize");
            psql.setString(1, "Caprimulgus indicus");
            psql.setString(2, "Jungle nightjar");
            psql.setInt(3, 16);
            psql.setInt(4, 2);

            // can't insert anage?
            // failed
            // create prepared statement
            int success = psql.executeUpdate();
            // get int as file discriptor?
            if(success < 0){
                // unsucessful
                System.out.println("Insert failed.");
            }
            else {
                System.out.println("Insert sucessful.");
            }
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteNightjar(Connection conn){
        String sql = "DELETE FROM anage WHERE species = 'Caprimulgus indicus'";
        // species is primary key
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            // create prepared statement
            int success = preparedStatement.executeUpdate();
            // get int as file discriptor?
            if(success < 0){
                // unsucessful
                System.out.println("Delete failed.");
            }
            else {
                System.out.println("Delete sucessful.");
            }
        }catch(SQLException e){
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
        }
    }

}

import core.Line;
import core.Station;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main
{
    private static Logger LOGGER;
    private static Marker INVALID_STATIONS_MARKER;
    private static Marker INPUT_HISTORY_MARKER;
    private static Marker MY_EXCEPTIONS;

    private static String dataFile = "src/main/resources/map.json";
    private static Scanner scanner;

    private static StationIndex stationIndex;

    public static void main(String[] args)
    {
        RouteCalculator calculator = getRouteCalculator();
        LOGGER = LogManager.getLogger(Main.class);
        INVALID_STATIONS_MARKER = MarkerManager.getMarker("INVALID_STATIONS");
        INPUT_HISTORY_MARKER = MarkerManager.getMarker("INPUT_HISTORY");
        MY_EXCEPTIONS = MarkerManager.getMarker("EXCEPTIONS");

        System.out.println("The program for calculating the routes of the St. Petersburg metro\n");
        scanner = new Scanner(System.in);
        for(;;)
        {
            try {
                Station from = takeStation("Enter the departure station:");
                LOGGER.info(INPUT_HISTORY_MARKER,"The user entered the departure station: {}", from);
                Station to = takeStation("Enter the destination station:");
                LOGGER.info(INPUT_HISTORY_MARKER,"The user entered the arrival station: {}",to);

                List<Station> route = calculator.getShortestRoute(from, to);
                System.out.println("Route:");
                printRoute(route);

                System.out.println("Duration: " +
                        RouteCalculator.calculateDuration(route) + " minutes");
                throw new Exception();
            } catch (Exception e)
            {
                LOGGER.info(MY_EXCEPTIONS,"An error has occurred: ",e);
            }
        }
    }

    private static RouteCalculator getRouteCalculator()
    {
        createStationIndex();
        return new RouteCalculator(stationIndex);
    }

    private static void printRoute(List<Station> route)
    {
        Station previousStation = null;
        for(Station station : route)
        {
            if(previousStation != null)
            {
                Line prevLine = previousStation.getLine();
                Line nextLine = station.getLine();
                if(!prevLine.equals(nextLine))
                {
                    System.out.println("\tTransfer to the station " +
                        station.getName() + " (" + nextLine.getName() + " line)");
                }
            }
            System.out.println("\t" + station.getName());
            previousStation = station;
        }
    }

    private static Station takeStation(String message)
    {
        for(;;)
        {
            System.out.println(message);
            String line = scanner.nextLine().trim();
            Station station = stationIndex.getStation(line);
            if(station != null) {
                return station;
            }
            System.out.println("Station not found :(");
            LOGGER.info(INVALID_STATIONS_MARKER,"The user entered: {}",line);
        }
    }

    private static void createStationIndex()
    {
        stationIndex = new StationIndex();
        try
        {
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(getJsonFile());

            JSONArray linesArray = (JSONArray) jsonData.get("lines");
            parseLines(linesArray);

            JSONObject stationsObject = (JSONObject) jsonData.get("stations");
            parseStations(stationsObject);

            JSONArray connectionsArray = (JSONArray) jsonData.get("connections");
            parseConnections(connectionsArray);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void parseConnections(JSONArray connectionsArray)
    {
        connectionsArray.forEach(connectionObject ->
        {
            JSONArray connection = (JSONArray) connectionObject;
            List<Station> connectionStations = new ArrayList<>();
            connection.forEach(item ->
            {
                JSONObject itemObject = (JSONObject) item;
                int lineNumber = ((Long) itemObject.get("line")).intValue();
                String stationName = (String) itemObject.get("station");

                Station station = stationIndex.getStation(stationName, lineNumber);
                if(station == null)
                {
                    throw new IllegalArgumentException("core.Station " +
                        stationName + " on line " + lineNumber + " not found");
                }
                connectionStations.add(station);
            });
            stationIndex.addConnection(connectionStations);
        });
    }

    private static void parseStations(JSONObject stationsObject)
    {
        stationsObject.keySet().forEach(lineNumberObject ->
        {
            int lineNumber = Integer.parseInt((String) lineNumberObject);
            Line line = stationIndex.getLine(lineNumber);
            JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);
            stationsArray.forEach(stationObject ->
            {
                Station station = new Station((String) stationObject, line);
                stationIndex.addStation(station);
                line.addStation(station);
            });
        });
    }

    private static void parseLines(JSONArray linesArray)
    {
        linesArray.forEach(lineObject -> {
            JSONObject lineJsonObject = (JSONObject) lineObject;
            Line line = new Line(
                    ((Long) lineJsonObject.get("number")).intValue(),
                    (String) lineJsonObject.get("name")
            );
            stationIndex.addLine(line);
        });
    }

    private static String getJsonFile()
    {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(dataFile));
            lines.forEach(line -> builder.append(line));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }
}
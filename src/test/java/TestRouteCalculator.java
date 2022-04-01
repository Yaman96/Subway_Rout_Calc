import core.Line;
import core.Station;
import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class TestRouteCalculator extends TestCase
{
    private static StationIndex stationIndex;
    List<Station> route;
    private RouteCalculator routeCalculator;

    @Override
    protected void setUp() throws Exception
    {
        route = new ArrayList<>();
        Line line1 = new Line(1, "Первая");
        Line line2 = new Line(2, "Вторая");
        route.add(new Station("Площадь Маркса", line1));
        route.add(new Station("Студенческая", line1));
        route.add(new Station("Речной вокзал", line1));
        route.add(new Station("Березовая роща", line2));
        route.add(new Station("Площадь Маркса", line2));

        stationIndex = new StationIndex();
//создаем 3 ветки метро
        line1 = new Line(1, "Первая");
        line2 = new Line(2, "Вторая");
        Line line3 = new Line(3, "Третья");
//добавляем ветки в обобщенный класс
        stationIndex.addLine(line1);
        stationIndex.addLine(line2);
        stationIndex.addLine(line3);
//добавляем 6 станции
        Station from = new Station("Чернышевская", line1);
        Station sl1 = new Station("Площадь Восстания", line1);
        Station sl2 = new Station("Маяковская", line3);
        Station sl3 = new Station("Гостиный двор", line3);
        Station sl4 = new Station("Невский проспект", line2);
        Station to = new Station("Горьковская", line2);
//добавляем станции на ветки метро
        line1.addStation(from);
        line1.addStation(sl1);
        line3.addStation(sl2);
        line3.addStation(sl3);
        line2.addStation(sl4);
        line2.addStation(to);
//добавляем станции в обобщенный класс
        stationIndex.addStation(from);
        stationIndex.addStation(sl1);
        stationIndex.addStation(sl2);
        stationIndex.addStation(sl3);
        stationIndex.addStation(sl4);
        stationIndex.addStation(to);
//создаем 2 соединения веток на определенных станциях
        List<Station> cs1 = new ArrayList<>();
        cs1.add(stationIndex.getStation(sl1.getName(), sl1.getLine().getNumber()));
        cs1.add(stationIndex.getStation(sl2.getName(), sl2.getLine().getNumber()));
        List<Station> cs2 = new ArrayList<>();
        cs2.add(stationIndex.getStation(sl3.getName(), sl3.getLine().getNumber()));
        cs2.add(stationIndex.getStation(sl4.getName(), sl4.getLine().getNumber()));
//добавляем соединнения в обобщенный клас
        stationIndex.addConnection(cs1);
        stationIndex.addConnection(cs2);
//создаем экземпляр класса расчета маршрута с нашим собранным обобщеным классом
        routeCalculator = new RouteCalculator(stationIndex);
    }

    public void testGetShortestRoute()
    {
        List<Station> expectedList = new ArrayList<>();
        expectedList.add(stationIndex.getStation("Чернышевская"));
        expectedList.add(stationIndex.getStation("Площадь Восстания"));
        expectedList.add(stationIndex.getStation("Маяковская"));
        expectedList.add(stationIndex.getStation("Гостиный двор"));
        expectedList.add(stationIndex.getStation("Невский проспект"));
        expectedList.add(stationIndex.getStation("Горьковская"));

        List<Station> actualList = routeCalculator.getShortestRoute(stationIndex.getStation("Чернышевская",1), stationIndex.getStation("Горьковская",2));
        System.out.println(actualList);
        boolean isOk = true;
        for (int i = 0 ; i < expectedList.size() ; i++)
        {
            if (!expectedList.get(i).equals(actualList.get(i))) {
                isOk = false;
                break;
            }
        }
        assertTrue(isOk);
    }
    public void testCalculateDuration()
    {
        double expected = 11.0;
        double actual = RouteCalculator.calculateDuration(route);
        assertEquals(expected,actual);
    }

    @Override
    protected void tearDown() throws Exception
    {
    }
}

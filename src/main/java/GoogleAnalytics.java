import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.model.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/ga")
public class GoogleAnalytics {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String KEY_FILE_NAME = "front-end-project-a7897-6edf8a4e78c3.json";
    private static final String VIEW_ID = "192085271";

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public static String main() {
        String result ="############################\n";
        result += "##### Google Analytics #####\n";
        result +="############################\n";
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();
            GetReportsResponse response = getReportAllLabels(service);
            result+="\n\nTotal events :\n";
            result+="----------------------------";
            result+= printResponse(response);

            response = getAverageReportAllLabels(service);
            result+="\n\nAverage events :\n";
            result+="----------------------------";
            result+= printResponse(response);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "finish";
    }

    /**
     * Initializes an Analytics Reporting API V4 service object.
     *
     * @return An authorized Analytics Reporting API V4 service object.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = GoogleCredential
                .fromStream(GoogleAnalytics.class.getResourceAsStream(KEY_FILE_NAME))
                .createScoped(AnalyticsReportingScopes.all());

        // Construct the Analytics Reporting service object.
        return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("api-front-end").build();
    }

    /**
     * Queries the Analytics Reporting API V4.
     *
     * @param service An authorized Analytics Reporting API V4 service object.
     * @return GetReportResponse The Analytics Reporting API V4 response.
     * @throws IOException
     */
    private static GetReportsResponse getReportAllLabels(AnalyticsReporting service) throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate("2019-03-01");
        dateRange.setEndDate("today");

        // Create the Metrics object.
        Metric sessions = new Metric()
                .setExpression("ga:totalEvents")
                .setAlias("events");

        Dimension pageTitle = new Dimension().setName("ga:eventLabel");

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(VIEW_ID)
                .setDateRanges(Arrays.asList(dateRange))
                .setMetrics(Arrays.asList(sessions))
                .setDimensions(Arrays.asList(pageTitle));

        ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(requests);

        // Call the batchGet method.
        GetReportsResponse response = service.reports().batchGet(getReport).execute();

        // Return the response.
        return response;
    }

    private static GetReportsResponse getAverageReportAllLabels(AnalyticsReporting service) throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate("2019-03-01");
        dateRange.setEndDate("today");

        // Create the Metrics object.
        Metric sessions = new Metric()
                .setExpression("ga:avgEventValue")
                .setAlias("events");

        Dimension pageTitle = new Dimension().setName("ga:eventLabel");

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(VIEW_ID)
                .setDateRanges(Arrays.asList(dateRange))
                .setMetrics(Arrays.asList(sessions))
                .setDimensions(Arrays.asList(pageTitle));

        ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(requests);

        // Call the batchGet method.
        GetReportsResponse response = service.reports().batchGet(getReport).execute();

        // Return the response.
        return response;
    }

    /**
     * Parses and prints the Analytics Reporting API V4 response.
     *
     * @param response An Analytics Reporting API V4 response.
     */
    private static String printResponse(GetReportsResponse response) {
        StringBuilder result = new StringBuilder();
        for (Report report : response.getReports()) {
            ColumnHeader header = report.getColumnHeader();
            List<String> dimensionHeaders = header.getDimensions();
            List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
            List<ReportRow> rows = report.getData().getRows();

            if (rows == null) {
                result.append(("No data found for " + VIEW_ID) + "\n");
                return result.toString();
            }

            for (ReportRow row : rows) {
                result.append("\n");
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();

                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    result.append(dimensionHeaders.get(i)).append(": ").append(dimensions.get(i)).append(" ");
                }

                for (int j = 0; j < metrics.size(); j++) {
                    result.append("Date Range (").append(j).append("): ").append(" ");
                    DateRangeValues values = metrics.get(j);
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        result.append(metricHeaders.get(k).getName()).append(": ").append(values.getValues().get(k)).append(" ");
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Parses and prints the Analytics Reporting API V4 response.
     *
     * @param response An Analytics Reporting API V4 response.
     */
    private static ArrayList<Events> getEventsList(GetReportsResponse response) {
        ArrayList<Events> eventsArrayList = new ArrayList<Events>();
        for (Report report : response.getReports()) {
            ColumnHeader header = report.getColumnHeader();
//            List<String> dimensionHeaders = header.getDimensions();
            List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
            List<ReportRow> rows = report.getData().getRows();

            if (rows == null) {
                System.out.println("No data found for " + VIEW_ID);
                return eventsArrayList;
            }

            for (ReportRow row : rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();

                for (DateRangeValues values : metrics) {
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        eventsArrayList.add(new Events(dimensions.get(0), Integer.valueOf(values.getValues().get(k))));
                    }
                }
            }
        }
        return eventsArrayList;
    }

    @GET
    @Path("/images-display")
    @Produces(MediaType.TEXT_PLAIN)
    public String getNumberImagesDisplay(){
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();
            GetReportsResponse response = getReportAllLabels(service);
            ArrayList<Events> events = getEventsList(response);
            int value = 0;
            for (Events event : events) {
                if (event.name.equals("falseAnswer") || event.name.equals("goodAnswer")) {
                    value += event.value;
                }
            }
            return String.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @GET
    @Path("/false-answer")
    @Produces(MediaType.TEXT_PLAIN)
    public String getNumberFalseAnswer(){
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();
            GetReportsResponse response = getReportAllLabels(service);
            ArrayList<Events> events = getEventsList(response);
            int value = 0;
            for (Events event : events) {
                if (event.name.equals("falseAnswer")) {
                    value += event.value;
                }
            }
            return String.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @GET
    @Path("/good-answer")
    @Produces(MediaType.TEXT_PLAIN)
    public String getNumberGoodAnswer(){
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();
            GetReportsResponse response = getReportAllLabels(service);
            ArrayList<Events> events = getEventsList(response);
            int value = 0;
            for (Events event : events) {
                if (event.name.equals("goodAnswer")) {
                    value += event.value;
                }
            }
            return String.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @GET
    @Path("/percent-good-answer")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPercentGoodAnswer(){
        return String.valueOf(Integer.valueOf(getNumberGoodAnswer())*100/Integer.valueOf(getNumberImagesDisplay()));
    }

    @GET
    @Path("/average-find-it")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAverageFindIt(){
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();
            GetReportsResponse response = getAverageReportAllLabels(service);
            ArrayList<Events> events = getEventsList(response);
            int value = 0;
            for (Events event : events) {
                if (event.name.equals("findIn")) {
                    value += event.value;
                }
            }
            return String.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }
}

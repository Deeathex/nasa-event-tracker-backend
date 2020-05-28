package NASA.service;

import NASA.model.Category;
import NASA.model.Event;
import NASA.model.Geometry;
import NASA.model.Source;
import NASA.model.enums.EventStatus;
import NASA.model.enums.QueryParameterType;
import NASA.utils.JsonParser;
import NASA.utils.URLManipulator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/***
 * This API consumer was implemented by this documentation https://eonet.sci.gsfc.nasa.gov/docs/v2.1
 * and uses NASA's api Earth Observatory Natural Event Tracker  (EONET)
 */
@Service
public class APIConsumer {
    private static final Logger LOG = LogManager.getLogger(APIConsumer.class.getName());

    private static final String SLASH = "/";
    private static final String EVENTS = "events";
    private static final String CATEGORIES = "categories";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LINK = "link";
    private static final String SOURCES = "sources";
    private static final String GEOMETRIES = "geometries";
    private static final String CLOSED = "closed";

    private static final String URL_BASE = "https://eonet.sci.gsfc.nasa.gov/api/v2.1";
    private static final String URL_BASE_EVENTS = URL_BASE + SLASH + EVENTS;
    private static final String URL_BASE_CATEGORIES = URL_BASE + SLASH + CATEGORIES;

    private final RestTemplate restTemplate;
    private final JsonParser jsonParser = new JsonParser();

    public APIConsumer() {
        this.restTemplate = new RestTemplate();
    }

    /***
     *
     * @param status the status of the event: open, closed, all
     * @return the list with open/closed/all events
     */
    public List<Event> getAllEvents(EventStatus status, long affectedPlacesNo) {
        List<Event> events = new ArrayList<>();

        if (EventStatus.all.equals(status)) {
            events.addAll(getAllEventsWithStatus(EventStatus.open));
            events.addAll(getAllEventsWithStatus(EventStatus.closed));
        } else {
            events = getAllEventsWithStatus(status);
        }

        return getEventsByAffectedPlacesNumber(events, affectedPlacesNo);
    }

    public List<Event> getAllEventsWithStatus(EventStatus status) {
        Map<QueryParameterType, String> queryParams = new HashMap<>();
        queryParams.put(QueryParameterType.status, status.toString());

        URLManipulator urlManipulator = new URLManipulator(URL_BASE_EVENTS, queryParams);
        String json = getNasaJsonResponse(urlManipulator.getUrlWithQueryParams());

        List<Event> events = new ArrayList<>();
        try {
            events = getEventsFromJson(json);
        } catch (JsonProcessingException e) {
            LOG.error("Error parsing events json " + e.getMessage());
        } catch (ParseException e) {
            LOG.error("Error parsing date from json " + e.getMessage());
        }

        return events;
    }

    /***
     *
     * @param status is the event status: open, closed, all
     * @param days Limit the number of prior days (including today) from which events will be returned
     * @return the list with all Events that are either open, or closed limiting the number of prior days
     * (including today) from which events will be returned
     */
    public List<Event> getAllEvents(EventStatus status, long days, long affectedPlacesNo) {
        List<Event> events = new ArrayList<>();

        if (EventStatus.all.equals(status)) {
            events.addAll(getAllEventsInRangeWithStatus(EventStatus.open, days));
            events.addAll(getAllEventsInRangeWithStatus(EventStatus.closed, days));
            return getEventsByAffectedPlacesNumber(events, affectedPlacesNo);
        }

        events = getAllEventsInRangeWithStatus(status, days);
        return getEventsByAffectedPlacesNumber(events, affectedPlacesNo);
    }

    private List<Event> getAllEventsInRangeWithStatus(EventStatus status, long days) {
        Map<QueryParameterType, String> queryParams = new HashMap<>();
        queryParams.put(QueryParameterType.status, status.toString());
        queryParams.put(QueryParameterType.days, String.valueOf(days));

        URLManipulator urlManipulator = new URLManipulator(URL_BASE_EVENTS, queryParams);
        String json = getNasaJsonResponse(urlManipulator.getUrlWithQueryParams());

        List<Event> events = new ArrayList<>();
        try {
            events = getEventsFromJson(json);
        } catch (JsonProcessingException e) {
            LOG.error("Error parsing events json " + e.getMessage());
        } catch (ParseException e) {
            LOG.error("Error parsing date from json " + e.getMessage());
        }

        return events;
    }

    /***
     *
     * @return all available categories
     */
    public List<Category> getAllCategories() {
        String json = getNasaJsonResponse(URL_BASE_CATEGORIES);

        List<Category> categories = new ArrayList<>();
        try {
            categories = getCategoriesFromJson(json);
        } catch (JsonProcessingException e) {
            LOG.error("Error parsing categories json " + e.getMessage());
        }

        return categories;
    }

    public List<Event> getAllEventsFromCategory(int categoryId, EventStatus status, long priorDays, long affectedPlacesNo) {
        String additionalQueries = "";
        if (priorDays != 0) {
            additionalQueries += "&days=" + priorDays;
        }

        String json;
        List<Event> events = new ArrayList<>();
        if (EventStatus.all.equals(status)) {
            json = getNasaJsonResponse(URL_BASE_CATEGORIES + SLASH + categoryId + "?status=" + EventStatus.open + additionalQueries);

            try {
                events = getEventsFromJson(json);
            } catch (JsonProcessingException e) {
                LOG.error("Error parsing events json " + e.getMessage());
            } catch (ParseException e) {
                LOG.error("Error parsing date from json " + e.getMessage());
            }

            json = getNasaJsonResponse(URL_BASE_CATEGORIES + SLASH + categoryId + "?status=" + EventStatus.closed + additionalQueries);

            try {
                events.addAll(getEventsFromJson(json));
            } catch (JsonProcessingException e) {
                LOG.error("Error parsing events json " + e.getMessage());
            } catch (ParseException e) {
                LOG.error("Error parsing date from json " + e.getMessage());
            }
            return getEventsByAffectedPlacesNumber(events, affectedPlacesNo);
        }

        json = getNasaJsonResponse(URL_BASE_CATEGORIES + SLASH + categoryId + "?status=" + status + additionalQueries);

        try {
            events = getEventsFromJson(json);
        } catch (JsonProcessingException e) {
            LOG.error("Error parsing events json " + e.getMessage());
        } catch (ParseException e) {
            LOG.error("Error parsing date from json " + e.getMessage());
        }

        return getEventsByAffectedPlacesNumber(events, affectedPlacesNo);
    }

    private String getNasaJsonResponse(String url) {
        return restTemplate.getForObject(url, String.class);
    }

    private List<Event> getEventsFromJson(String json) throws JsonProcessingException, ParseException {
        JsonNode eventsNode = jsonParser.getJsonNodeFrom(json, EVENTS);

        List<Event> events = new ArrayList<>();
        for (Iterator<JsonNode> eventsIterator = eventsNode.elements(); eventsIterator.hasNext(); ) {
            JsonNode eventNode = eventsIterator.next();
            String eventId = eventNode.get(ID).asText();
            String eventTitle = eventNode.get(TITLE).asText();
            String eventDescription = eventNode.get(DESCRIPTION).asText();
            String eventLink = eventNode.get(LINK).asText();

            JsonNode categoriesNode = eventNode.get(CATEGORIES);
            List<Category> categories = jsonParser.getCategoriesFromJsonNode(categoriesNode);

            JsonNode sourcesNode = eventNode.get(SOURCES);
            List<Source> sources = jsonParser.getSourcesFromJsonNode(sourcesNode);


            JsonNode geometriesNode = eventNode.get(GEOMETRIES);
            List<Geometry> geometries = jsonParser.getGeometriesFromJsonNode(geometriesNode);

            Date closedDate = null;
            if (eventNode.has(CLOSED)) {
                closedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(eventNode.get("closed").asText());
            }

            Event event = new Event();
            event.setId(eventId);
            event.setTitle(eventTitle);
            event.setDescription(eventDescription);
            event.setLink(eventLink);
            event.setCategories(categories);
            event.setSources(sources);
            event.setGeometries(geometries);
            event.setClosed(closedDate);
            events.add(event);
        }

        return events;
    }

    private List<Category> getCategoriesFromJson(String json) throws JsonProcessingException {
        JsonNode categoriesNode = jsonParser.getJsonNodeFrom(json, CATEGORIES);
        return jsonParser.getCategoriesFromJsonNode(categoriesNode);
    }

    private List<Event> getEventsByAffectedPlacesNumber(List<Event> events, long affectedPlacesNo) {
        if (affectedPlacesNo == 0) {
            return events;
        }

        for (int i = 0; i < events.size(); i++) {
            List<Geometry> geometries = events.get(i).getGeometries();
            if (geometries != null) {
                if (geometries.size() < affectedPlacesNo) {
                    events.remove(i);
                    i--;
                }
            }
        }

        return events;
    }
}

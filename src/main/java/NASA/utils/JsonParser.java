package NASA.utils;

import NASA.model.Category;
import NASA.model.Coordinates;
import NASA.model.Geometry;
import NASA.model.Source;
import NASA.model.enums.GeometryType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class JsonParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<Category> getCategoriesFromJsonNode(JsonNode jsonNode) {
        List<Category> categories = new ArrayList<>();
        for (Iterator<JsonNode> categoriesIterator = jsonNode.elements(); categoriesIterator.hasNext(); ) {
            JsonNode categoryNode = categoriesIterator.next();
            Category category = new Category();

            int categoryId = categoryNode.get("id").asInt();
            category.setId(categoryId);

            String categoryTitle = categoryNode.get("title").asText();
            category.setTitle(categoryTitle);

            if (categoryNode.has("description")) {
                String categoryDescription = categoryNode.get("description").asText();
                category.setDescription(categoryDescription);
            }

            if (categoryNode.has("link")) {
                String categoryLink = categoryNode.get("link").asText();
                category.setLink(categoryLink);
            }

            categories.add(category);
        }

        return categories;
    }

    public List<Source> getSourcesFromJsonNode(JsonNode jsonNode) throws JsonProcessingException {
        List<Source> sources = new ArrayList<>();
        for (Iterator<JsonNode> sourcesIterator = jsonNode.elements(); sourcesIterator.hasNext(); ) {
            JsonNode sourceNode = sourcesIterator.next();
            sources.add(MAPPER.treeToValue(sourceNode, Source.class));
        }

        return sources;
    }

    public List<Geometry> getGeometriesFromJsonNode(JsonNode jsonNode) throws ParseException {
        List<Geometry> geometries = new ArrayList<>();
        for (Iterator<JsonNode> geometriesIterator = jsonNode.elements(); geometriesIterator.hasNext(); ) {
            JsonNode geometryNode = geometriesIterator.next();
            Geometry geometry = new Geometry();

            if (geometryNode.has("id")) {
                int geometryId = geometryNode.get("id").asInt();
                geometry.setId(geometryId);
            }

            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(geometryNode.get("date").asText());
            geometry.setDate(date);

            GeometryType geometryType = GeometryType.valueOf(geometryNode.get("type").asText());
            geometry.setType(geometryType);

            JsonNode coordinatesNode = geometryNode.get("coordinates");
            if (geometryType.equals(GeometryType.Point)) {
                double latitude = coordinatesNode.get(0).asDouble();
                double longitude = coordinatesNode.get(1).asDouble();
                Coordinates coordinates = new Coordinates(latitude, longitude);
                geometry.setCoordinates(coordinates);
            }

            geometries.add(geometry);
        }

        return geometries;
    }

    public JsonNode getJsonNodeFrom(String rawJson, String type) throws JsonProcessingException {
        JsonNode actualObject = MAPPER.readTree(rawJson);
        return actualObject.get(type);
    }
}

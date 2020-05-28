package NASA.controller;

import NASA.model.Event;
import NASA.model.enums.EventStatus;
import NASA.service.APIConsumer;
import NASA.service.EventsGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@CrossOrigin
@RestController
public class NasaEventsController {
    private static final Logger LOG = LogManager.getLogger(NasaEventsController.class.getName());

    private final APIConsumer service;

    public NasaEventsController(APIConsumer service) {
        this.service = service;
    }

    @GetMapping("/events")
    public ResponseEntity<?> getAllEvents(@RequestParam EventStatus status, @RequestParam long priorDays, @RequestParam long affectedPlacesNo) {
        LOG.info("User requests " + status + " events within " + priorDays + " days and with number of affected places equals to: " + affectedPlacesNo);
        if (priorDays == 0) {
            return new ResponseEntity<>(service.getAllEvents(status, affectedPlacesNo), HttpStatus.OK);
        }
        return new ResponseEntity<>(service.getAllEvents(status, priorDays, affectedPlacesNo), HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        LOG.info("User requests all categories.");
        return new ResponseEntity<>(service.getAllCategories(), HttpStatus.OK);
    }

    @GetMapping("/categories/{category-id}/events")
    public ResponseEntity<?> getAllEventsWithinCategory(@PathVariable("category-id") int categoryId,
                                                        @RequestParam EventStatus status,
                                                        @RequestParam long priorDays,
                                                        @RequestParam long affectedPlacesNo) {
        LOG.info("User requests all events within category with id " + categoryId + ", with number of affected places equals to: " + affectedPlacesNo
                + ", with prior days: " + priorDays + " and status: " + status);

        return new ResponseEntity<>(service.getAllEventsFromCategory(categoryId, status, priorDays, affectedPlacesNo), HttpStatus.OK);
    }

    @GetMapping(value = "/stream/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> getEventsStream() {
        EventsGenerator eventsGenerator = new EventsGenerator(service.getAllCategories(), service.getAllEventsWithStatus(EventStatus.open));
        return eventsGenerator.getEvents();
    }
}

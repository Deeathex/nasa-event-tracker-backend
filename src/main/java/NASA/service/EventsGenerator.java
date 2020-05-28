package NASA.service;

import NASA.model.Category;
import NASA.model.Event;
import NASA.model.Geometry;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class EventsGenerator {
    private final List<Category> categories;
    private List<Event> events;
    private final List<Event> eventsBackup;

    public EventsGenerator(List<Category> categories, List<Event> events) {
        this.categories = categories;
        this.events = events;
        this.eventsBackup = events;
    }

    public Flux<Event> generateEvents() {
        return Flux.create(fluxSink -> {
            int i = 0;
            while (true) {
                if (i == events.size()) {
                    i = 0;
                }
                Event event = events.get(i++);
                Date currentDateTime = new Date(System.currentTimeMillis());
                for (Geometry geometry : event.getGeometries()) {
                    geometry.setDate(currentDateTime);
                }
                fluxSink.next(event);
            }
        });
    }

    public Publisher<Event> publishEvents() {
        return Flux.generate(sink -> {
            Event event = events.remove(events.size() - 1);
            Date currentDateTime = new Date(System.currentTimeMillis());
            for (Geometry geometry : event.getGeometries()) {
                geometry.setDate(currentDateTime);
            }
            sink.next(event);
        });
    }

    public Flux<Event> getEvents() {
        Flux<Event> eventFlux = Flux.fromStream(Stream.generate(() -> {
            Event event = new Event();
            if (!events.isEmpty()) {
                event = events.remove(events.size() - 1);
//                Date currentDateTime = new Date(System.currentTimeMillis());
//                for (Geometry geometry : event.getGeometries()) {
//                    geometry.setDate(currentDateTime);
//                }
            } else {
                events = eventsBackup;
            }
            return event;
        }));
        Flux<Long> durationFlux = Flux.interval(Duration.ofMillis(500));
        return Flux.zip(eventFlux, durationFlux).map(Tuple2::getT1);
    }
}

package com.merkle.oss.magnolia.content.indexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.magnolia.test.mock.jcr.MockEvent;
import info.magnolia.test.mock.jcr.MockEventIterator;

import java.util.Set;

import javax.jcr.observation.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventFilterTest {
    private EventFilter eventFilter;

    @BeforeEach
    void setUp() {
        eventFilter = new EventFilter();
    }

    @Test
    void remove() {
        assertEquals(
                Set.of(
                        new RemoveNodeEvent("1")
                ),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        // remove
                        new RemoveNodeEvent("1")
                ))
        );
    }

    @Test
    void moveAndRemove() {
        assertEquals(
                Set.of(
                        new RemoveNodeEvent("1")
                ),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        // move
                        new RemoveNodeEvent("1"),
                        new MoveNodeEvent("1"),
                        new AddNodeEvent("1"),
                        // remove
                        new RemoveNodeEvent("1")
                ))
        );
    }

    @Test
    void move() {
        assertEquals(
                Set.of(
                        new MoveNodeEvent("1")
                ),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        // move
                        new RemoveNodeEvent("1"),
                        new MoveNodeEvent("1"),
                        new AddNodeEvent("1")
                ))
        );
    }

    @Test
    void moveAndMove() {
        assertEquals(
                Set.of(
                        new MoveNodeEvent("1")
                ),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        new RemoveNodeEvent("1"),
                        new MoveNodeEvent("1"),
                        new AddNodeEvent("1"),

                        new RemoveNodeEvent("1"),
                        new MoveNodeEvent("1"),
                        new AddNodeEvent("1")
                ))
        );
    }

    @Test
    void addAndRemove() {
        assertEquals(
                Set.of(),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        new AddNodeEvent("1"),
                        new RemoveNodeEvent("1")
                ))
        );
    }

    @Test
    void add() {
        assertEquals(
                Set.of(
                        new AddNodeEvent("1")
                ),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        //add
                        new AddNodeEvent("1")
                ))
        );
    }

    @Test
    void addAndMoveAndRemove() {
        assertEquals(
                Set.of(),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        // add
                        new AddNodeEvent("1"),
                        // move
                        new RemoveNodeEvent("1"),
                        new MoveNodeEvent("1"),
                        new AddNodeEvent("1"),
                        // remove
                        new RemoveNodeEvent("1")
                ))
        );
    }

    @Test
    void addAndMoveAndMove() {
        assertEquals(
                Set.of(
                        new AddNodeEvent("1")
                ),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        // add
                        new AddNodeEvent("1"),

                        // move
                        new RemoveNodeEvent("1"),
                        new MoveNodeEvent("1"),
                        new AddNodeEvent("1"),

                        // move
                        new RemoveNodeEvent("1"),
                        new MoveNodeEvent("1"),
                        new AddNodeEvent("1")
                ))
        );
    }

    @Test
    void propertyChange() {
        assertEquals(
                Set.of(
                        new ChangePropertyEvent("1")
                ),
                eventFilter.getFilteredEvents(new MockEventIterator(
                        new ChangePropertyEvent("1")
                ))
        );
    }

    public static class AddNodeEvent extends AbstractNodeEvent {
        public AddNodeEvent(final String identifier) {
            super(Event.NODE_ADDED, identifier);
        }
    }
    public static class RemoveNodeEvent extends AbstractNodeEvent {
        public RemoveNodeEvent(final String identifier) {
            super(Event.NODE_REMOVED, identifier);
        }
    }
    public static class MoveNodeEvent extends AbstractNodeEvent {
        public MoveNodeEvent(final String identifier) {
            super(Event.NODE_MOVED, identifier);
        }
    }
    public static class ChangePropertyEvent extends AbstractNodeEvent {
        public ChangePropertyEvent(final String identifier) {
            super(Event.PROPERTY_CHANGED, identifier);
        }
    }
    public static class AbstractNodeEvent extends MockEvent {
        public AbstractNodeEvent(final int type, final String identifier) {
            setType(type);
            setIdentifier(identifier);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AbstractNodeEvent event)) return false;
            if (!getIdentifier().equals(event.getIdentifier())) return false;
            return getType() == event.getType();
        }

        @Override
        public int hashCode() {
            int result = getIdentifier().hashCode();
            result = 31 * result + Integer.hashCode(getType());
            return result;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "id='" + getIdentifier() + '\'' +
                    ", type='" + getType() + '\'' +
                    '}';
        }
    }
}

package com.eventwise;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;

import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.adapters.EventAdapter;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.NotificationDatabaseManager;
import com.eventwise.database.SessionStore;
import com.eventwise.fragments.EntrantEventsCommunityFragment;
import com.google.android.gms.tasks.Tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 34)

/**
 * Unit Tests for EntrantEventsCommunityFragment
 *
 * @author Luke Forster
 * @version 1.0
 * @since 2026-04-06
 */
public class EntrantEventsCommunityFragmentUnitTest {

    private FragmentActivity activity;
    private EntrantEventsCommunityFragment fragment;
    private EventAdapter mockAdapter;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(FragmentActivity.class).setup().get();

        FrameLayout container = new FrameLayout(activity);
        container.setId(R.id.entrant_fragment_container);
        activity.setContentView(container);

        try (MockedConstruction<EventSearcherDatabaseManager> ignored =
                     mockConstruction(EventSearcherDatabaseManager.class,
                             (mock, context) -> when(mock.getFilteredEvents(any()))
                                     .thenReturn(Tasks.forResult(new ArrayList<>())))
        ) {
            fragment = new EntrantEventsCommunityFragment();

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.entrant_fragment_container, fragment)
                    .commitNow();

            shadowOf(Looper.getMainLooper()).idle();
        }

        mockAdapter = mock(EventAdapter.class);
        setPrivateField(fragment, "eventAdapter", mockAdapter);
    }

    @Test
    public void joinEvent_privateEvent_returnsEarly() {
        Event event = mock(Event.class);
        when(event.isPrivateEvent()).thenReturn(true);

        try (MockedConstruction<EntrantDatabaseManager> ignoredDb =
                     mockConstruction(EntrantDatabaseManager.class);
             MockedConstruction<SessionStore> ignoredSession =
                     mockConstruction(SessionStore.class)
        ) {
            fragment.joinEvent(event);
            shadowOf(Looper.getMainLooper()).idle();

            verify(mockAdapter, never()).notifyDataSetChanged();
            assertEquals(0, ignoredDb.constructed().size());
        }
    }

    @Test
    public void joinEvent_missingEntrantId_returnsEarly() {
        Event event = mock(Event.class);
        when(event.isPrivateEvent()).thenReturn(false);

        try (
                MockedConstruction<EventSearcherDatabaseManager> ignoredSearcher =
                        mockConstruction(EventSearcherDatabaseManager.class,
                                (mock, context) -> when(mock.getFilteredEvents(any()))
                                        .thenReturn(Tasks.forResult(new ArrayList<>())));

                MockedConstruction<SessionStore> mockedSession =
                     mockConstruction(SessionStore.class, (mock, context) ->
                             when(mock.getEntrantProfileId()).thenReturn("   "));
             MockedConstruction<EntrantDatabaseManager> mockedDb =
                     mockConstruction(EntrantDatabaseManager.class)
        ) {
            fragment.joinEvent(event);
            shadowOf(Looper.getMainLooper()).idle();

            verify(mockAdapter, never()).notifyDataSetChanged();
            assertEquals(0, mockedDb.constructed().size());
        }
    }

    @Test
    public void joinEvent_alreadyWaitlisted_unregistersEntrant() {
        Event event = mock(Event.class);
        when(event.isPrivateEvent()).thenReturn(false);

        when(event.getEntrantIdsByStatus(any())).thenReturn(new ArrayList<>());
        when(event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED))
                .thenReturn(new ArrayList<>(Collections.singletonList("entrant-1")));

        when(event.getEventId()).thenReturn("event-1");
        when(event.getName()).thenReturn("Spring Gala");
        when(event.getOrganizerProfileId()).thenReturn("org-1");

        try (
                MockedConstruction<EventSearcherDatabaseManager> ignoredSearcher =
                        mockConstruction(EventSearcherDatabaseManager.class,
                                (mock, context) -> when(mock.getFilteredEvents(any()))
                                        .thenReturn(Tasks.forResult(new ArrayList<>())));
                MockedConstruction<SessionStore> mockedSession =
                     mockConstruction(SessionStore.class, (mock, context) ->
                             when(mock.getEntrantProfileId()).thenReturn("entrant-1"));
             MockedConstruction<EntrantDatabaseManager> mockedDb =
                     mockConstruction(EntrantDatabaseManager.class, (mock, context) ->
                             when(mock.unregisterEntrantInEvent(eq("entrant-1"), eq("event-1"), anyLong()))
                                     .thenReturn(Tasks.forResult(null)));
             MockedConstruction<NotificationDatabaseManager> mockedNotif =
                     mockConstruction(NotificationDatabaseManager.class, (mock, context) ->
                             when(mock.createNotification(any(Notification.class)))
                                     .thenReturn(Tasks.forResult(null)))
        ) {
            fragment.joinEvent(event);
            shadowOf(Looper.getMainLooper()).idle();

            assertEquals(1, mockedDb.constructed().size());
            EntrantDatabaseManager db = mockedDb.constructed().get(0);

            verify(event).addOrUpdateEntrantStatus(
                    eq("entrant-1"),
                    eq(EventEntrantStatus.LEFT_WAITLIST),
                    anyLong()
            );

            verify(db).unregisterEntrantInEvent(eq("entrant-1"), eq("event-1"), anyLong());
            verify(mockAdapter, atLeastOnce()).notifyDataSetChanged();

            assertEquals(1, mockedNotif.constructed().size());
            NotificationDatabaseManager notifDb = mockedNotif.constructed().get(0);
            verify(notifDb, times(2)).createNotification(any(Notification.class));
        }
    }

    @Test
    public void joinEvent_publicNonGeo_registersEntrant() {
        Event event = mock(Event.class);
        when(event.isPrivateEvent()).thenReturn(false);

        when(event.getEntrantIdsByStatus(any())).thenReturn(new ArrayList<>());

        when(event.isGeolocationRequired()).thenReturn(false);
        when(event.getEventId()).thenReturn("event-2");
        when(event.getName()).thenReturn("Art Night");
        when(event.getOrganizerProfileId()).thenReturn("org-2");

        try (    MockedConstruction<EventSearcherDatabaseManager> ignoredSearcher =
                         mockConstruction(EventSearcherDatabaseManager.class,
                                 (mock, context) -> when(mock.getFilteredEvents(any()))
                                         .thenReturn(Tasks.forResult(new ArrayList<>())));

                 MockedConstruction<SessionStore> mockedSession =
                     mockConstruction(SessionStore.class, (mock, context) ->
                             when(mock.getEntrantProfileId()).thenReturn("entrant-2"));
             MockedConstruction<EntrantDatabaseManager> mockedDb =
                     mockConstruction(EntrantDatabaseManager.class, (mock, context) ->
                             when(mock.registerEntrantInEvent(eq("entrant-2"), eq("event-2"), anyLong(), isNull()))
                                     .thenReturn(Tasks.forResult(null)));
             MockedConstruction<NotificationDatabaseManager> mockedNotif =
                     mockConstruction(NotificationDatabaseManager.class, (mock, context) ->
                             when(mock.createNotification(any(Notification.class)))
                                     .thenReturn(Tasks.forResult(null)))
        ) {
            fragment.joinEvent(event);
            shadowOf(Looper.getMainLooper()).idle();

            assertEquals(1, mockedDb.constructed().size());
            EntrantDatabaseManager db = mockedDb.constructed().get(0);

            verify(event).addOrUpdateEntrantStatus(
                    eq("entrant-2"),
                    eq(EventEntrantStatus.WAITLISTED),
                    anyLong()
            );

            verify(db).registerEntrantInEvent(eq("entrant-2"), eq("event-2"), anyLong(), isNull());
            verify(mockAdapter, atLeastOnce()).notifyDataSetChanged();

            assertEquals(1, mockedNotif.constructed().size());
            NotificationDatabaseManager notifDb = mockedNotif.constructed().get(0);
            verify(notifDb, times(2)).createNotification(any(Notification.class));
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
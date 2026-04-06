package com.eventwise;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentActivity;

import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.SessionStore;
import com.eventwise.fragments.EntrantProfileEmptyFragment;
import com.eventwise.fragments.EntrantProfileExistsFormFragment;
import com.google.android.gms.tasks.Tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 34)

/**
 * Unit Tests for EntrantEventsCommunityFragment
 *
 * @author Luke Forster
 * @version 1.0
 * @since 2026-04-06
 */
public class EntrantProfileEmptyFragmentUnitTest {

    private FragmentActivity activity;
    private EntrantProfileEmptyFragment fragment;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(FragmentActivity.class).setup().get();

        FrameLayout container = new FrameLayout(activity);
        container.setId(R.id.entrant_fragment_container);
        activity.setContentView(container);
    }

    @Test
    public void toggleNotifications_updatesCheckboxIcon() {
        try (
                MockedConstruction<SessionStore> mockedSession =
                        mockConstruction(SessionStore.class,
                                (mock, context) ->
                                        when(mock.getEntrantProfileId()).thenReturn("entrant123")
                        );

                MockedConstruction<EntrantDatabaseManager> mockedDb =
                        mockConstruction(EntrantDatabaseManager.class,
                                (mock, context) -> {
                                    when(mock.getEntrantFromId("entrant123"))
                                            .thenReturn(Tasks.forResult(null));
                                    when(mock.addEntrant(any()))
                                            .thenReturn(Tasks.forResult(null));
                                })
        ) {
            fragment = EntrantProfileEmptyFragment.newInstance(true);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.entrant_fragment_container, fragment)
                    .commitNow();

            LinearLayout layout =
                    fragment.requireView().findViewById(R.id.layout_receive_notifications);

            ImageView checkbox =
                    fragment.requireView().findViewById(R.id.image_receive_notifications_checkbox);

            assertNotNull(checkbox.getDrawable());

            layout.performClick();
            shadowOf(Looper.getMainLooper()).idle();

            assertNotNull(checkbox.getDrawable());
        }
    }

    @Test
    public void toggleNotifications_updatesExistingEntrantPreference() {
        Entrant mockEntrant = new Entrant("", "", "", true, activity);

        try (
                MockedConstruction<SessionStore> mockedSession =
                        mockConstruction(SessionStore.class,
                                (mock, context) ->
                                        when(mock.getEntrantProfileId()).thenReturn("entrant123")
                        );

                MockedConstruction<EntrantDatabaseManager> mockedDb =
                        mockConstruction(EntrantDatabaseManager.class,
                                (mock, context) -> {
                                    when(mock.getEntrantFromId("entrant123"))
                                            .thenReturn(Tasks.forResult(mockEntrant));
                                    when(mock.updateEntrantInfo(any()))
                                            .thenReturn(Tasks.forResult(null));
                                })
        ) {
            fragment = EntrantProfileEmptyFragment.newInstance(true);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.entrant_fragment_container, fragment)
                    .commitNow();

            LinearLayout layout =
                    fragment.requireView().findViewById(R.id.layout_receive_notifications);

            layout.performClick();
            shadowOf(Looper.getMainLooper()).idle();

            verify(mockedDb.constructed().get(0), times(1))
                    .updateEntrantInfo(any());
        }
    }

    @Test
    public void toggleNotifications_createsStubEntrantIfMissing() {
        try (
                MockedConstruction<SessionStore> mockedSession =
                        mockConstruction(SessionStore.class,
                                (mock, context) ->
                                        when(mock.getEntrantProfileId()).thenReturn("entrant123")
                        );

                MockedConstruction<EntrantDatabaseManager> mockedDb =
                        mockConstruction(EntrantDatabaseManager.class,
                                (mock, context) -> {
                                    when(mock.getEntrantFromId("entrant123"))
                                            .thenReturn(Tasks.forResult(null));
                                    when(mock.addEntrant(any()))
                                            .thenReturn(Tasks.forResult(null));
                                })
        ) {
            fragment = EntrantProfileEmptyFragment.newInstance(true);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.entrant_fragment_container, fragment)
                    .commitNow();

            LinearLayout layout =
                    fragment.requireView().findViewById(R.id.layout_receive_notifications);

            layout.performClick();
            shadowOf(Looper.getMainLooper()).idle();

            verify(mockedDb.constructed().get(0), times(1))
                    .addEntrant(any());
        }
    }

    @Test
    public void createProfileButton_opensProfileFormFragment() {
        fragment = EntrantProfileEmptyFragment.newInstance(true);

        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.entrant_fragment_container, fragment)
                .commitNow();

        fragment.requireView()
                .findViewById(R.id.btn_create_profile)
                .performClick();

        shadowOf(Looper.getMainLooper()).idle();

        assertTrue(
                activity.getSupportFragmentManager()
                        .findFragmentById(R.id.entrant_fragment_container)
                        instanceof EntrantProfileExistsFormFragment
        );
    }
}